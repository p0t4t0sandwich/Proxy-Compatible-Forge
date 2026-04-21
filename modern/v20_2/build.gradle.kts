val mainCompileOnly: Configuration by configurations.getting

unimined.minecraft(sourceSets.main.get()) {
    version(minecraftVersion)
    side("server")
    neoForge {
        loader(neoforgeVersion)
    }
    mappings {
        parchment(parchmentMinecraft, parchmentVersion)
        mojmap()
        devFallbackNamespace("official")
    }
}

dependencies {
    mainCompileOnly(project(":common"))
    mainCompileOnly(srcSetAsDep(":deobsf:v26_1", "neoforge"))
}
