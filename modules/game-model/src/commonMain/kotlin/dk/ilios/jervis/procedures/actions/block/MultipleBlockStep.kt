package dk.ilios.jervis.procedures.actions.block

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.BlockTypeSelected
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.ConfirmWhenReady
import dk.ilios.jervis.actions.DeselectPlayer
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.PlayerDeselected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.RerollOptionSelected
import dk.ilios.jervis.actions.SelectBlockType
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetTurnOver
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Ball
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.model.context.UseRerollContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.procedures.DieRoll
import dk.ilios.jervis.procedures.actions.block.multipleblock.MultipleBlockChoseResults
import dk.ilios.jervis.procedures.actions.block.multipleblock.MultipleBlockRerollDice
import dk.ilios.jervis.procedures.actions.block.standard.StandardBlockApplyResult
import dk.ilios.jervis.procedures.actions.block.standard.StandardBlockRerollDice
import dk.ilios.jervis.procedures.actions.block.standard.StandardBlockRollDice
import dk.ilios.jervis.procedures.tables.injury.RiskingInjuryContext
import dk.ilios.jervis.rules.BlockType
import dk.ilios.jervis.rules.BlockType.CHAINSAW
import dk.ilios.jervis.rules.BlockType.MULTIPLE_BLOCK
import dk.ilios.jervis.rules.BlockType.PROJECTILE_VOMIT
import dk.ilios.jervis.rules.BlockType.STAB
import dk.ilios.jervis.rules.BlockType.STANDARD
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.utils.INVALID_ACTION
import dk.ilios.jervis.utils.INVALID_GAME_STATE

/**
 * Class wrapping one of the block actions part of a multiple block actions.
 * It also acts as a facade, exposing a shared API for all the different block types.
 */
data class MultipleBlockDiceRoll(
    val type: BlockType,
    val rollContext: ProcedureContext, // The roll specific context for the given type
) {

    fun hasAcceptedResult(): Boolean {
        return when (type) {
            CHAINSAW -> TODO()
            MULTIPLE_BLOCK -> TODO()
            PROJECTILE_VOMIT -> TODO()
            STAB -> TODO()
            STANDARD -> {
                (rollContext as BlockContext).hasAcceptedResult
            }
        }
    }

    fun getRoll(): List<DieRoll<*>> {
        return when (type) {
            CHAINSAW -> TODO()
            MULTIPLE_BLOCK -> TODO()
            PROJECTILE_VOMIT -> TODO()
            STAB -> TODO()
            STANDARD -> (rollContext as BlockContext).roll
        }
    }

    fun copyAndSetHasAcceptedResult(acceptedResult: Boolean): MultipleBlockDiceRoll {
        return when (type) {
            CHAINSAW -> TODO()
            MULTIPLE_BLOCK -> TODO()
            PROJECTILE_VOMIT -> TODO()
            STAB -> TODO()
            STANDARD -> {
                this.copy(rollContext = (rollContext as BlockContext).copy(hasAcceptedResult = acceptedResult))
            }
        }
    }

    fun getRerollOptions(rules: Rules, attacker: Player, dicePoolId: Int): List<ActionDescriptor> {
        return when (type) {
            CHAINSAW -> TODO()
            MULTIPLE_BLOCK -> TODO()
            PROJECTILE_VOMIT -> TODO()
            STAB -> TODO()
            STANDARD -> {
                StandardBlockRerollDice.getRerollOptions(
                    rules = rules,
                    attackingPlayer = attacker,
                    dicePoolId = dicePoolId,
                    diceRoll = (rollContext as BlockContext).roll
                )
            }
        }
    }
}

/**
 * Context containing state related to doing a Multiple Block.
 *
 * Note, this context has been flattened to make it easier to update, but
 * it exposes an API that makes it possible to access rolls using list
 * indexes.
 */
data class MultipleBlockContext(
    val attacker: Player,
    val defender1: Player?,
    val defender2: Player? = null,
    // Rolls for the two blocks
    val roll1: MultipleBlockDiceRoll? = null,
    val roll2: MultipleBlockDiceRoll? = null,
    // Tracks the index of which defender is currently in focus. If set, it must either be 0 or 1.
    val activeDefender: Int? = null,
    // Tracks the ball for those players where it needs to bounce
    val attackerBall: Ball? = null,
    val defender1Ball: Ball? = null,
    val defender2Ball: Ball? = null,
    // Set if any of the players involved received an injury.
    val attackerInjuryContext: RiskingInjuryContext? = null,
    val defender1InjuryContext: RiskingInjuryContext? = null,
    val defender2InjuryContext: RiskingInjuryContext? = null,
    // Set to true, if a turnover happened during the first block.
    val postponeTurnOver: Boolean = false
): ProcedureContext {

    val rolls: List<MultipleBlockDiceRoll>
        get() = listOfNotNull(roll1, roll2)

    operator fun get(index: Int): MultipleBlockDiceRoll {
        return when (index) {
            0 -> roll1!!
            1 -> roll2!!
            else -> throw IllegalArgumentException("Invalid index: $index")
        }
    }

    fun getActiveRerollType(): BlockType {
        return get(activeDefender!!).type
    }

    fun copyAndUpdateRollContext(index: Int, updatedRollContext: ProcedureContext): MultipleBlockContext {
        return when (index) {
            0 -> copy(roll1 = roll1!!.copy(rollContext = updatedRollContext))
            1 -> copy(roll2 = roll2!!.copy(rollContext = updatedRollContext))
            else -> throw IllegalArgumentException("Invalid roll index: $index")
        }
    }

    fun copyAndUpdateHasAcceptedResult(index: Int, hasAcceptedResult: Boolean): MultipleBlockContext {
        return when (index) {
            0 -> copy(roll1 = roll1!!.copyAndSetHasAcceptedResult(hasAcceptedResult))
            1 -> copy(roll2 = roll2!!.copyAndSetHasAcceptedResult(hasAcceptedResult))
            else -> throw IllegalArgumentException("Invalid index: $index")
        }
    }

    /**
     * Creates a [UseRerollContext] for currently active Multiple Block Action its reroll type
     */
    fun createRerollContext(state: Game, action: RerollOptionSelected): UseRerollContext {
        return when(getActiveRerollType()) {
            CHAINSAW -> TODO()
            MULTIPLE_BLOCK -> TODO()
            PROJECTILE_VOMIT -> TODO()
            STAB -> TODO()
            STANDARD -> UseRerollContext(DiceRollType.BLOCK, action.getRerollSource(state))
        }
    }

    fun getRollDiceProcedure(): Procedure {
        return when (getActiveRerollType()) {
            CHAINSAW -> TODO()
            MULTIPLE_BLOCK -> TODO()
            PROJECTILE_VOMIT -> TODO()
            STAB -> TODO()
            STANDARD -> StandardBlockRollDice
        }
    }
    /**
     * Returns the Procedure used to reroll dice for the given block type.
     */
    fun getRerollDiceProcedure(): Procedure {
        return when (getActiveRerollType()) {
            CHAINSAW -> TODO()
            MULTIPLE_BLOCK -> TODO()
            PROJECTILE_VOMIT -> TODO()
            STAB -> TODO()
            STANDARD -> StandardBlockRerollDice
        }
    }

    /**
     * Returns the procedure responsible for applying a active block type
     */
    fun getResolveBlockResultProcedure(): Procedure {
        return when (getActiveRerollType()) {
            CHAINSAW -> TODO()
            MULTIPLE_BLOCK -> TODO()
            PROJECTILE_VOMIT -> TODO()
            STAB -> TODO()
            STANDARD -> StandardBlockApplyResult
        }
    }

    /**
     * Calling this method will retrieve the roll context for the given
     * block type and replace the active [MultipleBlockDiceRoll.rollContext]
     * with it.
     */
    fun copyAndUpdateWithLatestBlockTypeContext(state: Game): MultipleBlockContext {
        val updatedContext = when (getActiveRerollType()) {
            CHAINSAW -> TODO()
            MULTIPLE_BLOCK -> TODO()
            PROJECTILE_VOMIT -> TODO()
            STAB -> TODO()
            STANDARD -> state.getContext<BlockContext>()
        }
        return copyAndUpdateRollContext(activeDefender!!, updatedContext)
    }

    /**
     * Remove the provided player from the context. Also unset it from being
     * active if it was set there.
     *
     * Will throw exception if player was not found
     */
    fun copyAndUnsetDefender(player: Player): ProcedureContext {
        return when (player) {
            defender1 -> copy(defender1 = null, activeDefender = if (activeDefender == 0) null else 0)
            defender2 -> copy(defender2 = null, activeDefender = if (activeDefender == 1) null else 1)
            else -> throw IllegalArgumentException("Invalid defender: $player")
        }
    }

    /**
     * Set the ball reference for the current defender.
     */
    fun copyAndTrackBouncingBallForPlayer(player: Player, ball: Ball): ProcedureContext {
        return when (player) {
            attacker -> copy(attackerBall = ball)
            defender1 -> copy(defender1Ball = ball)
            defender2 -> copy(defender2Ball = ball)
            else -> throw IllegalArgumentException("Invalid player: $player")
        }
    }

    fun copyAndSetInjuryReferenceForPlayer(player: Player, injuryContext: RiskingInjuryContext): ProcedureContext {
        return when (player) {
            attacker -> copy(attackerInjuryContext = injuryContext)
            defender1 -> copy(defender1InjuryContext = injuryContext)
            defender2 -> copy(defender2InjuryContext = injuryContext)
            else -> throw IllegalArgumentException("Invalid player: $player")
        }
    }

    /**
     * Sets the block type for the current active defender.
     * This also c
     */
    fun copyAndSetBlockTypeForActiveDefender(type: BlockType): ProcedureContext {
        val defender = when (activeDefender) {
            0 -> defender1!!
            1 -> defender2!!
            else -> throw IllegalStateException("Invalid active defender")
        }

        val context = when (type) {
            CHAINSAW -> TODO()
            MULTIPLE_BLOCK -> TODO()
            PROJECTILE_VOMIT -> TODO()
            STAB -> TODO()
            STANDARD -> BlockContext(
                attacker,
                defender,
            )
        }

        return when(activeDefender) {
            0 -> copy(roll1 = MultipleBlockDiceRoll(type, context))
            1 -> copy(roll2 = MultipleBlockDiceRoll(type, context))
            else -> throw IllegalArgumentException("Invalid active defender: $activeDefender")
        }
    }

    fun getActiveDefender(): Player? {
        return when (activeDefender) {
            0 -> return defender1!!
            1 -> return defender2!!
            else -> null
        }
    }

    /**
     * Returns the block context for the currently active defender.
     * Note, it is the context stored in _this_ context that is returned,
     * and not the one stored globally.
     *
     * See [copyAndUpdateWithLatestBlockTypeContext] for that.
     */
    fun getContextForCurrentBlock(): ProcedureContext {
        return when (activeDefender) {
            0 -> roll1!!.rollContext
            1 -> roll2!!.rollContext
            else -> throw IllegalArgumentException("Invalid active defender: $activeDefender")
        }
    }


}

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
 * Multiple Block is a challenge to implement as it breaks the flow of multiple skills and actions.
 * It is also very vaguely worded, and the description doesn't cover a lot of edge cases.
 **
 * It is especially unclear how "performed simultaneously" should be interpreted, but to accommodate Designer's
 * Commentary, we try to resolve the blocks in lock-steps (or as much as possible).
 *
 * It is also unclear how to handle injuries, e.g., should we roll armour/injury dice in lockstep
 * and how does that work with e.g. Stab where the armour/injury roll behave differently.
 * And what about bouncing balls, when do they bounce?
 *
 * Since the rules define none of this, we opt for simplicity and implement lock-step up until the
 * point of resolving a block type, then resolve that block type up to the point of having rolled for armour/injury.
 * And then finally choose to use apothecary/regeneration or not.
 *
 * For pushes, this means that we fully resolve one push before doing the next.

 * It is unclear what should happen in a circular chain push. In that case, the starting square will be considered
 * empty. This also mirrors the normal push behavior.
 *
 * The starting square will be considered "occupied" for when using Grab on the 2nd player. While this is
 * inconsistent with pushes, it does match the rules described in the Designer's Commentary.
 *
 * Also, if we treated the starting square as blocked for pushes, it could result in some scenarios that
 * would look weird on a board. So given that this scenario will probably never surface in a realistic
 * game of Blood Bowl, having the behaviour being different seems acceptable.
 *
 * For injuries, we are introducing an "Injury Pool". This means that all injuries that happened to attacker
 * and defenders are placed in that pool and not fully resolved until both actions are "done". Then the blocking
 * coach can see the full effect of the block before deciding whether to use the apothecary on
 * all or some of them.
 *
 * For the sake of ease of implementation, crowd-pushes on other players than the ones being blocked
 * are resolved immediately, i.e., they are not placed in the "Injury Pool".
 *
 * All of this means that the logic of operations is as follows:
 *
 * 1. Select both block target and type of block, i.e., normal block or special action.
 *    a. It is possible to deselect an already selected player again.
 *    b. It is possible to abort the Multiple Block at this point.
 *
 * 2. Roll non-armor/injury dice for actions.
 *    a. Block: Normal block dice
 *    b. Projectile Vomit: D6 for who is hit.
 *    c. Chainsaw: D6 for who is hit.
 *    d. Breathe Fire: D6 for effect.
 *    c. Stab: No roll here, since it automatically "hit".
 *
 * 3. Select rerolls for both rolls and apply them. This process can happen multiple times here.
 *    a. Blocking player is free to choose the order of using rerolls or not.
 *    b. Pro still only works on one of them.
 *
 * 4. After rerolls are chosen or not. Select the result of both rolls that should be applied.
 *
 * 5. Let the active player decide which player to resolve first.
 *
 * 6. Resolve block results:
 *    a. If two normal blocks, resolve push/chainpushes individually, including rolling for injuries,
 *       but not rolling for apothecary or bouncing the ball. The player still stays on the field and
 *       any injuries are placed in an "Injury Pool". Injuries caused by chain pushing other players
 *       into the crowd are resolved fully immediately.
 *    b. If a special action is combined with a block. Resolve each action as chosen by the blocker. Any
 *       injuries are placed in the "injury pool".
 *
 *    c. Any turn-over caused by the first block is postponed until the 2nd block has resolved, i.e., the
 *       blocker does not loose access to skills mid-block.
 *
 * 7. Show the "injury pool" and let the blocking coach decide where to apply the apothecary. Even on all
 *    injuries or only some of them.
 *
 * 8. Finally, if any of the attacker or two defenders had the ball. It will now bounce from their square,
 *    (or the square they left)
 */
object MultipleBlockStep: Procedure() {
    override val initialNode: Node = SelectDefenderOrContinueBlock
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val blockContext = state.getContext<BlockActionContext>()
        return SetContext(MultipleBlockContext(
            attacker = blockContext.attacker,
            defender1 = blockContext.defender
        ))
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return compositeCommandOf(
            RemoveContext<MultipleBlockContext>()
        )
    }

    /**
     * Node responsible for selecting targets and optionally deselecting them again. Once
     * both targets are selected, we move to the next step using a `Confirm` action.
     *
     * (We could also choose to transition automatically, but currently let this be up to
     * the UI layer, as it choose to automatically respond with Confirm rather than showing
     * it to the user).
     */
    object SelectDefenderOrContinueBlock: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.activeTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<MultipleBlockContext>()
            val attacker = context.attacker
            val defender1 = context.defender1
            val defender2 = context.defender2

            val eligibleDefenders: List<ActionDescriptor> =
                attacker.coordinates.getSurroundingCoordinates(rules)
                    .filter { state.field[it].isOccupied() }
                    .filter { state.field[it].player!!.let { player ->
                        player.team != attacker.team &&
                        player != defender1 &&
                        player != defender2
                    }}
                    .map { SelectPlayer(state.field[it].player!!) }


            val deselectCommands = listOf(
                DeselectPlayer(attacker),
                if (defender1 != null) DeselectPlayer(defender1) else null,
                if (defender2 != null) DeselectPlayer(defender2) else null
            )

            // If both targets are selected, blocker can continue the multiblock.
            // Otherwise, they must continue to make selections.
            return if (defender1 != null && defender2 != null) {
                (deselectCommands + ConfirmWhenReady).filterNotNull()
            } else {
                (deselectCommands + eligibleDefenders).filterNotNull()
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val blockContext = state.getContext<BlockContext>()
            val context = state.getContext<MultipleBlockContext>()
            return when (action) {
                is Confirm -> {
                    if (!getAvailableActions(state, rules).contains(ConfirmWhenReady)) {
                        INVALID_ACTION(action)
                    }
                    compositeCommandOf(
                        SetContext(context.copy(activeDefender = null)),
                        GotoNode(RollDiceForTarget1)
                    )
                }
                is PlayerDeselected -> {
                    // If the attacker is deselected, it is a sign to cancel the block
                    // if a defender is deselected, that is fine, just update the context and redo this logic.
                    when (val player = action.getPlayer(state)) {
                        blockContext.attacker -> ExitProcedure()
                        else -> {
                            val updatedContext = context.copyAndUnsetDefender(player)
                            compositeCommandOf(
                                SetContext(updatedContext),
                                GotoNode(SelectDefenderOrContinueBlock)
                            )
                        }
                    }
                }
                is PlayerSelected -> {
                    val player = action.getPlayer(state)
                    when {
                        context.defender1 == null -> {
                            compositeCommandOf(
                                SetContext(context.copy(defender1 = player, activeDefender = 0)),
                                GotoNode(SelectBlockTypeAgainstSelectedDefender)
                            )
                        }
                        context.defender2 == null -> {
                            compositeCommandOf(
                                SetContext(context.copy(defender2 = player, activeDefender = 1)),
                                GotoNode(SelectBlockTypeAgainstSelectedDefender)
                            )
                        }
                        else -> INVALID_ACTION(action)
                    }
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    /**
     * Once a target has been selected, we also need to set the block type for the given player.
     * To give the UI more flexibility, we also allow the player to be deselected at this step,
     * which will remove the player as a target.
     */
    object SelectBlockTypeAgainstSelectedDefender: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.activeTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val attacker = state.getContext<MultipleBlockContext>().getActiveDefender()!!
            val availableBlockTypes = BlockAction.getAvailableBlockType(attacker, true)
            return availableBlockTypes.map {
                SelectBlockType(it)
            } + DeselectPlayer(attacker)
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<MultipleBlockContext>()
            return when (action) {
                is PlayerDeselected -> {
                    val player = action.getPlayer(state)
                    if (player != context.getActiveDefender()) INVALID_ACTION(action)
                    compositeCommandOf(
                        SetContext(context.copyAndUnsetDefender(player)),
                        GotoNode(SelectDefenderOrContinueBlock)
                    )
                }
                else -> {
                    checkTypeAndValue<BlockTypeSelected>(state, rules, action) { typeSelected ->
                        val type = typeSelected.type
                        val updatedContext = context.copyAndSetBlockTypeForActiveDefender(type)
                        compositeCommandOf(
                            SetContext(updatedContext),
                            GotoNode(SelectDefenderOrContinueBlock)
                        )
                    }
                }
            }
        }
    }

    /**
     * Node responsible for rolling the dice for the first target. Which dice to roll depends
     * on the type of block.
     */
    object RollDiceForTarget1: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            return getEnterBlockTypeNodeCommands(state, 0)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            val context = state.getContext<MultipleBlockContext>()
            return context.getRollDiceProcedure()
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            val leaveCommands = getLeaveBlockTypeNodeCommands(state)
            return compositeCommandOf(
                leaveCommands,
                GotoNode(RollDiceForTarget2)
            )
        }
    }

    /**
     * Node responsible for rolling the dice for the second target. Which dice to roll depends
     * on the type of block.
     */
    object RollDiceForTarget2: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            return getEnterBlockTypeNodeCommands(state, 1)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            val context = state.getContext<MultipleBlockContext>()
            return context.getRollDiceProcedure()
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            val leaveCommands = getLeaveBlockTypeNodeCommands(state)
            return compositeCommandOf(
                leaveCommands,
                GotoNode(RerollAllDice)
            )
        }
    }

    /**
     * With both blocks having rolled their first set of dice, it is now possible
     * to choose which dice to reroll. When using Multiple Block, it should be possible
     * to see both rolls at the same time and choose rerolls from both.
     */
    object RerollAllDice: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = MultipleBlockRerollDice
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(SelectBlockResults)
        }
    }

    /**
     * After rerolls, the active coach now needs to select the results of
     * each block that they should resolve. This is done at the same time.
     */
    object SelectBlockResults: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = MultipleBlockChoseResults
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(SelectPlayerToResolve)
        }
    }

    /**
     * After choosing the final results, now choose which player to resolve first.
     */
    object SelectPlayerToResolve: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.activeTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<MultipleBlockContext>()
            return listOf(
                SelectPlayer(context.defender1!!.id),
                SelectPlayer(context.defender2!!.id),
            )
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkTypeAndValue<PlayerSelected>(state, rules, action) { playerSelected ->
                val context = state.getContext<MultipleBlockContext>()
                val activeDefenderIndex = when (val player = playerSelected.getPlayer(state)) {
                    context.defender1 -> 0
                    context.defender2 -> 1
                    else -> INVALID_GAME_STATE("No matching defender: $player")
                }
                compositeCommandOf(
                    SetContext(context.copy(activeDefender = activeDefenderIndex)),
                    GotoNode(ResolveFirstPlayer)
                )
            }
        }
    }

    /**
     * Resolve the result of the first player, including rolling for injuries.
     * But do not fully resolve the injury yet. Iif resolving this block would result in a turnover
     * for the blocking player, the turn-over is delayed until the second result has been resolved.
     * E.g., the blocking player does not lose access to skills because they roll a Player Down before
     * rolling for Stab on the second target.
     */
    object ResolveFirstPlayer: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<MultipleBlockContext>()
            return getEnterBlockTypeNodeCommands(state, context.activeDefender!!)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            val context = state.getContext<MultipleBlockContext>()
            return context.getResolveBlockResultProcedure()
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<MultipleBlockContext>()
            val isTurnOver = state.isTurnOver
            val nextDefender = when (context.activeDefender) {
                0 -> 1
                1 -> 0
                else -> throw IllegalArgumentException("Invalid active defender: ${context.activeDefender}")
            }

            val contextClass = context.getContextForCurrentBlock()::class
            var updatedMultiBlockContext = context.copyAndUpdateWithLatestBlockTypeContext(state)
            updatedMultiBlockContext = updatedMultiBlockContext.copy(activeDefender = nextDefender, postponeTurnOver = isTurnOver)

            return compositeCommandOf(
                RemoveContext(contextClass),
                SetTurnOver(false),
                SetContext(updatedMultiBlockContext),
                GotoNode(ResolveSecondPlayer)
            )
        }
    }

    /**
     * Resolve the result of the second player, including rolling for injuries.
     * But do not fully resolve the injury yet.
     */
    object ResolveSecondPlayer: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<MultipleBlockContext>()
            return getEnterBlockTypeNodeCommands(state, context.activeDefender!!)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            val context = state.getContext<MultipleBlockContext>()
            return context.getResolveBlockResultProcedure()
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<MultipleBlockContext>()

            val contextClass = context.getContextForCurrentBlock()::class
            var updatedMultiBlockContext = context.copyAndUpdateWithLatestBlockTypeContext(state)
            updatedMultiBlockContext = updatedMultiBlockContext.copy(activeDefender = null)

            return compositeCommandOf(
                RemoveContext(contextClass),
                SetTurnOver(context.postponeTurnOver),
                SetContext(updatedMultiBlockContext),
                GotoNode(ResolveInjuries)
            )
        }
    }

    /**
     * Fully resolve the "injury pool", i.e., choose to allow it or use
     * skills or apothecaries to modify the result.
     */
    object ResolveInjuries: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            TODO("Not yet implemented")
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(BounceBallIfNeeded)
        }
    }

    object BounceBallIfNeeded: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.activeTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            TODO()
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            TODO("Not yet implemented")
        }
    }

    // ------------------------------------------------------------------------------------------------------------
    // HELPER FUNCTIONS

    /**
     * Returns the commands that set the correct contexts just before calling down into
     * a sub procedure for a specific block type.
     */
    fun getEnterBlockTypeNodeCommands(state: Game, activeDefender: Int): Command {
        val context = state.getContext<MultipleBlockContext>()
        val updatedContext = context.copy(activeDefender = activeDefender)
        return compositeCommandOf(
            SetContext(updatedContext),
            SetContext(context.getContextForCurrentBlock())
        )
    }

    /**
     * Returns the commands needed to clear the context from a specific sub procedure,
     * as well as making sure that [MultipleBlockContext] is updated.
     */
    fun getLeaveBlockTypeNodeCommands(state: Game): Command {
        val context = state.getContext<MultipleBlockContext>()
        val updatedMultiBlockContext = context.copyAndUpdateWithLatestBlockTypeContext(state)
        val contextClass = context.getContextForCurrentBlock()::class
        return compositeCommandOf(
            RemoveContext(contextClass),
            SetContext(updatedMultiBlockContext)
        )
    }
}
