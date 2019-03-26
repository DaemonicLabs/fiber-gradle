import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish`
    kotlin("jvm")
    kotlin("kapt")
    id("kotlinx-serialization") version Kotlin.version
}

dependencies {
    implementation(kotlin("stdlib", Kotlin.version))
    implementation(project(":annotations"))

    logger.lifecycle(System.getenv("JAVA_HOME"))
    implementation(files("${System.getenv("JAVA_HOME")}/lib/tools.jar"))

    implementation("com.google.auto.service:auto-service:1.0-rc5")
    kapt("com.google.auto.service:auto-service:1.0-rc5")

    implementation(
        group = "org.jetbrains.kotlinx",
        name = "kotlinx-serialization-runtime",
        version = KotlinX.Serialization.version
    )
}

kapt {
//    arguments {
//        arg("configClasses", "value", "value2")
//    }
    correctErrorTypes = true
    strictMode = false
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val sourcesJar = tasks.create<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

val javadoc = tasks.getByName<Javadoc>("javadoc") {}
val javadocJar = tasks.create<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(javadoc)
}

publishing {
    publications {
        create("main", MavenPublication::class.java) {
            from(components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)
        }
    }
}