import moe.nikky.counter.CounterExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import plugin.generateconstants.GenerateConstantsTask

plugins {
    `maven-publish`
    application
    idea
    `java-library`
//    id("constantsGenerator")
    kotlin("jvm") version Kotlin.version
    kotlin("kapt") version Kotlin.version
    id("moe.nikky.persistentCounter") version "0.0.7-SNAPSHOT"
}

idea {
    module {
        excludeDirs.add(file("run"))
    }
}

//base {
//    archivesBaseName = "fiber-gradle"
//}

val major = Constants.major
val minor = Constants.minor
val patch = Constants.patch
counter {
    variable(id = "buildnumber", key = "$major.$minor.$patch")
}
val counter: CounterExtension = extensions.getByType()
val buildnumber = counter.get("buildnumber")
val versionSuffix = System.getenv("BUILD_NUMBER")?.let { "$buildnumber" } ?: "dev"

allprojects {
    group = Constants.group
    description = Constants.description
    version = "$major.$minor.$patch-$versionSuffix"

    repositories {
        mavenLocal()
        maven(url = "https://kotlin.bintray.com/kotlinx") {
            name = "kotlinx"
        }
        mavenCentral()
        jcenter()
    }

    apply {
        plugin("java-library")
    }
    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
val kotlinProjects = listOf(
    project(":extractor"),
    project(":plugin")
)
subprojects {

    if(this in kotlinProjects) {
        apply {
            plugin("constantsGenerator")
            plugin("kotlin")
        }
        configure<ConstantsExtension> {
            constantsObject(
                pkg = "fiber",
                className = project.name
                    .split("-")
                    .joinToString("") {
                        it.capitalize()
                    } + "Constants"
            ) {
                field("BUILD_NUMBER") value buildnumber
                field("MAJOR_VERSION") value major
                field("MINOR_VERSION") value minor
                field("PATCH_VERSION") value patch
                field("VERSION") value "$major.$minor.$patch"
                field("FULL_VERSION") value project.version as String
            }
        }

        val generateConstants by tasks.getting(GenerateConstantsTask::class) {
            kotlin.sourceSets["main"].kotlin.srcDir(outputFolder)
        }

// TODO depend on kotlin tasks in the plugin
        tasks.withType<KotlinCompile> {
            dependsOn(generateConstants)
        }
    }
    apply {
        plugin("maven-publish")
    }
    publishing {
        repositories {
            maven(url = "http://mavenupload.modmuss50.me/") {
                val mavenPass: String? = project.properties["mavenPass"] as String?
                mavenPass?.let {
                    credentials {
                        username = "buildslave"
                        password = mavenPass
                    }
                }
            }
        }
    }
}

application {
    mainClassName = "config.Main"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":annotations"))

    api(group = "io.github.microutils", name = "kotlin-logging", version = Versions.kotlinLogging)
    implementation(group = "ch.qos.logback", name = "logback-classic", version = Versions.logbackClassic)

    kapt(project(":processor"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

