package dk.ilios.jervis.model.inducements

enum class AssistantCoachType {
    STANDARD,
    PART_TIME
}
data class AssistantCoach(val type: AssistantCoachType)
