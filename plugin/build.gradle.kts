plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
}

val major = Constants.major
val minor = Constants.minor
val patch = Constants.patch
// use "SNAPSHOT" on CI and "dev" locally
val versionSuffix = System.getenv("BUILD_NUMBER")?.let { "SNAPSHOT" } ?: "dev"
version = "$major.$minor.$patch-$versionSuffix"

gradlePlugin {
    plugins {
        register("fiber") {
            id = "fiber"
            implementationClass = "fiber.FiberPlugin"
        }
    }
}
