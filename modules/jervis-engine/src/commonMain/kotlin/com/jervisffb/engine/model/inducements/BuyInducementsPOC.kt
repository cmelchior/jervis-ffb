package com.jervisffb.engine.model.inducements

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team

// This fiile currently just contain snippets of code trying while I am trying
// to figure out how to model buying inducements

/**
 * See page 89 in the rulebook
 */
enum class InducementType {
    // Standard game
    TEMP_AGENCY_CHEERLEADER,
    PART_TIME_ASSISTANT_COACH,
    WEATHER_MAGE,
    BLOODWEISER_KEG,
    SPECIAL_PLAY,
    EXTRA_TEAM_TRAINING,
    BRIBE,
    WANDERING_APOTHECARY,
    MORTUARY_ASSISTANT,
    PLAGUE_DOCTOR,
    RIOTOUS_ROOKIE,
    HALFING_MASTER_CHEF,
    MERCENARY,
    INFAMOUS_COACHING_STAFF,
    WIZARD,
    BIASED_REFEREE
}

sealed interface Inducement {
    val name: String
    val max: Int
}

sealed interface SingleInducement: Inducement {
    fun isAvailable(team: Team): Boolean
    fun getPrice(team: Team): Int
}

sealed interface InducementCollection: Inducement {
    override val name: String
    override val max: Int
    fun getChoices(team: Team): List<StaffInducement>
}

// Describes an inducement that is a "staff" member, i.e., a person with
// some special effect.
sealed interface StaffInducement {
    val name: String
    val named: Boolean // Is "named" in the context of the rules, i.e. has special restrictions in League Play
    fun getPrice(): Int
    fun getPlayer(): Player?
}

class TempAgencyCheerLeader(
    override val name: String = "Temp Agency Cheerleaders",
    override val max: Int = 4,
): Inducement {
    fun isAvailable(team: Team): Boolean = true
    fun getPrice(team: Team): Int = 20_000
}

//class InfamousCoachingStaff(
//    override val name: String,
//    override val max: Int
//): InducementCollection {
//    override fun getChoices(team: Team): List<StaffInducement> {
//        TODO("Not yet implemented")
//    }
//}


class TeamInducement {

}




/**
 * Track all inducements allocated to this team.
 * Note, this just tracks whether or not it has been bought.
 * Any single inducement might add modifiers, players and skills.
 * These are added during the Buy Inducement step and any procedure
 * that is affected by them will need to check for it.
 */
class TeamInducements {
    fun hasInducement(type: InducementType): Boolean {
        TODO()
    }
//    fun addInducement()
//    fun removeInducement(inducement: Inducement)
}

