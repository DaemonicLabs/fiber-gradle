package fiber

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByName

open class FiberPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.pluginManager.apply("org.gradle.java-library")
        val fiberConfiguration = project.configurations.create("fiber")
        val implementation = project.configurations.getByName("implementation")
        val annotationProcessor = if(project.configurations.asMap.containsKey("kapt"))
            project.configurations.getByName("kapt")
         else
            project.configurations.getByName("annotationProcessor")
        project.dependencies {
            add(fiberConfiguration.name, "fiber:extractor:${PluginConstants.FULL_VERSION}")
            add(implementation.name, "fiber:annotations:${PluginConstants.FULL_VERSION}")
            add(annotationProcessor.name, "fiber:processor:${PluginConstants.FULL_VERSION}")
        }
        project.afterEvaluate {
            val jar = tasks.getByName<Jar>("jar")
            tasks.create<JavaExec>("extractInfo") {
                group = "build"
                dependsOn(jar)
                mustRunAfter(jar)
                main = "fiber.ExtractInfo"
                args(
                    "config.ArrayConfig",
                    "config.NestedConfig",
                    "config.KottonKonfig.NestedInner",
                    "config.KottonKonfig",
                    "io.github.cottonmc.cotton.config.CottonConfig"
                )
                doFirst {
                    fiberConfiguration.resolve().forEach {
                        logger.lifecycle("adding $it to classpath")
                        classpath(it)
                    }
                    configurations.getByName("apiDependenciesMetadata").resolve().forEach {
                        logger.lifecycle("adding $it to classpath")
                        classpath(it)
                    }
                    configurations.getByName("implementationDependenciesMetadata").resolve().forEach {
                        logger.lifecycle("adding $it to classpath")
                        classpath(it)
                    }
                    classpath(jar.archiveFile)
                }
            }
        }
    }
}