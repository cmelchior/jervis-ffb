package com.jervisffb.engine.rules.bb2020.procedures.actions.block

import com.jervisffb.engine.actions.BlockDicePool
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Ball
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.UseRerollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.BlockType
import com.jervisffb.engine.rules.BlockType.CHAINSAW
import com.jervisffb.engine.rules.BlockType.MULTIPLE_BLOCK
import com.jervisffb.engine.rules.BlockType.PROJECTILE_VOMIT
import com.jervisffb.engine.rules.BlockType.STAB
import com.jervisffb.engine.rules.BlockType.STANDARD
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.DieRoll
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockApplyResult
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockRerollDice
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockRollDice
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.bb2020.skills.DiceRollType
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Class wrapping one of the block actions part of a multiple block actions.
 * It also acts as a facade, exposing a shared API for all the different block types.
 */
data class MultipleBlockDiceRoll(
    val type: BlockType,
    val rollContext: ProcedureContext, // The roll specific context for the given type
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

    fun copyAndSetSelectedResult(die: DieResult): MultipleBlockDiceRoll {
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
                this.copy(
                    rollContext = rollContext.copy(resultIndex  = selectedIndex)
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
    val defender1: Player? = null,
    val defender2: Player? = null,
    val actionAborted: Boolean = true,

    // Rolls for the two blocks
    val roll1: MultipleBlockDiceRoll? = null,
    val roll2: MultipleBlockDiceRoll? = null,
    // Tracks the index of which defender is currently in focus. If set, it must either be 0 or 1.
    val activeDefender: Int? = null,
    // Tracks the ball for those players where it needs to bounce. Set back to `null` once the ball has bounced
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
    fun copyAndUpdateWithLatestBlockTypeContext(state: Game): MultipleBlockContext {
        val updatedContext = when (getActiveRerollType()) {
            BlockType.BREATHE_FIRE -> TODO()
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

    fun copyAndSetInjuryReferenceForPlayer(player: Player, injuryContext: RiskingInjuryContext): MultipleBlockContext {
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

    fun copyAndRemoveBallRef(ball: Ball): MultipleBlockContext {
        return if (attackerBall == ball) {
            copy(attackerBall = null)
        } else if (defender1Ball == ball) {
            copy(defender1Ball = null)
        } else if (defender2Ball == ball) {
            copy(defender2Ball = null)
        } else {
            INVALID_GAME_STATE("Ball not found: $ball")
        }
    }
}
