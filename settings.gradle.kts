pluginManagement {
    repositories {
        maven(url = "https://maven.fabricmc.net/") {
            name = "Fabric MC"
        }
        jcenter()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if(requested.id.id == "kotlinx-serialization") {
                useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
            }
        }
    }
}
rootProject.name = Constants.name

include("annotations")
include("extractor")
include("processor")
include("plugin")