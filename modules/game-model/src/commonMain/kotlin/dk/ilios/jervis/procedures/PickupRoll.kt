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
import dk.ilios.jervis.commands.SetOldContext
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.context.UseRerollContext
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRerollOption
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.rules.skills.RerollSource
import dk.ilios.jervis.utils.INVALID_ACTION
import dk.ilios.jervis.utils.INVALID_GAME_STATE

/**
 * Procedure for handling a Pickup Roll. It is only responsible for handling the actual dice roll.
 * The result is stored in [Game.pickupRollContext]] and it is up to the caller of the procedure to
 * choose the appropriate action depending on the outcome.
 */
object PickupRoll : Procedure() {
    override fun isValid(
        state: Game,
        rules: Rules,
    ) {
        if (state.pickupRollContext == null) {
            INVALID_GAME_STATE("No pickup roll context found")
        }
    }

    override val initialNode: Node = RollDie

    override fun onEnterProcedure(
        state: Game,
        rules: Rules,
    ): Command? = null

    override fun onExitProcedure(
        state: Game,
        rules: Rules,
    ): Command? = null

    object RollDie : ActionNode() {
        override fun getAvailableActions(
            state: Game,
            rules: Rules,
        ): List<ActionDescriptor> = listOf(RollDice(Dice.D6))

        override fun applyAction(
            action: GameAction,
            state: Game,
            rules: Rules,
        ): Command {
            return checkDiceRoll<D6Result>(action) {
                val rollContext = state.pickupRollContext!!
                val target = rollContext.player.agility
                val resultContext =
                    PickupRollResultContext(
                        player = rollContext.player,
                        target = target,
                        modifiers = rollContext.modifiers,
                        roll = D6DieRoll(originalRoll = it),
                        success = isPickupSuccess(it, target, rollContext),
                    )
                return compositeCommandOf(
                    SetOldContext(Game::pickupRollResultContext, resultContext),
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
            val context = state.pickupRollResultContext!!
            val successOnFirstRoll = context.success
            val pickupPlayer = context.player
            val availableSkills: List<SelectRerollOption> =
                pickupPlayer.skills.asSequence()
                    .filter { it is RerollSource }
                    .map { it as RerollSource }
                    .filter { skill -> skill.canReroll(DiceRollType.PICKUP, listOf(context.roll), successOnFirstRoll) }
                    .flatMap { it: RerollSource ->
                        it.calculateRerollOptions(DiceRollType.PICKUP, context.roll, successOnFirstRoll)
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
                        listOf(SelectRerollOption(DiceRerollOption(team.availableRerolls.last(), listOf(context.roll))))
                    } else {
                        emptyList()
                    }
                listOf(SelectNoReroll) + availableSkills + teamReroll
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
            return checkDiceRoll<D6Result>(action) {
                val rollResultContext = state.pickupRollResultContext!!
                val rollContext = state.pickupRollContext!!
                val target = rollContext.player.agility + rollContext.diceModifier()
                val rerollContext =
                    PickupRollResultContext(
                        player = rollContext.player,
                        target = target,
                        modifiers = rollContext.modifiers,
                        roll =
                            rollResultContext.roll.copy(
                                rerollSource = state.rerollContext!!.source,
                                rerolledResult = it,
                            ),
                        success = isPickupSuccess(it, target, rollContext),
                    )
                compositeCommandOf(
                    SetOldContext(Game::pickupRollResultContext, rerollContext),
                    ExitProcedure(),
                )
            }
        }
    }

    private fun isPickupSuccess(
        it: D6Result,
        target: Int,
        rollContext: PickupRollContext,
    ) = it.value != 1 && (target <= it.value + rollContext.diceModifier())
}
