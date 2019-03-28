plugins {
    `java-library`
    `maven-publish`
//    kotlin("jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
//    kotlin("stdlib", Kotlin.version)
    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    api(group = "com.google.code.gson", name = "gson", version = "2.8.5")
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