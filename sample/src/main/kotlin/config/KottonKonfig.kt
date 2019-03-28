package config

import fiber.annotations.Comment
import fiber.annotations.ConfigFile
import fiber.annotations.RangeValidatorInt
import fiber.annotations.RegexValidator

@ConfigFile(name = "KottonConfig")
public data class KottonKonfig(
    @RangeValidatorInt(min = 0)
    var number1: Short = 8,
    @RangeValidatorInt(min = 0)
    var number2: Int = 9,
    @RegexValidator(regex = "a+")
    var number3: Long = 10,
    var character: Char = 'A',
    var state: State = State.ONE,

    @RegexValidator(regex = "a+")
    val string: String = "aaaabbbb",

    @Comment(value = "A list of mod ids, in order of preference for resource loading.")
    var namespacePreferenceOrder: MutableList<String> = mutableListOf(),
    var someStateList: MutableList<State> = mutableListOf(),

    var someIntSet: MutableSet<Int> = mutableSetOf(1, 5, 2),
    var someStringSet: MutableSet<String> = mutableSetOf("one", "two", "three"),

    var arrays: ArrayConfig = ArrayConfig(),

    @Comment("nested config")
    var nested: NestedConfig = NestedConfig(),

//    @Comment("nested inner")
//    val nestedInner: NestedInner = NestedInner(),

    val nestedGenerics: MutableList<Map<String, ByteArray>> = mutableListOf()
) {
//    data class NestedInner(
//        val innerData: String = "defaultInner"
//    )
}

data class ArrayConfig(
    var byteObjectArray: Array<Byte> = arrayOf(0, 1, 2),
    var byteArray: ByteArray = byteObjectArray.toByteArray(),
    var byteList: List<Byte> = byteArray.toList(),

    var shortObjectArray: Array<Short> = arrayOf(Short.MIN_VALUE, Short.MAX_VALUE),
    var shortArray: ShortArray = shortObjectArray.toShortArray(),
    var shortList: List<Short> = shortArray.toList(),

    var intObjectArray: Array<Int> = arrayOf(0, 3, 4),
    var intArray: IntArray = intObjectArray.toIntArray(),

    var longObjectArray: Array<Long> = arrayOf(0, 3, 4),
    var longArray: LongArray = longObjectArray.toLongArray(),

    var floatObjectArray: Array<Float> = arrayOf(0.0f, 3.1f, 4.4f),
    var floatArray: FloatArray = floatObjectArray.toFloatArray(),

    var doubleObjectArray: Array<Double> = arrayOf(0.0, 3.1, 4.4),
    var doubleArray: DoubleArray = doubleObjectArray.toDoubleArray(),

    var charObjectArray: Array<Char> = arrayOf('a', 'b', 'c'),
    var charArray: CharArray = charObjectArray.toCharArray(),

    var stringArray:Array<String> = arrayOf("one", "two", "three"),

    var enumArray: Array<State> = arrayOf(State.ONE, State.TWO, State.THREE, State.THREE)
)


data class NestedConfig(
    val data: String = "defaultdata"
)

enum class State(val number: Int) {
    ONE(1), TWO(2), THREE(3)
}
