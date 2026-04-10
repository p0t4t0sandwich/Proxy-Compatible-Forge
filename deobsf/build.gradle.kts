plugins {
    id("xyz.wagyourtail.unimined") version("1.4.2-SNAPSHOT")
}

subprojects {
    apply(plugin = "xyz.wagyourtail.unimined")

    base {
        archivesName = "${modId}-${minecraftVersion}"
    }

    val mainCompileOnly: Configuration by configurations.getting

    dependencies {
        listOf(
            project(":common")
        ).forEach {
            mainCompileOnly(it)
        }
    }
}
