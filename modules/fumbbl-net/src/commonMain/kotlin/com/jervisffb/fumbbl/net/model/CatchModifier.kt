package com.jervisffb.fumbbl.net.model

import com.jervisffb.fumbbl.net.api.serialization.FumbblEnum
import com.jervisffb.fumbbl.net.api.serialization.FumbblEnumSerializer
import kotlinx.serialization.Serializable

class CatchModifierSerializer : FumbblEnumSerializer<CatchModifier>(CatchModifier::class)

val foo = mapOf(
    "Inaccurate Pass, Deviated Ball or Scatter" to CatchModifier.INACCURATE,
)




@Serializable(with = CatchModifierSerializer::class)
enum class CatchModifier(override val id: String, val modifier: Int) : FumbblEnum {
    INACCURATE("Inaccurate Pass, Deviated Ball or Scatter", -1),
    DEFLECTED_PASS("Deflected Pass", -1),
    BLAST_IT("Blast It!", -1),
    NERVES_OF_STEEL("Nerves of Steel", 0),
    TACKLEZONES_1("1 Tacklezone", -1),
    TACKLEZONES_2("2 Tacklezones", -2),
    TACKLEZONES_3("3 Tacklezones", -3),
    TACKLEZONES_4("4 Tacklezones", -4),
    TACKLEZONES_5("5 Tacklezones", -5),
    TACKLEZONES_6("6 Tacklezones", -6),
    TACKLEZONES_7("7 Tacklezones", -7),
    TACKLEZONES_8("8 Tacklezones", -8),
    DISTURBING_PRESENCE_1("1 Disturbing Presence", -1),
    DISTURBING_PRESENCE_2("2 Disturbing Presences", -2),
    DISTURBING_PRESENCE_3("3 Disturbing Presences", -3),
    DISTURBING_PRESENCE_4("4 Disturbing Presences", -4),
    DISTURBING_PRESENCE_5("5 Disturbing Presences", -5),
    DISTURBING_PRESENCE_6("6 Disturbing Presences", -6),
    DISTURBING_PRESENCE_7("7 Disturbing Presences", -7),
    DISTURBING_PRESENCE_8("8 Disturbing Presences", -8),
    DISTURBING_PRESENCE_9("9 Disturbing Presences", -9),
    DISTURBING_PRESENCE_10("10 Disturbing Presences", -10),
    DISTURBING_PRESENCE_11("11 Disturbing Presences", -11),
    POURING_RAIN("Pouring Rain", -1),
    HAND_OFF("Hand Off", -1),
    ACCURATE_PASS("Accurate Pass", -1),
    ;
}
