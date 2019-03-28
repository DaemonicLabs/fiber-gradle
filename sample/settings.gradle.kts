pluginManagement {
    repositories {
        mavenLocal()
        maven(url = "https://kotlin.bintray.com/kotlinx") {
            name = "kotlinx"
        }
        maven(url = "https://maven.fabricmc.net/") {
            name = "Fabric MC"
        }
        jcenter()
        gradlePluginPortal()
    }
}
rootProject.name = "fiber-sample"