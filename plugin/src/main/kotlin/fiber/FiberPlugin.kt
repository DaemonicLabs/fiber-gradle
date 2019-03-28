package fiber

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import kotlinx.serialization.map
import kotlinx.serialization.serializer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByName
import org.zeroturnaround.zip.ZipEntryCallback
import org.zeroturnaround.zip.ZipUtil
import org.zeroturnaround.zip.transform.StringZipEntryTransformer
import org.zeroturnaround.zip.transform.ZipEntryTransformerEntry
import java.io.IOException
import java.io.OutputStream
import java.util.zip.ZipEntry

open class FiberPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.pluginManager.apply("org.gradle.java-library")
        val fiberConfiguration = project.configurations.create("fiber")
        val implementation = project.configurations.getByName("implementation")
        val annotationProcessor = if (project.configurations.asMap.containsKey("kapt"))
            project.configurations.getByName("kapt")
        else
            project.configurations.getByName("annotationProcessor")
        project.dependencies {
            add(fiberConfiguration.name, "fiber:extractor:${PluginConstants.FULL_VERSION}")
            add(implementation.name, "fiber:annotations:${PluginConstants.FULL_VERSION}")
            add(annotationProcessor.name, "fiber:processor:${PluginConstants.FULL_VERSION}")
        }
        project.afterEvaluate {
            val serializer: KSerializer<ConfigField> = ConfigField.serializer()
            val mapSerializer: KSerializer<Map<String, List<ConfigField>>> =
                (String.serializer() to serializer.list).map

            val json = Json(
                indented = true,
                encodeDefaults = false
            )

            val jar = tasks.getByName<Jar>("jar")
            tasks.create<JavaExec>("extractInfo") {
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

                    lateinit var classnames: Array<String>
                    ZipUtil.iterate(
                        jar.archiveFile.get().asFile,
                        ZipEntryCallback { input, entry: ZipEntry ->
                            if (entry.name == "fiber.schema.json") {
                                val text = input.bufferedReader().readText()
                                val schema = json.parse(mapSerializer, text)
                                classnames = schema.keys.toTypedArray()
                            }
                        }
                    )
                    args(
                        *classnames
//                    "config.ArrayConfig",
//                    "config.NestedConfig",
//                    "config.KottonKonfig.NestedInner",
//                    "config.KottonKonfig",
//                    "io.github.cottonmc.cotton.config.CottonConfig"
                    )
                }
                group = "build"
                dependsOn(jar)
                mustRunAfter(jar)
                main = "fiber.ExtractInfo"

                standardOutput = object : OutputStream() {
                    private val string = StringBuilder()
                    @Throws(IOException::class)
                    override fun write(b: Int) {
                        string.append(b.toChar())
                    }

                    override fun toString(): String {
                        return string.toString()
                    }
                }

                doLast {

                    val extractSerializer =
                        (String.serializer() to (String.serializer() to String.serializer()).map).map
                    val extractResult = json.parse(extractSerializer, standardOutput.toString())
                    logger.debug("extractResult: $extractResult")

                    val schemaFileNames = setOf("fiber.schema.json")
                    ZipUtil.transformEntries(
                        jar.archiveFile.get().asFile,
                        schemaFileNames.map { f ->
                            ZipEntryTransformerEntry(f, object : StringZipEntryTransformer("UTF-8") {
                                override fun transform(zipEntry: ZipEntry?, input: String): String {
                                    logger.lifecycle("reading json")
                                    logger.debug(input)
                                    val schema = json.parse(mapSerializer, input)

                                    logger.lifecycle("parsed schema")
                                    logger.debug(schema.toString())
                                    val newSchema = schema.mapValues { (className, fields) ->
                                        val extractedFields = extractResult[className]
                                        if(extractedFields != null) {
                                            fields.map { field ->
                                                val original = schema.getValue(className).find { it.name == field.name }
                                                    ?: throw IllegalStateException("could not find field with name `${field.name}`")
                                                val value = extractedFields[original.name]
                                                if (value != null)
                                                    original.copy(
                                                        value = value
                                                    )
                                                else
                                                    original
                                            }
                                        } else fields

                                    }

                                    return json.stringify(mapSerializer, newSchema)
                                }
                            })
                        }.toTypedArray()
                    )
                }

            }
        }
    }
}