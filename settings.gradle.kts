rootProject.name = "zientis-server"

include(
    "zientis-core",
    "zientis-multiworld",
    "zientis-economy",
    "zientis-display", 
    "zientis-nations",
    "zientis-social"
)

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://repo.codemc.org/repository/maven-public/")
        maven("https://jitpack.io")
    }
}