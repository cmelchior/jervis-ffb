package dk.ilios.jervis.procedures

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
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetOldContext
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.context.PickupRollContext
import dk.ilios.jervis.model.context.UseRerollContext
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.model.modifiers.DiceModifier
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRerollOption
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.rules.skills.RerollSource
import dk.ilios.jervis.utils.INVALID_ACTION
import dk.ilios.jervis.utils.sum

/**
 * Procedure for handling a Pickup Roll as described on page 46 in the rulebook.
 * It is only responsible for handling the actual dice roll. The result is stored
 * in [PickupRollContext]] and it is up to the caller of the procedure to
 * choose the appropriate action depending on the outcome.
 */
object PickupRoll : Procedure() {
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<PickupRollContext>()
    }

    override val initialNode: Node = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object RollDie : ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result>(action) {
                val context = state.getContext<PickupRollContext>()
                val updatedContext = context.copy(
                    roll = D6DieRoll(originalRoll = it),
                    isSuccess = isPickupSuccess(it, context.player.agility, context.modifiers),
                )
                return compositeCommandOf(
                    SetContext(updatedContext),
                    GotoNode(ChooseReRollSource),
                )
            }
        }
    }

    // Team Reroll, Pro, Catch (only if failed), other skills
    object ChooseReRollSource : ActionNode() {
        override fun getAvailableActions(
            state: Game,
            rules: Rules,
        ): List<ActionDescriptor> {
            val context = state.getContext<PickupRollContext>()
            val successOnFirstRoll = context.isSuccess
            val pickupPlayer = context.player
            val availableSkills: List<SelectRerollOption> =
                pickupPlayer.skills.asSequence()
                    .filter { it is RerollSource }
                    .map { it as RerollSource }
                    .filter { skill -> skill.canReroll(DiceRollType.PICKUP, listOf(context.roll!!), successOnFirstRoll) }
                    .flatMap { it: RerollSource ->
                        it.calculateRerollOptions(DiceRollType.PICKUP, context.roll!!, successOnFirstRoll)
                    }
                    .map { SelectRerollOption(it) }
                    .toList()

            val team = pickupPlayer.team
            val hasTeamRerolls = team.availableRerollCount > 0
            val allowedToUseTeamReroll =
                when (team.usedTeamRerollThisTurn) {
                    true -> rules.allowMultipleTeamRerollsPrTurn
                    false -> true
                }

            return if (availableSkills.isEmpty() && (!hasTeamRerolls || !allowedToUseTeamReroll)) {
                listOf(ContinueWhenReady)
            } else {
                val teamReroll =
                    if (hasTeamRerolls && allowedToUseTeamReroll) {
                        listOf(SelectRerollOption(DiceRerollOption(team.availableRerolls.last(), listOf(context.roll!!))))
                    } else {
                        emptyList()
                    }
                listOf(SelectNoReroll(context.isSuccess)) + availableSkills + teamReroll
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
                    val rerollContext = UseRerollContext(DiceRollType.CATCH, action.option.source)
                    compositeCommandOf(
                        SetOldContext(Game::rerollContext, rerollContext),
                        GotoNode(UseRerollSource),
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object UseRerollSource : ParentNode() {
        override fun getChildProcedure(
            state: Game,
            rules: Rules,
        ): Procedure = state.rerollContext!!.source.rerollProcedure

        override fun onExitNode(
            state: Game,
            rules: Rules,
        ): Command {
            return if (state.rerollContext!!.rerollAllowed) {
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
                val context = state.getContext<PickupRollContext>()
                val updatedContext = context.copy(
                    roll = context.roll!!.copy(
                        rerollSource = state.rerollContext!!.source,
                        rerolledResult = d6,
                    ),
                    isSuccess = isPickupSuccess(d6, context.player.agility, context.modifiers)
                )
                compositeCommandOf(
                    SetContext(updatedContext),
                    ExitProcedure(),
                )
            }
        }
    }

    private fun isPickupSuccess(
        roll: D6Result,
        target: Int,
        modifiers: List<DiceModifier>,
    ): Boolean {
        return when(roll.value) {
            1 -> false
            in 2..5 -> roll.value != 1 && (target <= roll.value + modifiers.sum())
            6 -> true
            else -> error("Invalid value: ${roll.value}")
        }
    }
}
