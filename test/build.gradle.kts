import java.nio.file.Files
import java.util.Locale

// Create new ./test/HeadlessMC folder
// Download into new HeadlessMC folder: https://github.com/headlesshq/headlessmc/releases/download/2.9.0/headlessmc-launcher-2.9.0.jar

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

val headlessJar: ConfigurableFileCollection = files("HeadlessMC/headlessmc-launcher-2.9.0.jar")
val headlessMain: String = "io.github.headlesshq.headlessmc.launcher.Main"

fun serverDlArgs(platform: String, mcVersion: String): List<String> {
    return "--command server add $platform $mcVersion pcf-$platform-$mcVersion".split(" ")
}

// Generate setup tasks for each version of Forge and NeoForge
versions.forEach { (platform, mcVersions) ->
    mcVersions.forEach { mcVersion ->
        val mcVersionName = if (mcVersion.startsWith("1.")) {
            mcVersion.substring(2) } else { mcVersion }
            .replace(".", "_")
        val platformName = if (platform == "neoforge") "NeoForge" else platform.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
        val taskName = "setup${platformName}${mcVersionName}"
        tasks.register<JavaExec>(taskName) {
            group = "setup"
            classpath += headlessJar
            mainClass.set(headlessMain)
            args(serverDlArgs(platform, mcVersion))
            doLast {
                val common = file("HeadlessMC/libraries").apply { mkdirs() }
                val template = file("HeadlessMC/template")
                val base = file("HeadlessMC/servers/$platform/$mcVersion")
                val parents = base.walk().maxDepth(3)
                    .filter { it.isDirectory && it.name == "libraries" }
                    .map { it.parentFile }
                    .toList()

                parents.forEach { parent ->
                    copy { from(template); into(parent) }

                    val versioned = parent.resolve("libraries")
                    if (Files.isSymbolicLink(versioned.toPath())) { return@doLast }
                    copy { from(versioned); into(common) }
                    versioned.deleteRecursively()

                    ant.invokeMethod("symlink", mapOf(
                        "resource" to common.absolutePath,
                        "link" to versioned.absolutePath
                    ))
                }
            }
        }
    }
}
