package dk.ilios.jervis.model.inducements

enum class CheerleaderType {
    STANDARD,
    TEMP_AGENCY
}

data class Cheerleader(val type: CheerleaderType)

