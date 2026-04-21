val forge: SourceSet by sourceSets.creating
val mainCompileOnly: Configuration by configurations.getting
val forgeCompileOnly: Configuration by configurations.getting {
    extendsFrom(mainCompileOnly)
}

unimined.minecraft(forge) {
    version(minecraftVersion)
    side("server")
    mappings {
        parchment(parchmentMinecraft, parchmentVersion)
        mojmap()
        devFallbackNamespace("official")
    }
    minecraftForge {
        loader(forgeVersion)
        mixinConfig("$modId.mixins.v14_4.forge.json")
        accessTransformer(aw2at(rootProject.file("common/src/main/resources/accessWidener.aw")))
    }
}

dependencies {
    forgeCompileOnly(libs.mixin)
    forgeCompileOnly(srcSetAsDep(":deobsf:v26_1", "forge"))
    forgeCompileOnly(project(":common"))
}

tasks.jar {
    dependsOn("remapForgeJar")
    from(jarToFiles("remapForgeJar"))
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
