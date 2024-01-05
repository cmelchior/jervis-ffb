package dk.ilios.jervis.model

import kotlin.jvm.JvmInline

@JvmInline
value class CoachId(val id: String)

data class Coach(val id: CoachId, val name: String)
