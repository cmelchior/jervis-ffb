package com.jervisffb.engine.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Coach(val id: CoachId, val name: String)
