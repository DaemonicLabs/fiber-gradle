package fiber

import com.google.auto.service.AutoService
import com.sun.tools.javac.code.Type
import fiber.annotations.Comment
import fiber.annotations.ConfigFile
import fiber.annotations.RangeValidatorFloat
import fiber.annotations.RangeValidatorInt
import fiber.annotations.RangeValidatorLong
import fiber.annotations.RegexValidator
import fiber.annotations.SetValidatorString
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import kotlinx.serialization.map
import kotlinx.serialization.serializer
import java.io.IOException
import java.io.PrintStream
import java.io.PrintWriter
import java.io.StringWriter
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.FilerException
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedOptions
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic
import javax.tools.FileObject
import javax.tools.StandardLocation

@SupportedAnnotationTypes(
    "fiber.annotations.ConfigFile"
)
@AutoService(Processor::class) // For registering the service
@SupportedSourceVersion(SourceVersion.RELEASE_8) // to support Java 8
@SupportedOptions(
    ConfigProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME,
    "debug",
    "verify"
)
class ConfigProcessor : AbstractProcessor() {
    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        return try {
            processImpl(annotations, roundEnv)
        } catch (e: Exception) {
            // We don't allow exceptions of any kind to propagate to the compiler
            val writer = StringWriter()
            e.printStackTrace(PrintWriter(writer))
            log(writer.toString())
            true
        }
    }

    val types: MutableMap<String, List<ConfigField>> = mutableMapOf()
    fun processImpl(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
//        openLog()
        val generatedSourcesRoot: String = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME] ?: kotlin.run {
            error("Can't find the target directory for generated Kotlin files.")
            "build/generated/resources"
        }

        log("starting log ${System.currentTimeMillis()}")

        log("options: ${processingEnv.options}")
        processingEnv.options.forEach { (key, value) ->
            log("""options["$key"] = $value""")
        }

        log("annotations: $annotations")
        log("roundEnv: $roundEnv")

        if (annotations.isEmpty()) return false

        val configFiles: MutableList<Element> = mutableListOf()
        roundEnv.getElementsAnnotatedWith(ConfigFile::class.java).forEach { configFileElement ->
            configFiles += configFileElement
            // TODO: set rootElement type name
            log("configFileElement: $configFileElement")
            log("configFileElement::class: ${configFileElement::class}")
            log("  kind: ${configFileElement.kind}")

            val typeElement = processingEnv.elementUtils.getTypeElement(configFileElement.toString())
            addType(typeElement)
        }
        annotations.forEach { annotation ->
            log("processing annotation '$annotation'")
            roundEnv.getElementsAnnotatedWith(annotation).forEach { element ->
                log("  element: $element")
                log("    kind: ${element.kind}")

            }
            log("")
        }
        roundEnv.rootElements.forEach { rootElement ->
            log("rootElement: $rootElement")
            log("  kind: ${rootElement.kind}")
            log("  enclosed elements: ${rootElement.enclosedElements}")
            log("  enclosing elements: ${rootElement.enclosingElement}")
        }
//        roundEnv.getElementsAnnotatedWith(Comment::class.java).forEach { comment ->
//            log("element: $comment")
//            val configFile = configFiles.find { configFile ->
//                comment.enclosingElement == configFile
//            }
//            log("  configFile = $configFile")
//            log("")
//        }

        val serializer: KSerializer<ConfigField> = ConfigField.serializer()
        val mapSerializer = (String.serializer() to serializer.list).map

        val json = Json(
            indented = true,
            encodeDefaults = false
        )

        val jsonString = json.stringify(mapSerializer, types)

        try {
            val fileObj = processingEnv.filer.createResource(StandardLocation.CLASS_OUTPUT, "", "fiber.schema.json")

            fileObj.openOutputStream().use {
                it.bufferedWriter().use {
                    it.write(jsonString)
                }
            }
        } catch (e: FilerException) {
            log("test 2")
            val writer = StringWriter()
            e.printStackTrace(PrintWriter(writer))
            log(writer.toString())
        }

//        val sourceFolder = File(generatedSourcesRoot).apply {
//            mkdirs()
//        }
//        val testFile = sourceFolder.resolve("test.json").apply {
//            createNewFile()
//        }
//
//        // Testing if i can write kt files
//        val sourceTest = File(generatedSourcesRoot).resolve("fiber.schema.json").apply {
//            createNewFile()
//        }
//        sourceTest.writeText(
//            """
//            const val version = 1
//        """.trimIndent()
//        )
//
//        testFile.writeText(
//            """
//            {
//                "version": 1
//            }
//        """.trimIndent()
//        )

        types.forEach { key, fields ->
            log("type: $key")
            fields.forEach { field ->
                log("  field: $field")
            }
        }

        // TODO serialize types

        // Use stored value for output stream
//        closeLog()
        log("This will be written on the console!")
        return false
    }

    fun addType(typeElement: TypeElement) {
        // parse type
        if (types.containsKey(typeElement.toString())) {
            log("type: $typeElement is already registered")
            log("${types[typeElement.toString()]}")
            return
        }
        if (typeElement.toString().startsWith("java.lang.")) {
            log("not processing builtin types")
            return
        }
        // DO not add enums as types
        if(typeElement.kind == ElementKind.ENUM) {
            log("not processing enum types")
            return
        }
        log(">>> processing $typeElement")
        val fields: MutableList<ConfigField> = mutableListOf()

//        val clazz = javaClass.classLoader.loadClass(typeElement.qualifiedName.toString())
//        val instance = clazz.constructors[0].newInstance()
//        log("default instance: $instance")

        val stringType = processingEnv.elementUtils.getTypeElement("java.lang.String").asType()
        val booleanType = processingEnv.elementUtils.getTypeElement("java.lang.Boolean").asType()
        val byteType = processingEnv.elementUtils.getTypeElement("java.lang.Byte").asType()
        val shortType = processingEnv.elementUtils.getTypeElement("java.lang.Short").asType()
        val integerType = processingEnv.elementUtils.getTypeElement("java.lang.Integer").asType()
        val longType = processingEnv.elementUtils.getTypeElement("java.lang.Long").asType()
        val floatType = processingEnv.elementUtils.getTypeElement("java.lang.Float").asType()
        val doubleType = processingEnv.elementUtils.getTypeElement("java.lang.Double").asType()
        val enumType = processingEnv.elementUtils.getTypeElement(Enum::class.java.canonicalName).asType()


        enclosedLoop@ for (enclosedElement in typeElement.enclosedElements) {
            when (enclosedElement.kind) {
                ElementKind.METHOD, ElementKind.CONSTRUCTOR -> {
                    // ignore constructors
                    continue@enclosedLoop
                }
                else -> {
                }
            }
            log("    enclosed: $enclosedElement")
            log("      kind: ${enclosedElement.kind}")

            when (enclosedElement.kind) {
                ElementKind.METHOD -> {
                    // ignore getters and setters
                    continue@enclosedLoop
                }
                ElementKind.CLASS -> {
                    // ignore classes for now
                    continue@enclosedLoop
                }
                ElementKind.CONSTRUCTOR -> {
                    // ignore constructors
                    continue@enclosedLoop
                }
                ElementKind.FIELD -> {
                    val type = enclosedElement.asType()
                    log("      type: $type")
                    log("      type.kind: ${type.kind}")
                    log("      type::class: ${type::class}")
                    val subTypeElement = processingEnv.elementUtils.getTypeElement(type.toString()) ?: null
                    log("      typeElement: $subTypeElement")
                    if (subTypeElement != null) {
                        log("      typeElement::class: ${subTypeElement::class}")
                        log("      typeElement.superClass: ${subTypeElement.superclass}")
                    }

                    val constraints: MutableList<Constraint> = mutableListOf()

                    val fieldType: FieldType =  type.asFieldType(enclosedElement, constraints)

                    // comment
                    val comment = enclosedElement.getAnnotation(Comment::class.java)?.value

                    fields += ConfigField(
                        name = enclosedElement.simpleName.toString(),
                        type = fieldType,
                        value = "",
                        comment = comment,
                        constraints = constraints
                    )

                    log("      annotationMirrors: ${enclosedElement.annotationMirrors}")
                }
                ElementKind.ENUM_CONSTANT -> {
                    // ignore enum constants
                    // TODO: add to constraint "fabric:enum"

//                    fields += Field(
//                        name = enclosedElement.simpleName.toString(),
//                        type = FieldType("ENUM_CONSTANT")
//                    )
                }
                else -> {
                    TODO(enclosedElement.kind.name)
                }
            }
            //                enclosedElement.annotationMirrors
        }
        types[typeElement.qualifiedName.toString()] = fields
    }

    private fun TypeMirror.asFieldType(
        enclosedElement: Element,
        constraints: MutableList<Constraint>,
        notGenericParameter: Boolean = true
    ): FieldType {
        val type = this

        val stringType = processingEnv.elementUtils.getTypeElement("java.lang.String").asType()
        val booleanType = processingEnv.elementUtils.getTypeElement("java.lang.Boolean").asType()
        val byteType = processingEnv.elementUtils.getTypeElement("java.lang.Byte").asType()
        val shortType = processingEnv.elementUtils.getTypeElement("java.lang.Short").asType()
        val integerType = processingEnv.elementUtils.getTypeElement("java.lang.Integer").asType()
        val longType = processingEnv.elementUtils.getTypeElement("java.lang.Long").asType()
        val floatType = processingEnv.elementUtils.getTypeElement("java.lang.Float").asType()
        val doubleType = processingEnv.elementUtils.getTypeElement("java.lang.Double").asType()
        val enumType = processingEnv.elementUtils.getTypeElement(Enum::class.java.canonicalName).asType()

        val fieldType: FieldType = when (type) {
            is Type.ArrayType -> {
                log("        array type: $type")
                type.toString()

                when {
                    processingEnv.typeUtils.isAssignable(type.elemtype, stringType) -> {
                        if (notGenericParameter)
                            assertMatchingAnnotationType(enclosedElement, 0, "String")

                        constraints += getStringConstraints(enclosedElement, 0)
                    }
                    processingEnv.typeUtils.isAssignable(type.elemtype, byteType) -> {
                        if (notGenericParameter)
                        assertMatchingAnnotationType(enclosedElement, 0, "byte")
                    }
                }
                fiber.FieldType(type.toString())

                // TODO: check all primitive + String + enum arrays
                // TODO: apply validators
            }
//                        is Type.UnionClassType -> {
//                            log("        union class type: $type")
//
//                            type.toString()
//                        }
            is Type.ClassType -> {
                log("        class type: $type")

                val subTypeElement = processingEnv.elementUtils.getTypeElement(type.toString()) ?: null

                val typeParams = type.typarams_field
                log("          type params: $typeParams")

//                            val isList = type.toString().startsWith("java.util.List")
//                            val isSet = type.toString().startsWith("java.util.Set")
                when {
                    processingEnv.typeUtils.isAssignable(type, stringType) -> {
                        // load valid string values

                        if (notGenericParameter)
                        assertMatchingAnnotationType(enclosedElement, 0, "String")

                        constraints += getStringConstraints(enclosedElement, 0)

                        FieldType(type.toString())
                    }
                    processingEnv.typeUtils.isAssignable(type, byteType) -> {
                        //TODO: load min/max byte values
                        FieldType(type.toString())
                    }
                    processingEnv.typeUtils.isAssignable(type, shortType) -> {
                        //TODO: load min/max short values
                        FieldType(type.toString())
                    }
                    processingEnv.typeUtils.isAssignable(type, integerType) -> {
                        //TODO: load min/max int values
                        FieldType(type.toString())
                    }
                    type.isParameterized -> {
                        for ((index, parameter) in typeParams.withIndex()) {
                            log("parameter: $parameter")
                            if (processingEnv.typeUtils.isAssignable(parameter, stringType)) {
                                // load valid string values

                                // do not assert for matching annotations, since this is just a generic parameter

//                                continue
                            }

                            // TODO: check for all other builtins (java.lang.*)
                            // TODO: parse all validators

                            log("parameter::class: ${parameter::class}")
                            val parameterElement =
                                processingEnv.elementUtils.getTypeElement(parameter.toString()) ?: null
                            if (parameterElement != null) {
                                log("parameterElement::class: ${parameterElement::class}")
                                addType(parameterElement)
                            }
                        }
                        val parameters = typeParams.map {
                            log("mapping $it to FieldType")
                            it.asFieldType(enclosedElement, constraints, false)
                        }
                        // TODO: map paramters to FieldType
                        FieldType(type.tsym.qualifiedName.toString(), parameters)
                    }
                    subTypeElement?.kind == ElementKind.ENUM -> {
                        log("is enum type: $type")
                        // TODO: process enum, get all ENUM_CONSTANT
                        addType(subTypeElement)

                        if(notGenericParameter) {
                            val enumValues: MutableList<String> = mutableListOf()
                            for (enumElement in subTypeElement.enclosedElements) {
                                when (enumElement.kind) {
                                    ElementKind.ENUM_CONSTANT -> {
                                        enumValues += enumElement.simpleName.toString()
                                    }
                                    else -> {
                                    }
                                }
                            }
                            constraints += Constraint("fabric:enum", stringList = enumValues)
                        }
                        FieldType(type.toString())
                    }
                    subTypeElement != null -> {
                        log("is not a list, set or string")

                        // TODO: add to extra types
                        addType(subTypeElement)
                        FieldType(type.toString())
                    }
                    else -> {
                        throw IllegalStateException("unhandled state: $type")
                    }
                }
            }
            is Type.JCPrimitiveType -> {

                log("constValue: ${type.constValue()}")
//                            log("stringValue: ${type.stringValue()}")
                when (type.toString()) {
                    "boolean" -> {
                        log("is boolean")
                    }
                    "byte" -> {
                        log("is byte")
                    }
                    "short" -> {
                        log("is short")
                    }
                    "int" -> {
                        log("is int")
                    }
                    "long" -> {
                        log("is long")
                    }
                    "char" -> {
                        log("is char")
                    }
                    "float" -> {
                        log("is float")
                    }
                    "double" -> {
                        log("is double")
                    }
                    else -> {
                        kotlin.TODO(type.toString())
                    }
                }

                fiber.FieldType(type.toString())
            }
            else -> kotlin.TODO()
        }
        return fieldType
    }

    private fun getStringConstraints(element: Element, typeIndex: Int): List<Constraint> {
        val setValidator = element.getAnnotation(SetValidatorString::class.java)
            ?.takeIf { it.typeIndex.contains(typeIndex) }
            ?.let {
                Constraint("fabric:string_set", stringList = it.values.toList())
            }
        val regexValidator = element.getAnnotation(RegexValidator::class.java)
            ?.takeIf { it.typeIndex.contains(typeIndex) }
            ?.let {
                Constraint("fabric:regex", string = it.regex)
            }
        return listOfNotNull(setValidator, regexValidator)
    }

    private fun assertMatchingAnnotationType(element: Element, typeIndex: Int, type: String) {
        listOf(
            *element.getAnnotationsByType(RangeValidatorInt::class.java).filter { it.typeIndex.contains(typeIndex) }.toTypedArray(),
            *element.getAnnotationsByType(RangeValidatorFloat::class.java).filter { it.typeIndex.contains(typeIndex) }.toTypedArray(),
            *element.getAnnotationsByType(RangeValidatorLong::class.java).filter { it.typeIndex.contains(typeIndex) }.toTypedArray(),
            *element.getAnnotationsByType(RegexValidator::class.java).filter { it.typeIndex.contains(typeIndex) }.toTypedArray(),
            *element.getAnnotationsByType(SetValidatorString::class.java).filter { it.typeIndex.contains(typeIndex) }.toTypedArray()
        ).forEach {
            assertMatchingAnnotationType(it, type)
        }
    }

    fun assertMatchingAnnotationType(annotation: Annotation, type: String) {
        val expectedType = when (annotation) {
            is RangeValidatorInt -> "int"
            is RangeValidatorFloat -> "floar"
            is RangeValidatorLong -> "long"
            is RegexValidator, is SetValidatorString -> "String"
            else -> {
                log("unhandled annotaion: $annotation")
                return
            }
        }

        if (type != expectedType) {
            log("annotation '$annotation' should only be applied to $expectedType, but was applied to $type")
        }
        require(type == expectedType) {
            "annotation '$annotation' should only be applied to $expectedType, but was applied to $type"
        }
    }

    private fun error(msg: String) {
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, msg)
    }

    private fun log(msg: String) {
        processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, msg)
//        println(msg)
//        if (processingEnv.options.containsKey("debug")) {
//            processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, msg)
//        } else {
//            println(msg)
//        }
    }

    // Store current System.out before assigning a new value
    private val console = System.out

    fun openLog() {
        var logFile: FileObject? = null

        var n = 0
        do {
            try {
                logFile = processingEnv.filer.createResource(
                    StandardLocation.CLASS_OUTPUT,
                    "",
                    "annotationProcessor.log.$n.txt"
                )
            } catch (e: IOException) {
                val writer = StringWriter()
                e.printStackTrace(PrintWriter(writer))
                log(writer.toString())
                n++
                continue
            } catch (e: FilerException) {
                val writer = StringWriter()
                e.printStackTrace(PrintWriter(writer))
                log(writer.toString())
                n++
                continue
            }
        } while (logFile == null)

        val o = PrintStream(logFile.openOutputStream())

        // Assign o to output stream
        System.setOut(o)
    }

    fun closeLog() {
        System.setOut(console)
    }

    private fun error(msg: String, element: Element, annotation: AnnotationMirror) {
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, msg, element, annotation)
    }

    private fun fatalError(msg: String) {
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "FATAL ERROR: $msg")
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}