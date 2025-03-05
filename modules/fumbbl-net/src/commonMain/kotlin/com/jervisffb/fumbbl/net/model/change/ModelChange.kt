@file:UseContextualSerialization(
    LocalDateTime::class,
)

package com.jervisffb.fumbbl.net.model.change

import com.jervisffb.engine.model.PlayerId
import com.jervisffb.fumbbl.net.model.ModelChangeId
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlin.jvm.JvmInline

fun ModelChange.isHomeData(): Boolean {
    return key == "home"
}

@Serializable
@JvmInline
value class PlayerId(val id: String) {
    fun toJervisId(): PlayerId {
        return PlayerId(id)
    }
}

@Serializable
@JvmInline
value class TeamId(val id: String)

@Serializable
@JsonClassDiscriminator("modelChangeId")
sealed interface ModelChange {
    val id: ModelChangeId
    val key: Any? // Normally String
    val value: Any?
}
