val forge: SourceSet by sourceSets.creating
val neoforge: SourceSet by sourceSets.creating
val mainCompileOnly: Configuration by configurations.getting
val forgeCompileOnly: Configuration by configurations.getting {
    extendsFrom(mainCompileOnly)
}
val neoforgeCompileOnly: Configuration by configurations.getting {
    extendsFrom(mainCompileOnly)
}

unimined.minecraft(sourceSets.main.get()) {
    version(minecraftVersion)
    neoForge { // TODO: Use a custom patcher to apply the widener
        loader(neoforgeVersion)
        accessTransformer(aw2at(rootProject.file("common/src/main/resources/accessWidener.aw")))
    }
}

unimined.minecraft(forge) {
    version(minecraftVersion)
    minecraftForge {
        loader(forgeVersion)
    }
}

unimined.minecraft(neoforge) {
    version(minecraftVersion)
    neoForge {
        loader(neoforgeVersion)
    }
}

dependencies {
    evaluationDependsOn(":modern:v16_5")
    forgeCompileOnly(srcSetAsDep(":modern:v16_5", "forge"))
    forgeCompileOnly(project(":common"))
    neoforgeCompileOnly(project(":common"))
    mainCompileOnly(libs.asm.tree)
    mainCompileOnly(libs.mixin)
}

tasks.jar {
    from(forge.output, neoforge.output)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
