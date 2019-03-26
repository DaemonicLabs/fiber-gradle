package fiber

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class FieldType(
    val className: String,
    @Optional val generics: List<FieldType> = listOf()
)