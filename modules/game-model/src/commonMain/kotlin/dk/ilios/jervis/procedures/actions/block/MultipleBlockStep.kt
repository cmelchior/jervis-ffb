package dk.ilios.jervis.procedures.actions.block

import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.DeselectPlayer
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.PlayerDeselected
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.rules.Rules

data class MultipleBlockContext(
    val defender1: Player?,
    val defender2: Player? = null,
    val currentDefender: Player? = null,
): ProcedureContext

/**
 * This procedure is responsible for handling Multiple Block as described on page 80 in the rulebook.
 *
 * Designer's Commentary:
 * - When using Grab, the blocked players starting square are treated as being Occupied, regardless of
 *   who is moved first.
 * - Blocked players cannot offer assists to each other.
 * - Stab can be used on both block actions.
 *
 * Developer's Commentary:
 * Multiple Block is a nightmare to implement as it breaks the flow of multiple skills and actions.
 * It is also very vaguely worded. It is especially unclear how "performed simultaneously" should be
 * interpreted. At least with respect to pushbacks (where it is impossible to do so). To accommodate
 * Designer's Commentary, we let the player choose which player to resolve push for first and then
 * fully resolve that push. But the starting square will be considered "occupied" for the purpose of
 * Grab if that is used on the other player.
 *
 * It is unclear what should happen in circular chain pushes. In that case, the
 * square will be considered empty (effectively stopping chain pushes).
 *
 * It is also unclear how to handle injuries, i.e., how does an apothecary work in this case. In that
 * case, we are introducing an "injury pool", i.e., all injuries happening to the two blocked players
 * are placed in that pool and not fully resolved until both actions are "done". Then the blocking
 * coach can see the full effect of the block before deciding whether to use the apothecary on
 * all or some of them.
 *
 * For the sake of ease of implementation, crowd-pushes on other players than the ones being blocked
 * are resolved immediately, i.e., they are not placed in the "injury pool".
 *
 * So in general, this procedure will attempt to run as many choices as possible in parallel. This means
 * the logic is as follows:
 *
 * 1. Select both block target and type of block, i.e., normal block or special action.
 *    a. It is possible to deselect an already selected player again.
 *    b. It is possible to abort the Multiple Block.
 *
 * 2. Roll non-armor/injury dice for actions
 *    a. Block: Normal block dice
 *    b. Projectile Vomit: D6 for who is hit.
 *    c. Chainsaw: D6 for who is hit.
 *    d. Breathe Fire: D6 for effect.
 *    c. Stab: No roll here, since it automatically "hit".
 *
 * 3. Use rerolls for both rolls.
 *    a. Blocking player is free to choose order of using rerolls / no-rerolls.
 *    b. Pro still only works on one of them
 *
 * 4. After rerolls are chosen or not. Select the result of both rolls that should be applied.
 *
 * 5. Let the active player decide which player to resolve first.
 *
 * 6. Resolve block results:
 *    a. If two normal blocks, resolve push/chainpushes individually, but do not roll for Injury until
 *       both players are pushed/downed. Injuries caused by chain pushing other players into the crowd
 *       are rolled for immediately.
 *    b. If a special action is combined with a block. Resolve each action as chosen by the blocker. Any
 *       injuries are placed in the "injury pool".
 *
 * 7. Show the "injury pool" and let the blocking coach decide where to apply the apothecary. Even on all
 *    injuries or only some of them.
 *
 */
object MultipleBlockStep: Procedure() {
    override val initialNode: Node = SelectTargetOrContinueBlock
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val blockContext = state.getContext<BlockContext>()
        return SetContext(MultipleBlockContext(
            defender1 = blockContext.defender
        ))
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    /**
     * Node responsible for selecting targets and optionally deselecting them again. Once
     * both targets are selected, we move to the next step using a `Confirm` action.
     *
     * (We could also choose to transition automatically, but currently let this be up to
     * the UI layer, as it choose to automatically respond with Confirm rather than showing
     * it to the user).
     */
    object SelectTargetOrContinueBlock: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = state.activeTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val attacker = state.activePlayer!!
            val defender1 = state.getContext<MultipleBlockContext>().defender1
            val defender2 = state.getContext<MultipleBlockContext>().defender2
            val eligibleDefenders: List<ActionDescriptor> =
                attacker.coordinates.getSurroundingCoordinates(rules)
                    .filter { state.field[it].isOccupied() }
                    .filter { state.field[it].player!!.let { player ->
                        player.team != attacker.team &&
                        player != defender1 &&
                        player != defender2
                    }}
                    .map { SelectPlayer(state.field[it].player!!) }
            return (listOf(
                DeselectPlayer(attacker),
                if (defender1 != null) DeselectPlayer(defender1) else null,
                if (defender2 != null) DeselectPlayer(defender2) else null
            ) + eligibleDefenders).filterNotNull()
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is PlayerDeselected -> {
                    // If attacker is deselected, it is a sign to cancel the block
                    // if a defender is deselected, that is fine, just update the context and redo this logic.
                    ExitProcedure()
                }
                else -> {
                    TODO()
//                   checkTypeAndValue<PlayerSelected>(state, rules, action, this) { playerSelected ->
//                       playerSelected.getPlayer(state)
//                   }
                }
            }
        }
    }

    /**
     * Node responsible for selecting the block type for the currently selected player.
     * To make the flow more flexible, we also allow the user to deselect the player here.
     */
    object SelectBlockType: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? {
            TODO("Not yet implemented")
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            TODO("Not yet implemented")
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            TODO("Not yet implemented")
        }
    }


//    object RollTarget1BlockTypeDice: ActionNode() {
//
//    }
//
//    object RollTarget1BlockTypeDice: ActionNode() {}
//
//
//    object ChooseRerollSourcesOrContinueBlock: ActionNode()
//
//    object UseRerolls: ActionNode()
//
//    object SelectBlockResults: ActionNode() {}
//
//    object SelectPlayerToResolve: ActionNode() {}
//
//    object ResolveFirstPlayer: ParentNode() {}
//
//    object ResolveSecondPlayer: ParentNode() {}
//
//    object RollForTarget1Injuries: ActionNode() {}
//
//    object RollForTarget2Injuries: ActionNode() {}
//
//    object ResolveInjuries: ParentNode() {}
//


}
