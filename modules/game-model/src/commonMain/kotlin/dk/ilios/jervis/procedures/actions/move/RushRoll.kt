package dk.ilios.jervis.procedures.actions.move

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.NoRerollSelected
import dk.ilios.jervis.actions.RerollOptionSelected
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.actions.SelectNoReroll
import dk.ilios.jervis.actions.SelectRerollOption
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.SetOldContext
import dk.ilios.jervis.commands.SetPlayerRushesLeft
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.context.UseRerollContext
import dk.ilios.jervis.model.modifiers.DiceModifier
import dk.ilios.jervis.procedures.D6DieRoll
import dk.ilios.jervis.reports.ReportDiceRoll
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.utils.INVALID_ACTION
import dk.ilios.jervis.utils.INVALID_GAME_STATE
import dk.ilios.jervis.utils.calculateAvailableRerollsFor
import dk.ilios.jervis.utils.sum

/**
 * Handle a player rushing a single square.
 * See page 44 in the rulebook.
 *
 * Designer's Commentary:
 * If two rushes are necessary for a Jump/Leap and the first roll is a failure,
 * the player will be knocked down in the starting square, rather than
 * in the ending square.
 *
 * It more than one rush is required, it is up to the caller of this procedure
 * to do so. And also handle each roll result.
 */
 object RushRoll: Procedure() {
    override val initialNode: Node = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        if (state.rushRollContext == null) {
            INVALID_GAME_STATE("Missing rush context")
        }
        return null
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command? {
        val context = state.rushRollContext!!
        return SetPlayerRushesLeft(context.player, context.player.rushesLeft - 1)
    }

    object RollDie: ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result>(action) { d6 ->
                val context = state.rushRollContext!!
                val success = isRushSuccess(d6, context.rollModifiers)
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.RUSH, d6),
                    SetOldContext(Game::rushRollContext, context.copy(
                        roll = D6DieRoll(d6),
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
        override fun getAvailableActions(
            state: Game,
            rules: Rules,
        ): List<ActionDescriptor> {
            val context = state.rushRollContext!!
            val rushingPlayer = context.player
            val availableReRolls: List<SelectRerollOption> = calculateAvailableRerollsFor(
                rules,
                rushingPlayer,
                DiceRollType.RUSH,
                context.roll!!,
                context.isSuccess
            )
            return if (availableReRolls.isEmpty()) {
                listOf(ContinueWhenReady)
            } else {
                listOf(SelectNoReroll) + availableReRolls
            }
        }

        override fun applyAction(
            action: GameAction,
            state: Game,
            rules: Rules,
        ): Command {
            return when (action) {
                Continue -> ExitProcedure()
                NoRerollSelected -> ExitProcedure()
                is RerollOptionSelected -> {
                    val rerollContext = UseRerollContext(DiceRollType.RUSH, action.option.source)
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
        override fun getChildProcedure(
            state: Game,
            rules: Rules,
        ): Procedure = state.rerollContext!!.source.rerollProcedure

        override fun onExitNode(
            state: Game,
            rules: Rules,
        ): Command {
            val context = state.rerollContext!!
            return if (context.rerollAllowed) {
                GotoNode(ReRollDie)
            } else {
                ExitProcedure()
            }
        }
    }

    object ReRollDie : ActionNode() {
        override fun getAvailableActions(
            state: Game,
            rules: Rules,
        ): List<ActionDescriptor> = listOf(RollDice(Dice.D6))

        override fun applyAction(
            action: GameAction,
            state: Game,
            rules: Rules,
        ): Command {
            return checkDiceRoll<D6Result>(action) { d6 ->
                val rushContext = state.rushRollContext!!
                val rerollContext = state.rerollContext!!
                val rerollResult = rushContext.copy(
                    roll = rushContext.roll!!.copy(
                        rerollSource = rerollContext.source,
                        rerolledResult = d6,
                    ),
                    isSuccess = isRushSuccess(d6, rushContext.rollModifiers),
                )
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.RUSH, d6),
                    SetOldContext(Game::rushRollContext, rerollResult),
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
