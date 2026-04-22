plugins {
    alias(libs.plugins.blossom)
    alias(libs.plugins.shadow)
}

base {
    archivesName = "$modId-common"
}

sourceSets.main {
    blossom.javaSources {
        property("mod_id", modId)
        property("mod_name", modName)
        property("version", version.toString())
        property("license", license)
        property("authors", authors)
        property("description", description ?: "")
        property("homepage_url", homepageUrl)
    }
}

dependencies {
    compileOnly(libs.annotations)
    compileOnly(libs.mojang.authlib)
    compileOnly(libs.mojang.brigadier)
    compileOnly(libs.gson)
    compileOnly(libs.guava)
    compileOnly(libs.jspecify)
    compileOnly(libs.mixin)
    compileOnly(libs.netty.buffer)
    compileOnly(libs.netty.codec)
    compileOnly(libs.netty.unix.common)
    compileOnly(libs.asm.tree)

    compileOnly(libs.entrypoint.spoof)
    implementation(libs.taterlib.lite.base)
    implementation(libs.taterlib.lite.core)
    implementation(libs.taterlib.lite.metadata)
    implementation(libs.taterlib.lite.muxins)
    implementation(libs.taterlib.lite.network)
}

tasks.withType<ProcessResources> {
    filteringCharset = Charsets.UTF_8.name()
    filesMatching(listOf(
            "META-INF/mods.toml",
            "META-INF/neoforge.mods.toml",
            "mcmod.info",
            "pack.mcmeta",
    )) {
        expand(project.properties)
    }
}

tasks.shadowJar {
    archiveClassifier = "shaded"
}

tasks.build.get().dependsOn(tasks.shadowJar)
