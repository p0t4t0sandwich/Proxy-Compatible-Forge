import java.nio.file.Files
import java.util.Locale

// Create new ./test/HeadlessMC folder
// Download into new HeadlessMC folder: https://github.com/headlesshq/headlessmc/releases/download/2.9.0/headlessmc-launcher-2.9.0.jar

// Create new ./test/HeadlessMC/velocity folder
// Download into new velocity folder: https://papermc.io/downloads/velocity
// Rename to velocity-proxy.jar

// Create new ./test/HeadlessMC/velocity/modern/plugins folder
// Download into velocity/modern/plugins folder: https://modrinth.com/plugin/ambassador
// Repeat for "legacy" and "bungeeguard" if you plan to test them

// If you're testing modern forwarding with 1.7.2-1.12.2 you'll need the Velocity fork which adds that functionality
// Build from: https://github.com/p0t4t0sandwich/Velocity/tree/feat/modern-forwarding-legacy


val versions: Map<String, List<String>> = mapOf(
    "forge" to listOf(
        "1.7.10", "1.12.2",
        "1.13.2",
        "1.14.4", "1.15.2", "1.16.5",
        "1.17.1", "1.18.2", "1.19", "1.19.2", "1.19.4", "1.20.1", "1.20.2", "1.20.4",
        "1.21.1", "1.21.5",
        "26.1.2"
    ),
    "neoforge" to listOf(
        "1.20.2", "1.20.4", "1.21.1", "1.21.5",
        "26.1.2"
    )
)

val forwardingModes = listOf(
    "legacy",
    "bungeeguard",
    "modern"
)

val headlessJar: ConfigurableFileCollection = files("HeadlessMC/headlessmc-launcher-2.9.0.jar")
val headlessMain: String = "io.github.headlesshq.headlessmc.launcher.Main"
var headlessJavaArgs = listOf(
    "-Dhmc.mcdir=./HeadlessMC/.minecraft",
    "-Dhmc.gamedir=./HeadlessMC/clients",
    "-Dhmc.game.dir.for.each.version=true",
    //"-Dhmc.always.lwjgl.flag=true",
    "-Dhmc.always.pauls.flag=true",

    "-Dhmc.server.accept.eula=true",
    "--enable-native-access=ALL-UNNAMED"
)

// NOTE: HeadlessMC only supports offline mode in a headless environment
//  If a full client is needed for testing this must be disabled, or another client must be used.
//  If you decide the former, be sure that you DO NOT COMMIT ./test/HeadlessMC/auth.
//  Also note that in such cases the templates/server/ops.json will need to be updated.
//  To enable online in dev:
//  - set run_offline=false in gradle.properties
//  - enable online mode in HeadlessMC/velocity/velocity.toml
//  - run `./gradlew headlessmc`
//  - enter the "login" command and follow the instructions
val offlineHeadlessJavaArgs = listOf(
    "-Dhmc.offline=true",
    "-Dhmc.offline.username=dev",
    "-Dhmc.offline.uuid=8c6c43b32bef3c48a644fe1d4c106c17",
)
if (project.findProperty("run_offline") == "true") {
    headlessJavaArgs += offlineHeadlessJavaArgs
}


fun taskSuffix(platform: String, mcVersion: String): String {
    val mcVersionName = if (mcVersion.startsWith("1.")) {
        mcVersion.substring(2) } else { mcVersion }
        .replace(".", "_")
    val platformName = if (platform == "neoforge") "NeoForge" else platform.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
    return "$platformName$mcVersionName"
}

// General HeadlessMC task
tasks.register<JavaExec>("headlessmc") {
    group = "headlessmc"
    classpath += headlessJar
    mainClass.set(headlessMain)
    jvmArgs(headlessJavaArgs)
    standardInput = System.`in`
}

// Generate server setup tasks for each platform and version
versions.forEach { (platform, mcVersions) ->
    mcVersions.forEach { mcVersion ->
        val taskName = "setup${taskSuffix(platform, mcVersion)}"
        tasks.register<JavaExec>(taskName) {
            group = "setup_server"
            classpath += headlessJar
            mainClass.set(headlessMain)
            jvmArgs(headlessJavaArgs)
            args("--command server add $platform $mcVersion pcf-$platform-$mcVersion".split(" "))
            doLast {
                val common = file("HeadlessMC/libraries").apply { mkdirs() }
                val template = file("HeadlessMC/templates/server")
                val base = file("HeadlessMC/servers/$platform/$mcVersion")
                val parents = base.walk().maxDepth(3)
                    .filter { it.isDirectory && it.name == "libraries" }
                    .map { it.parentFile }
                    .toList()

                parents.forEach { parent ->
                    copy { from(template); into(parent) }

                    val libraries = parent.resolve("libraries")
                    if (Files.isSymbolicLink(libraries.toPath())) { return@doLast }
                    copy { from(libraries); into(common) }
                    libraries.deleteRecursively()

                    ant.invokeMethod("symlink", mapOf(
                        "resource" to common.absolutePath,
                        "link" to libraries.absolutePath
                    ))
                }
            }
        }
    }
}

// Generate server run tasks for each platform and version
versions.forEach { (platform, mcVersions) ->
    forwardingModes.forEach { forwardingMode ->
        val parsedMode = forwardingMode.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        mcVersions.forEach { mcVersion ->
            val taskName = "run${taskSuffix(platform, mcVersion)}Server$parsedMode"
            tasks.register<JavaExec>(taskName) {
                val finalJar = rootProject.tasks.getByName<Jar>("finalJar")
                dependsOn(finalJar)
                doFirst {
                    val serverDir = file("HeadlessMC/servers/$platform/$mcVersion")
                    if (!serverDir.exists()) {
                        throw GradleException("Server for $platform $mcVersion not found. Please run setup task first.")
                    }
                    val parents = serverDir.walk().maxDepth(3)
                        .filter { it.isDirectory && it.name == "libraries" }
                        .map { it.parentFile }
                        .toList()

                    parents.forEach { parent ->
                        val mods = parent.resolve("mods").apply { mkdirs() }
                        // Remove any proxy-compatible-forge jars from mods folder
                        parent.resolve("mods").listFiles()
                            ?.forEach { if (it.name.startsWith("proxy-compatible-forge-")) it.delete() }
                        copy { from(files(finalJar.archiveFile)); into(mods) }
                    }
                }
                group = "run_server_$forwardingMode"
                classpath += headlessJar
                mainClass.set(headlessMain)
                environment("PCF_FORWARDING_MODE", forwardingMode)
                jvmArgs(headlessJavaArgs)
                args("--command server launch pcf-$platform-$mcVersion".split(" "))
                standardInput = System.`in`
            }
        }
    }
}

// Generate client run tasks for each platform and version
versions.forEach { (platform, mcVersions) ->
    forwardingModes.forEach { forwardingMode ->
        val parsedMode = forwardingMode.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        mcVersions.forEach { mcVersion ->
            val taskName = "run${taskSuffix(platform, mcVersion)}Client$parsedMode"
            tasks.register<JavaExec>(taskName) {
                group = "run_client_$forwardingMode"
                classpath += headlessJar
                mainClass.set(headlessMain)
                jvmArgs(headlessJavaArgs)
                when (forwardingMode) {
                    "legacy" -> jvmArgs("-Dhmc.gameargs=--server 127.0.0.1 --port 25578 --quickPlayMultiplayer 127.0.0.1:25578")
                    "bungeeguard" -> jvmArgs("-Dhmc.gameargs=--server 127.0.0.1 --port 25579 --quickPlayMultiplayer 127.0.0.1:25579")
                    "modern" -> jvmArgs("-Dhmc.gameargs=--server 127.0.0.1 --port 25577 --quickPlayMultiplayer 127.0.0.1:25577")
                }
                args("--command launch $platform:$mcVersion".split(" "))
                standardInput = System.`in`
            }
        }
    }
}

val velocityJar: ConfigurableFileCollection = files("HeadlessMC/velocity/velocity-proxy.jar")
val velocityMain: String = "com.velocitypowered.proxy.Velocity"
val velocityJavaArgs = listOf(
    "-Dvelocity.packet-decode-logging=true",
    "-Dvelocity.strictErrorHandling=true",
    "-Dvelocity.max-known-packs=499",
    "-Dvelocity.max-plugin-message-payload-size=1000000",
    "--enable-native-access=ALL-UNNAMED"
)

// Set up Velocity
forwardingModes.forEach { forwardingMode ->
    val parsedMode = forwardingMode.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    tasks.register<Copy>("setupVelocity$parsedMode") {
        group = "setup_proxy"
        from(files(
            "HeadlessMC/templates/velocity/common",
            "HeadlessMC/templates/velocity/$forwardingMode"
        ))
        into(file("HeadlessMC/velocity/$forwardingMode"))
    }
}

// Run Velocity
forwardingModes.forEach { forwardingMode ->
    val parsedMode = forwardingMode.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    tasks.register<JavaExec>("runVelocity$parsedMode") {
        group = "run_proxy"
        workingDir(file("HeadlessMC/velocity/$forwardingMode"))
        classpath += velocityJar
        mainClass.set(velocityMain)
        jvmArgs(velocityJavaArgs)
        standardInput = System.`in`
    }
}
