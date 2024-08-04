@file:UseContextualSerialization(
    LocalDateTime::class,
)

package dk.ilios.jervis.fumbbl.model.change

import dk.ilios.jervis.fumbbl.model.ModelChangeId
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import kotlinx.serialization.json.JsonClassDiscriminator
import java.time.LocalDateTime

fun ModelChange.isHomeData(): Boolean {
    return key == "home"
}

@Serializable
@JvmInline
value class PlayerId(val id: String)

@Serializable
@JsonClassDiscriminator("modelChangeId")
sealed interface ModelChange {
    val id: ModelChangeId
    val key: Any? // Normally String
    val value: Any?
}
