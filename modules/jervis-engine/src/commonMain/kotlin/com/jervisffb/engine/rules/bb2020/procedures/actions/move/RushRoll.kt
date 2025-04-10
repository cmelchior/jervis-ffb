package com.jervisffb.engine.rules.bb2020.procedures.actions.move

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectNoReroll
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.SetOldContext
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.RushRollContext
import com.jervisffb.engine.model.context.UseRerollContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.model.modifiers.RushModifier
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.D6DieRoll
import com.jervisffb.engine.rules.bb2020.tables.PrayerToNuffle
import com.jervisffb.engine.rules.bb2020.tables.Weather
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.calculateAvailableRerollsFor
import com.jervisffb.engine.utils.sum

/**
 * Handle a player rushing a single square.
 * See page 44 in the rulebook.
 *
 * This procedure is only responsible for the actual dice roll. The result
 * must be handled by the procedure calling this one. This also includes
 * modifying the number of rushes left.
 *
 * Designer's Commentary:
 * If two rushes are necessary for a Jump/Leap and the first roll is a failure,
 * the player will be knocked down in the starting square, rather than
 * in the ending square.
 *
 * Developer's Commentary:
 * If more than one rush is required, it is up to the caller of this procedure
 * to do so. And also handle each roll result.
 *
 * Skills are optional to use, so technically you would choose to use Sprint
 * as part of doing a Rush, but since Rushing is also optional, we opt for the
 * easier implementation and check for Sprint when starting an action and add
 * either 2 or 3 allowed rushes there.
 *
 * Also, an observation about Rushing. It is worded this way in the rules:
 *
 * "Whenever a player performs an action that includes movement.."
 *
 * This means that if, by any means, a player is able to do two actions.
 * They would be able to move 2*Rush distance. Sprint has a similar wording
 * that would allow it to be used in both actions as well.
 *
 * @see [StandardMoveStep.ResolveRush]
 */
object RushRoll: Procedure() {
    override val initialNode: Node = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        // Check for Rush modifiers
        val modifiers = mutableListOf<DiceModifier>()

        // Blizzard (Weather)
        if (state.weather == Weather.BLIZZARD) {
            modifiers.add(RushModifier.BLIZZARD)
        }
        // Moles under the Pitch (Prayers to Nuffle)
        if (state.homeTeam.activePrayersToNuffle.contains(PrayerToNuffle.MOLES_UNDER_THE_PITCH)) {
            modifiers.add(RushModifier.MOLES_UNDER_THE_PITCH_HOME)
        }
        if (state.awayTeam.activePrayersToNuffle.contains(PrayerToNuffle.MOLES_UNDER_THE_PITCH)) {
            modifiers.add(RushModifier.MOLES_UNDER_THE_PITCH_AWAY)
        }

        return if (modifiers.isNotEmpty()) {
            SetContext(state.getContext<RushRollContext>().copyAndAddModifier(*modifiers.toTypedArray()))
        } else {
            null
        }
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<RushRollContext>()

    object RollDie: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<RushRollContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result>(action) { d6 ->
                val context = state.getContext<RushRollContext>()
                val success = isRushSuccess(d6, context.modifiers)
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.RUSH, d6),
                    SetContext(context.copy(
                        roll = D6DieRoll.create(state, d6),
                        isSuccess = success,
                    )),
                    GotoNode(ChooseReRollSource)
                )
            }
        }
    }

    /**
     * Choose where a reroll should come from. This can be skills, team rerolls, special cards
     * or other sources.
     */
    object ChooseReRollSource : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<RushRollContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<RushRollContext>()
            val rushingPlayer = context.player
            val availableReRolls: SelectRerollOption? = calculateAvailableRerollsFor(
                rules,
                rushingPlayer,
                DiceRollType.RUSH,
                context.roll!!,
                context.isSuccess
            )
            return if (availableReRolls == null) {
                listOf(ContinueWhenReady)
            } else {
                listOf(SelectNoReroll(context.isSuccess)) + availableReRolls
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Continue -> ExitProcedure()
                is NoRerollSelected -> ExitProcedure()
                is RerollOptionSelected -> {
                    val rerollContext = UseRerollContext(DiceRollType.RUSH, action.getRerollSource(state))
                    compositeCommandOf(
                        SetOldContext(Game::rerollContext, rerollContext),
                        GotoNode(UseRerollSource),
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    /**
     * Use the selected reroll source.
     */
    object UseRerollSource : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return state.rerollContext!!.source.rerollProcedure
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.rerollContext!!
            return if (context.rerollAllowed) {
                GotoNode(ReRollDie)
            } else {
                ExitProcedure()
            }
        }
    }

    object ReRollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<RushRollContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> = listOf(RollDice(Dice.D6))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result>(action) { d6 ->
                val rushContext = state.getContext<RushRollContext>()
                val rerollContext = state.rerollContext!!
                val rerollResult = rushContext.copy(
                    roll = rushContext.roll!!.copyReroll(
                        rerollSource = rerollContext.source,
                        rerolledResult = d6,
                    ),
                    isSuccess = isRushSuccess(d6, rushContext.modifiers),
                )
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.RUSH, d6),
                    SetContext(rerollResult),
                    ExitProcedure(),
                )
            }
        }
    }

    private fun isRushSuccess(d6: D6Result, modifiers: List<DiceModifier>): Boolean {
        val target = 2
        return d6.value + modifiers.sum() >= target
    }
}
