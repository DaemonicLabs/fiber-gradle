pluginManagement {
    repositories {
        mavenLocal()
        maven(url = "https://maven.fabricmc.net/") {
            name = "Fabric MC"
        }
        jcenter()
        gradlePluginPortal()
    }
}
rootProject.name = "fiber-sample"