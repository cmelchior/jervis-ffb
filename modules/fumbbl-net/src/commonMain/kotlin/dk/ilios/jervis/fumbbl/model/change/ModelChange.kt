@file:UseContextualSerialization(
    LocalDateTime::class
)
package dk.ilios.jervis.fumbbl.model.change

import dk.ilios.jervis.fumbbl.model.ModelChangeId
import dk.ilios.jervis.fumbbl.model.PlayerAction
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseContextualSerialization
import kotlinx.serialization.json.JsonClassDiscriminator
import java.time.LocalDateTime

@Serializable
@JvmInline
value class PlayerId(val id: String)

@Serializable
@JsonClassDiscriminator("modelChangeId")
sealed interface ModelChange {
    val modelChangeId: ModelChangeId
    val modelChangeKey: Any? // Normally String
    val modelChangeValue: Any?
}
