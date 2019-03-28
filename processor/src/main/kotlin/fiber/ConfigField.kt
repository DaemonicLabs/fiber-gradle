package fiber

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class ConfigField(
    val name: String,
    val type: FieldType,
    val value: String,
    @Optional val comment: String? = null,
    @Optional val constraints: List<Constraint> = listOf()
)