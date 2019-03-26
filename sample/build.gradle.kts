import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    idea
    kotlin("jvm") version "1.3.21"
    kotlin("kapt") version "1.3.21"
    id("fiber") version "1.0.0-dev"
}

idea {
    module {
        excludeDirs.add(file("run"))
    }
}
//version = "$major.$minor.$patch-$buildnumber-${Env.branch}"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
repositories {
    mavenLocal()
    maven(url = "https://kotlin.bintray.com/kotlinx") {
        name = "kotlinx"
    }
    mavenCentral()
    jcenter()
}

application {
    mainClassName = "config.Main"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(("fiber:annotations:1.0.0+"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
