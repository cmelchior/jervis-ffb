package dk.ilios.jervis.model

import dk.ilios.jervis.rules.roster.Position
import dk.ilios.jervis.rules.roster.bb2020.HumanTeam
import dk.ilios.jervis.rules.skills.Skill
import kotlin.jvm.JvmInline

@JvmInline
value class PlayerNo(val number: Int)

class Player {
    var name: String = ""
    var number: PlayerNo = PlayerNo(0)
    var position: Position = HumanTeam.positions.first()
    var baseMove: Int = 0
    var baseStrenght: Int = 0
    var baseAgility: Int = 0
    var basePassing: Int = 0
    var baseArmorValue: Int = 0
    val skills = mutableListOf<Skill>()
    val move: Int
        get() {
            return baseMove
        }
    val strength: Int
        get() {
            return baseStrenght
        }
    val agility: Int
        get() {
            return baseAgility
        }
    val passing: Int
        get() {
            return basePassing
        }
    val armorValue: Int
        get() {
            return baseArmorValue
        }
}