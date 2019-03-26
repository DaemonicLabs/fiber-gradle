package fiber

import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.list
import kotlinx.serialization.serializer

@Serializable(with = Constraint.Companion::class)
data class Constraint(
    val identifier: String,
    val value: Any
) {
    @Serializer(forClass = Constraint::class)
    companion object : KSerializer<Constraint> {
//        override fun deserialize(decoder: Decoder): Constraint {
//
//        }

        override fun serialize(encoder: Encoder, obj: Constraint) {
            val composite = encoder.beginStructure(descriptor)
            composite.encodeStringElement(descriptor, 0, obj.identifier)

            when (obj.identifier) {
                "fabric:min", "fabric.max" -> {
                    when(val number = obj.value) {
                        is Int -> composite.encodeIntElement(descriptor, 1, number)
                        is Long -> composite.encodeLongElement(descriptor, 1, number)
                        is Float -> composite.encodeFloatElement(descriptor, 1, number)
                        is Double -> composite.encodeDoubleElement(descriptor, 1, number)
                        else -> throw IllegalStateException("${obj.identifier} cannot use type ${obj.value::class}")
                    }
                }
                "fabric:enum" -> {
                    val list = obj.value as? List<String> ?: throw IllegalStateException("${obj.identifier} cannot use type ${obj.value::class}")
                    composite.encodeSerializableElement(
                        descriptor,
                        1,
                        String.serializer().list,
                        list
                    )
                }
                "fabric:regex" -> {
                    val string = obj.value as? String ?: throw IllegalStateException("${obj.identifier} cannot use type ${obj.value::class}")
                    composite.encodeStringElement(
                        descriptor,
                        1,
                        string
                    )
                }
                else -> composite.encodeNonSerializableElement(descriptor, 1, obj.value)
            }
            composite.endStructure(descriptor)
        }
    }
}