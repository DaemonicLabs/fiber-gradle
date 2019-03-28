plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("kotlinx-serialization") version Kotlin.version
}

val major = Constants.major
val minor = Constants.minor
val patch = Constants.patch
// use "SNAPSHOT" on CI and "dev" locally
val versionSuffix = System.getenv("BUILD_NUMBER")?.let { "SNAPSHOT" } ?: "dev"
version = "$major.$minor.$patch-$versionSuffix"

dependencies {
    implementation(project(":processor"))

    implementation ("org.zeroturnaround:zt-zip:1.13")
//    implementation(
//        group = "org.jetbrains.kotlinx",
//        name = "kotlinx-serialization-runtime",
//        version = KotlinX.Serialization.version
//    )
}

gradlePlugin {
    plugins {
        register("fiber") {
            id = "fiber"
            implementationClass = "fiber.FiberPlugin"
        }
    }
}
