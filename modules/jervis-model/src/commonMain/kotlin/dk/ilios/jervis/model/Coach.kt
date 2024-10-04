package dk.ilios.jervis.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class CoachId(val id: String)

@Serializable
data class Coach(val id: CoachId, val name: String)
