package com.jervisffb.engine.model.inducements

enum class CheerleaderType {
    STANDARD,
    TEMP_AGENCY
}

data class Cheerleader(val type: CheerleaderType)

