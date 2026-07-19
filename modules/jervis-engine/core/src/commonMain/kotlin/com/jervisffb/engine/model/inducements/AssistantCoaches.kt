package com.jervisffb.engine.model.inducements

enum class AssistantCoachType {
    STANDARD,
    PART_TIME
}
data class AssistantCoach(val type: AssistantCoachType)
