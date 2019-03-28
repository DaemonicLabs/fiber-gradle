package fiber

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

//@Serializable(with = Constraint.Companion::class)
@Serializable
data class Constraint(
    val identifier: String,
    @Optional val string: String? = null,
    @Optional val long: Long? = null,
    @Optional val double: Double? = null,
    @Optional val stringList: List<String>? = null
) {
//    @Serializer(forClass = Constraint::class)
//    companion object : KSerializer<Constraint> {
//        override fun deserialize(decoder: Decoder): Constraint {
//            val composite = decoder.beginStructure(descriptor)
//            val id = composite.decodeStringElement(descriptor, 0)
//            val value = when(id) {
//                "fabric:min", "fabric.max" -> {
//                    composite.decodeLongElement(descriptor, 1)
//                }
//                "fabric:enum" -> {
//                    composite.decodeSerializableElement(
//                        descriptor,
//                        1,
//                        String.serializer().list
//                    )
//                }
//                "fabric:regex" -> {
//                    composite.decodeStringElement(
//                        descriptor,
//                        1
//                    )
//                }
//                else -> "unknown type: $id"//throw IllegalStateException("unhandled identifier: $id")
//            }
//            composite.endStructure(descriptor)
//            return Constraint(id, value)
//        }
//
//        override fun serialize(encoder: Encoder, obj: Constraint) {
//            val composite = encoder.beginStructure(descriptor)
//            composite.encodeStringElement(descriptor, 0, obj.identifier)
//
//            when (obj.identifier) {
//                "fabric:min", "fabric.max" -> {
//                    when(val number = obj.value) {
//                        is Int -> composite.encodeIntElement(descriptor, 1, number)
//                        is Long -> composite.encodeLongElement(descriptor, 1, number)
//                        is Float -> composite.encodeFloatElement(descriptor, 1, number)
//                        is Double -> composite.encodeDoubleElement(descriptor, 1, number)
//                        else -> throw IllegalStateException("${obj.identifier} cannot use type ${obj.value::class}")
//                    }
//                }
//                "fabric:enum" -> {
//                    val list = obj.value as? List<String> ?: throw IllegalStateException("${obj.identifier} cannot use type ${obj.value::class}")
//                    composite.encodeSerializableElement(
//                        descriptor,
//                        1,
//                        String.serializer().list,
//                        list
//                    )
//                }
//                "fabric:regex" -> {
//                    val string = obj.value as? String ?: throw IllegalStateException("${obj.identifier} cannot use type ${obj.value::class}")
//                    composite.encodeStringElement(
//                        descriptor,
//                        1,
//                        string
//                    )
//                }
//                else -> composite.encodeNonSerializableElement(descriptor, 1, obj.value)
//            }
//            composite.endStructure(descriptor)
//        }
//    }
}