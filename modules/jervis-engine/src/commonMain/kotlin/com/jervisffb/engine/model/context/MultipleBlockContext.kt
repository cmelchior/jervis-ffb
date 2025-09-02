package com.jervisffb.engine.model.context

import com.jervisffb.engine.actions.BlockDicePool
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.context.AddContextListItem
import com.jervisffb.engine.commands.context.SetContextProperty
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BlockContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.PushContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockApplyResult
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockRerollDice
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockRollDice
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.common.actions.BlockType
import com.jervisffb.engine.rules.common.actions.BlockType.CHAINSAW
import com.jervisffb.engine.rules.common.actions.BlockType.MULTIPLE_BLOCK
import com.jervisffb.engine.rules.common.actions.BlockType.PROJECTILE_VOMIT
import com.jervisffb.engine.rules.common.actions.BlockType.STAB
import com.jervisffb.engine.rules.common.actions.BlockType.STANDARD
import com.jervisffb.engine.rules.common.procedures.DieRoll

/**
 * Class wrapping one of the block actions part of a multiple block actions.
 * It also acts as a facade, exposing a shared API for all the different block types.
 */
data class MultipleBlockDiceRoll(
    val type: BlockType,
    var rollContext: ProcedureContext, // The roll specific context for the given type
) {

    fun createDicePool(id: Int): BlockDicePool {
        return when (type) {
            BlockType.BREATHE_FIRE -> TODO()
            CHAINSAW -> TODO()
            MULTIPLE_BLOCK -> TODO()
            PROJECTILE_VOMIT -> TODO()
            STAB -> TODO()
            STANDARD -> BlockDicePool((rollContext as BlockContext).roll, selectDice = 1, id = id)
        }
    }


    fun hasAcceptedResult(): Boolean {
        return when (type) {
            BlockType.BREATHE_FIRE -> TODO()
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
            BlockType.BREATHE_FIRE -> TODO()
            CHAINSAW -> TODO()
            MULTIPLE_BLOCK -> TODO()
            PROJECTILE_VOMIT -> TODO()
            STAB -> TODO()
            STANDARD -> (rollContext as BlockContext).roll
        }
    }

    fun copyAndSetHasAcceptedResult(acceptedResult: Boolean): MultipleBlockDiceRoll {
        return when (type) {
            BlockType.BREATHE_FIRE -> TODO()
            CHAINSAW -> TODO()
            MULTIPLE_BLOCK -> TODO()
            PROJECTILE_VOMIT -> TODO()
            STAB -> TODO()
            STANDARD -> {
                this.copy(rollContext = (rollContext as BlockContext).copy(hasAcceptedResult = acceptedResult))
            }
        }
    }

    fun getRerollOptions(rules: Rules, attacker: Player, dicePoolId: Int): List<GameActionDescriptor> {
        return when (type) {
            BlockType.BREATHE_FIRE -> TODO()
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

    fun setSelectedDieResult(die: DieResult): Command {
        return when (type) {
            BlockType.BREATHE_FIRE -> TODO()
            CHAINSAW -> TODO()
            MULTIPLE_BLOCK -> TODO()
            PROJECTILE_VOMIT -> TODO()
            STAB -> TODO()
            STANDARD -> {
                val context = (rollContext as BlockContext)
                var selectedIndex = -1
                for (i in context.roll.indices) {
                    // This might select another index if two dice have the same value
                    // Does it matter?
                    if (context.roll[i].result == die) {
                        selectedIndex = i
                        break
                    }
                }
                SetContextProperty(BlockContext::resultIndex, context, selectedIndex)
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
    val defender1: Player? = null,
    val defender2: Player? = null,
    val actionAborted: Boolean = true,
    // Rolls for the two blocks
    val roll1: MultipleBlockDiceRoll? = null,
    val roll2: MultipleBlockDiceRoll? = null,
    // Tracks the index of which defender is currently in focus. If set, it must either be 0 or 1.
    var activeDefender: Int? = null,
    // If the blocks result in a push, all the data related to the push is stored here.
    var defender1PushChain: PushContext? = null,
    var defender2PushChain: PushContext? = null,
    // Tracks the ball for those players where it needs to bounce. Set back to `null` once the ball has bounced
    val defender1BallsHandled: Boolean = false,
    val defender2BallsHandled: Boolean = false,
    val attackerBallHandled: Boolean = false,
    // Set if any of the players involved received an injury. The attacker might suffer an
    // injury from both blocks
    val attackerInjuryContext: MutableList<RiskingInjuryContext> = mutableListOf(),
    var defender1InjuryContext: RiskingInjuryContext? = null,
    var defender2InjuryContext: RiskingInjuryContext? = null,
    // Set to true, if a turnover happened during the first block.
    var postponeTurnOver: TurnOver? = null,
    // Player starting locations (as they might leave the field due to injuries)
    val attackerLocation: FieldCoordinate = attacker.coordinates,
    val defender1Location: FieldCoordinate? = null,
    val defender2Location: FieldCoordinate? = null,
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

    fun updateRollContext(index: Int, updatedRollContext: ProcedureContext): Command {
        return when (index) {
            0 -> SetContextProperty(MultipleBlockDiceRoll::rollContext, roll1!!, updatedRollContext)
            1 -> SetContextProperty(MultipleBlockDiceRoll::rollContext, roll2!!, updatedRollContext)
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
        return when (getActiveRerollType()) {
            BlockType.BREATHE_FIRE -> TODO()
            CHAINSAW -> TODO()
            MULTIPLE_BLOCK -> TODO()
            PROJECTILE_VOMIT -> TODO()
            STAB -> TODO()
            STANDARD -> UseRerollContext(DiceRollType.BLOCK, action.getRerollSource(state))
        }
    }

    fun getRollDiceProcedure(): Procedure {
        return when (getActiveRerollType()) {
            BlockType.BREATHE_FIRE -> TODO()
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
            BlockType.BREATHE_FIRE -> TODO()
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
            BlockType.BREATHE_FIRE -> TODO()
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
    fun updateWithLatestBlockTypeContext(state: Game): Command {
        val updatedContext = when (getActiveRerollType()) {
            BlockType.BREATHE_FIRE -> TODO()
            CHAINSAW -> TODO()
            MULTIPLE_BLOCK -> TODO()
            PROJECTILE_VOMIT -> TODO()
            STAB -> TODO()
            STANDARD -> state.getContext<BlockContext>()
        }
        return updateRollContext(activeDefender!!, updatedContext)
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
     * Return the commands needed to add an Injury to the injury pool.
     */
    fun addInjuryReferenceForPlayer(player: Player, injuryContext: RiskingInjuryContext): Command {
        return when (player) {
            attacker -> AddContextListItem(attackerInjuryContext, injuryContext)
            defender1 -> SetContextProperty(MultipleBlockContext::defender1InjuryContext, this, injuryContext)
            defender2 -> SetContextProperty(MultipleBlockContext::defender2InjuryContext, this, injuryContext)
            else -> throw IllegalArgumentException("Invalid player: $player")
        }
    }

    /**
     * Sets the block type for the current active defender.
     * This also c
     */
    fun copyAndSetBlockTypeForActiveDefender(type: BlockType): MultipleBlockContext {
        val defender = when (activeDefender) {
            0 -> defender1!!
            1 -> defender2!!
            else -> throw IllegalStateException("Invalid active defender")
        }

        val context = when (type) {
            BlockType.BREATHE_FIRE -> TODO()
            CHAINSAW -> TODO()
            MULTIPLE_BLOCK -> TODO()
            PROJECTILE_VOMIT -> TODO()
            STAB -> TODO()
            STANDARD -> BlockContext(
                attacker = attacker,
                defender = defender,
                isUsingMultiBlock = true
            )
        }

        return when (activeDefender) {
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
     * See [updateWithLatestBlockTypeContext] for that.
     */
    fun getContextForCurrentBlock(): ProcedureContext {
        return when (activeDefender) {
            0 -> roll1!!.rollContext
            1 -> roll2!!.rollContext
            else -> throw IllegalArgumentException("Invalid active defender: $activeDefender")
        }
    }
}
