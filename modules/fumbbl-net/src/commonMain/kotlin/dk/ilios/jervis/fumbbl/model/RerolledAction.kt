package dk.ilios.jervis.fumbbl.model

import dk.ilios.jervis.fumbbl.net.serialization.FumbblEnum
import dk.ilios.jervis.fumbbl.net.serialization.FumbblEnumSerializer
import kotlinx.serialization.Serializable

class ReRolledActionSerializer : FumbblEnumSerializer<ReRolledAction>(ReRolledAction::class)

@Serializable(with = ReRolledActionSerializer::class)
enum class ReRolledAction(override val id: String) : FumbblEnum {
    GO_FOR_IT("Go For It"),
    RUSH("Rush"),
    DODGE("Dodge"),
    CATCH("Catch"),
    PICK_UP("Pick Up"),
    PASS("Pass"),
    DAUNTLESS("Dauntless"),
    JUMP("Jump"),
    FOUL_APPEARANCE("Foul Appearance"),
    BLOCK("Block"),
    REALLY_STUPID("Really Stupid"),
    BONE_HEAD("Bone Head"),
    BONEHEAD("Bone-Head"),
    WILD_ANIMAL("Wild Animal"),
    ANIMAL_SAVAGERY("Animal Savagery"),
    TAKE_ROOT("Take Root"),
    WINNINGS("Winnings"),
    ALWAYS_HUNGRY("Always Hungry"),
    THROW_TEAM_MATE("Throw Team-Mate"),
    KICK_TEAM_MATE("Kick Team-Mate"),
    RIGHT_STUFF("Right Stuff"),
    SHADOWING("Shadowing"),
    SHADOWING_ESCAPE("Shadowing Escape"),
    TENTACLES("Tentacles"),
    TENTACLES_ESCAPE("Tentacles Escape"),
    ESCAPE("Escape"),
    SAFE_THROW("Safe Throw"),
    INTERCEPTION("Interception"),
    JUMP_UP("Jump Up"),
    STAND_UP("standUp"),
    CHAINSAW("Chainsaw"),
    BLOOD_LUST("Blood Lust"),
    HYPNOTIC_GAZE("Hypnotic Gaze"),
    ANIMOSITY("Animosity"),
    UNCHANNELED_FURY("Unchanneled Fury"),
    PROJECTILE_VOMIT("Projectile Vomit"),
    TRAP_DOOR("Trapdoor"),
    ARGUE_THE_CALL("Argue the Call"),
    OLD_PRO("Old Pro"),
    THROW_KEG("Throw Keg"),
    DIRECTION("Direction"),
    LOOK_INTO_MY_EYES("Look Into My Eyes"),
    BALEFUL_HEX("Baleful Hex"),
    SINGLE_DIE("Single Die"),
    ALL_YOU_CAN_EAT("All You Can Eat"),
}
