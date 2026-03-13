package com.jervisffb.engine.rules.bb2025.procedures.actions.move

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectNoReroll
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetOldContext
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.LeapRollContext
import com.jervisffb.engine.model.context.UseRerollContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.model.modifiers.LeapModifier
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportRerollUsed
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.testAgainstAgility
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.calculateAvailableRerollsFor

/**
 * Procedure for handling a Leap Roll as described on page 130 in the BB2025
 * rulebook. It is only responsible for handling the actual dice roll. The
 * result is stored in [LeapRollContext] and it is up to the caller of the
 * procedure to choose the appropriate action depending on the outcome.
 */
object LeapRoll : Procedure() {
    override val initialNode: Node = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<LeapRollContext>()

    object RollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules) = state.getContext<LeapRollContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> = listOf(RollDice(Dice.D6))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D6Result>(action) { d6 ->
                val rollContext = state.getContext<LeapRollContext>()
                val resultContext = rollContext.copy(
                    roll = D6DieRoll.create(state, d6),
                    isSuccess = isSuccess(rollContext, overrideD6 = d6)
                )
                return compositeCommandOf(
                    ReportDiceRoll(DiceRollType.LEAP, d6),
                    UpdateContext(resultContext),
                    GotoNode(ChooseReRollSource),
                )
            }
        }
    }

    object ChooseReRollSource : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules) = state.getContext<LeapRollContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<LeapRollContext>()
            val availableRerolls = calculateAvailableRerollsFor(
                rules,
                context.player,
                DiceRollType.LEAP,
                context.roll!!,
                context.isSuccess
            )
            return if (availableRerolls == null) {
                listOf(ContinueWhenReady)
            } else {
                listOf(SelectNoReroll(context.isSuccess)) + availableRerolls
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Continue,
                is NoRerollSelected -> GotoNode(ChooseToUseDivingTackleAfterReRoll)
                is RerollOptionSelected -> {
                    val rerollContext = UseRerollContext(DiceRollType.LEAP, action.getRerollSource(state))
                    compositeCommandOf(
                        SetOldContext(Game::rerollContext, rerollContext),
                        ReportRerollUsed(action.getRerollSource(state)),
                        GotoNode(UseRerollSource),
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object UseRerollSource : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return state.rerollContext!!.source.rerollProcedure
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.rerollContext!!
            return if (context.rerollAllowed) {
                GotoNode(ReRollDie)
            } else {
                GotoNode(ChooseToUseDivingTackleAfterReRoll)
            }
        }
    }

    object ReRollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules) = state.getContext<LeapRollContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> = listOf(RollDice(Dice.D6))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D6Result>(action) { d6 ->
                val rollContext = state.getContext<LeapRollContext>()
                val rerollResult = rollContext.copy(
                    roll = rollContext.roll!!.copyReroll(
                        rerollSource = state.rerollContext!!.source,
                        rerolledResult = d6,
                    ),
                    isSuccess = isSuccess(rollContext, overrideD6 = d6)
                )
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.LEAP, d6),
                    UpdateContext(rerollResult),
                    GotoNode(ChooseToUseDivingTackleAfterReRoll),
                )
            }
        }
    }

    object ChooseToUseDivingTackleAfterReRoll: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<LeapRollContext>().player.team.otherTeam()
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<LeapRollContext>()
            val eligiblePlayers = context.startingSquare.getSurroundingCoordinates(rules)
                .filter { coord ->
                    state.field[coord].player?.let { player ->
                        player.team != context.player.team
                    } ?: false
                }
                .mapNotNull { state.field[it].player }
                .filter { it.isSkillAvailable(SkillType.DIVING_TACKLE) }

            return if (eligiblePlayers.isNotEmpty()) {
                listOf(SelectPlayer.fromPlayers(eligiblePlayers), CancelWhenReady)
            } else {
                listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<LeapRollContext>()
            return when (action) {
                is PlayerSelected -> {
                    val player = action.getPlayer(state)
                    val skill = player.getSkill(SkillType.DIVING_TACKLE)
                    val updatedModifiers = context.modifiers.add(LeapModifier.DIVING_TACKLE)
                    val success = isSuccess(context, overrideModifiers = updatedModifiers)
                    compositeCommandOf(
                        ReportSkillUsed(player, skill),
                        UpdateContext(context.copy(
                            modifiers = updatedModifiers,
                            isSuccess = success
                        )),
                        SetPlayerState(player, PlayerState.PRONE, hasTackleZones = false),
                        SetPlayerLocation(player, context.startingSquare),
                        ExitProcedure()
                    )
                }
                Cancel,
                Continue -> ExitProcedure()
                else -> INVALID_ACTION(action)
            }
        }
    }

    // -- HELPER METHODS --

    private fun isSuccess(
        context: LeapRollContext,
        overrideD6: D6Result? = null,
        overrideModifiers: List<DiceModifier>? = null
    ): Boolean {
        val player = context.player
        val d6 = overrideD6 ?: context.roll!!.result
        val modifiers = overrideModifiers ?: context.modifiers
        return testAgainstAgility(player, d6, modifiers)
    }
}
