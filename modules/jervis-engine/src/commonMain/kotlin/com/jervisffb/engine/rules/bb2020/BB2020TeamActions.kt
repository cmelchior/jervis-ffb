package com.jervisffb.engine.rules.bb2020

import com.jervisffb.engine.rules.bb2020.procedures.actions.blitz.BlitzAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BlockAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.MultipleBlockAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.ProjectileVomitAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.StabAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.foul.FoulAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.handoff.HandOffAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.MoveAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.pass.PassAction
import com.jervisffb.engine.rules.common.actions.PlayerAction
import com.jervisffb.engine.rules.common.actions.PlayerSpecialActionType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.actions.TeamActions
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.rules.bb2020.procedures.actions.handoff.ThrowTeamMateAction
import kotlinx.serialization.Serializable

/**
 * Define the standard set of actions that are available in the rules.
 * TODO What if these are modified by skills, events, cards or otherwise?
 */
@Serializable
class BB2020TeamActions : TeamActions() {

    private val actions: Map<PlayerStandardActionType, PlayerAction> = mapOf(
        PlayerStandardActionType.MOVE to PlayerAction(
            name = "Move",
            type = PlayerStandardActionType.MOVE,
            countsAs = null,
            availablePrTurn = Int.MAX_VALUE,
            worksDuringBlitz = false,
            procedure = MoveAction,
            compulsory = false,
        ),

        PlayerStandardActionType.PASS to PlayerAction(
            name = "Pass",
            type = PlayerStandardActionType.PASS,
            countsAs = null,
            availablePrTurn = 1,
            worksDuringBlitz = false,
            procedure = PassAction,
            compulsory = false,
        ),

        PlayerStandardActionType.HAND_OFF to PlayerAction(
            name = "Hand-off",
            type = PlayerStandardActionType.HAND_OFF,
            countsAs = null,
            availablePrTurn = 1,
            worksDuringBlitz = false,
            procedure = HandOffAction,
            compulsory = false,
        ),

        PlayerStandardActionType.THROW_TEAM_MATE to PlayerAction(
            name = "Throw Team-mate",
            type = PlayerStandardActionType.THROW_TEAM_MATE,
            countsAs = null,
            availablePrTurn = 1,
            worksDuringBlitz = false,
            procedure = ThrowTeamMateAction,
            compulsory = false,
        ),

        PlayerStandardActionType.BLOCK to PlayerAction(
            name = "Block",
            type = PlayerStandardActionType.BLOCK,
            countsAs = null,
            availablePrTurn = Int.MAX_VALUE,
            worksDuringBlitz = true,
            procedure = BlockAction,
            compulsory = false,
        ),

        PlayerStandardActionType.BLITZ to PlayerAction(
            name = "Blitz",
            type = PlayerStandardActionType.BLITZ,
            countsAs = null,
            availablePrTurn = 1,
            worksDuringBlitz = true,
            procedure = BlitzAction,
            compulsory = false,
        ),

        PlayerStandardActionType.FOUL to PlayerAction(
            name = "Foul",
            type = PlayerStandardActionType.FOUL,
            countsAs = null,
            availablePrTurn = 1,
            worksDuringBlitz = false,
            procedure = FoulAction,
            compulsory = false,
        ),
    )

    override val specialActions: Set<PlayerAction> = buildSet {
        PlayerSpecialActionType.entries.forEach {
            val action = when (it) {
//                PlayerSpecialActionType.BALL_AND_CHAIN -> TODO()
//                PlayerSpecialActionType.BOMBARDIER -> TODO()
//                PlayerSpecialActionType.BREATHE_FIRE -> TODO()
//                PlayerSpecialActionType.CHAINSAW -> TODO()
//                PlayerSpecialActionType.HYPNOTIC_GAZE -> TODO()
//                PlayerSpecialActionType.KICK_TEAM_MATE -> TODO()
                PlayerSpecialActionType.MULTIPLE_BLOCK -> {
                    PlayerAction(
                        name = "Multiple Block",
                        type = PlayerSpecialActionType.MULTIPLE_BLOCK,
                        countsAs = PlayerStandardActionType.BLOCK,
                        availablePrTurn = Int.MAX_VALUE,
                        procedure = MultipleBlockAction,
                        worksDuringBlitz = false,
                        compulsory = false,
                    )
                }
                PlayerSpecialActionType.PROJECTILE_VOMIT -> {
                    PlayerAction(
                        name = "Projectile Vomit",
                        type = PlayerSpecialActionType.PROJECTILE_VOMIT,
                        countsAs = PlayerStandardActionType.BLOCK,
                        availablePrTurn = Int.MAX_VALUE,
                        procedure = ProjectileVomitAction,
                        worksDuringBlitz = true,
                        compulsory = false,
                    )
                }
                PlayerSpecialActionType.STAB -> {
                    PlayerAction(
                        name = "Stab",
                        type = PlayerSpecialActionType.STAB,
                        countsAs = PlayerStandardActionType.BLOCK,
                        availablePrTurn = Int.MAX_VALUE,
                        procedure = StabAction,
                        worksDuringBlitz = true,
                        compulsory = false,
                    )
                }
                else -> null
            }
            if (action != null) add(action)
        }
    }

    override fun get(type: PlayerStandardActionType): PlayerAction {
        return actions[type] ?: INVALID_GAME_STATE("Actions this type are not configured here: $type")
    }
    override fun get(type: PlayerSpecialActionType): PlayerAction {
        return specialActions.firstOrNull { it.type == type } ?: INVALID_GAME_STATE("Actions this type are not configured here: $type")
    }
    override val move: PlayerAction = get(PlayerStandardActionType.MOVE)
    override val pass: PlayerAction = get(PlayerStandardActionType.PASS)
    override val handOff: PlayerAction = get(PlayerStandardActionType.HAND_OFF)
    override val block: PlayerAction = get(PlayerStandardActionType.BLOCK)
    override val blitz: PlayerAction = get(PlayerStandardActionType.BLITZ)
    override val foul: PlayerAction = get(PlayerStandardActionType.FOUL)
}
