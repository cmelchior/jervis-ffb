package com.jervisffb.engine

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.CalculatedAction
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.CoinSideSelected
import com.jervisffb.engine.actions.CoinTossResult
import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.D12Result
import com.jervisffb.engine.actions.D16Result
import com.jervisffb.engine.actions.D20Result
import com.jervisffb.engine.actions.D2Result
import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D4Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.DeselectPlayer
import com.jervisffb.engine.actions.DevModeGameAction
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DicePoolResultsSelected
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.DogoutSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.EndSetupWhenReady
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.EndTurnWhenReady
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.ForegoActivationSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.actions.InducementSelected
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PassTypeSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerDeselected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.PlayersSelected
import com.jervisffb.engine.actions.RandomPlayersSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.Revert
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectBlockType
import com.jervisffb.engine.actions.SelectCoinSide
import com.jervisffb.engine.actions.SelectDicePoolResult
import com.jervisffb.engine.actions.SelectDirection
import com.jervisffb.engine.actions.SelectDogout
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.engine.actions.SelectForgoActivation
import com.jervisffb.engine.actions.SelectMoveType
import com.jervisffb.engine.actions.SelectNoReroll
import com.jervisffb.engine.actions.SelectPassType
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.SelectPlayerAction
import com.jervisffb.engine.actions.SelectPlayers
import com.jervisffb.engine.actions.SelectRandomPlayers
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.actions.SelectSkill
import com.jervisffb.engine.actions.SkillSelected
import com.jervisffb.engine.actions.TossCoin
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.utils.singleInstanceOfOrNull

/**
 * This class represents a request from the [GameEngineController] to generate
 * a [GameAction] for the current [ActionNode]..
 *
 * @see [GameEngineController.getAvailableActions]
 */
data class ActionRequest(
    val id: GameActionId, // The id for the next action being generated
    val team: Team?,
    val actions: List<GameActionDescriptor>
): List<GameActionDescriptor> by actions {
    val actionsCount = actions.sumOf { it.size } // TODO Should also count all sub actions

    fun contains(type: MoveType): Boolean {
        val found = actions.firstOrNull {
            it is SelectMoveType }
            ?.let { (it as SelectMoveType).types.contains(type) }
        return found == true
    }

    inline fun <reified T: GameActionDescriptor> contains(): Boolean {
        return actions.any { it is T }
    }

    inline fun <reified T: GameActionDescriptor> get(): T {
        return actions.single { it is T } as T
    }

    inline fun <reified T: GameActionDescriptor> getOrNull(): T? {
        return actions.singleOrNull { it is T } as T?
    }

    /**
     * Returns `true` if the given action is one part of this request, `false` if not.
     */
    fun isValid(action: GameAction): Boolean {
        return when (action) {
            is BlockTypeSelected -> {
                actions.singleInstanceOfOrNull<SelectBlockType>()?.types.orEmpty().contains(action.type)
            }
            is PassTypeSelected -> {
                actions.singleInstanceOfOrNull<SelectPassType>()?.types.orEmpty().contains(action.type)
            }
            is CalculatedAction -> true // Only used by tests, so always accept
            Cancel -> actions.contains(CancelWhenReady)
            is CoinSideSelected -> actions.contains(SelectCoinSide)
            is CoinTossResult -> actions.contains(TossCoin)
            is CompositeGameAction -> TODO()
            Confirm -> actions.contains(ConfirmWhenReady)
            Continue -> actions.contains(ContinueWhenReady)
            is DicePoolResultsSelected -> {
                actions.singleInstanceOfOrNull<SelectDicePoolResult>()?.let { actionRequest ->
                    val pools = actionRequest.pools.size
                    if (action.results.size != pools) return false
                    action.results.forEachIndexed { index, responsePool ->
                        val requestPool = actionRequest.pools[index]
                        if (requestPool.id != responsePool.id) return false
                        if (requestPool.selectDice != responsePool.diceSelected.size) return false
                        responsePool.diceSelected.forEach { dice ->
                            requestPool.dice.find { it.id == dice.id && it.result == dice.result } ?: return false
                        }
                    }
                    true
                } ?: false
            }
            is DiceRollResults -> {
                actions.singleInstanceOfOrNull<RollDice>()?.let { rollRequest ->
                    if (action.rolls.size != rollRequest.dice.size) return false
                    action.forEachIndexed { index, result ->
                        val isMatch = when (result) {
                            is D12Result -> (rollRequest.dice[index] == Dice.D12)
                            is D16Result -> (rollRequest.dice[index] == Dice.D16)
                            is D20Result -> (rollRequest.dice[index] == Dice.D20)
                            is D2Result -> (rollRequest.dice[index] == Dice.D2)
                            is D3Result -> (rollRequest.dice[index] == Dice.D3)
                            is D4Result -> (rollRequest.dice[index] == Dice.D4)
                            is D6Result -> (rollRequest.dice[index] == Dice.D6)
                            is D8Result -> (rollRequest.dice[index] == Dice.D8)
                            is DBlockResult -> (rollRequest.dice[index] == Dice.BLOCK)
                        }
                        if (!isMatch) return false
                    }
                    true
                } ?: false
            }
            is D12Result -> checkDiceRequest(Dice.D12, actions)
            is D16Result -> checkDiceRequest(Dice.D16, actions)
            is D20Result -> checkDiceRequest(Dice.D20, actions)
            is D2Result -> checkDiceRequest(Dice.D2, actions)
            is D3Result -> checkDiceRequest(Dice.D3, actions)
            is D4Result -> checkDiceRequest(Dice.D4, actions)
            is D6Result -> checkDiceRequest(Dice.D6, actions)
            is D8Result -> checkDiceRequest(Dice.D8, actions)
            is DBlockResult -> checkDiceRequest(Dice.BLOCK, actions)
            is DirectionSelected -> {
                actions.singleInstanceOfOrNull<SelectDirection>()?.directions.orEmpty().contains(action.direction)
            }
            DogoutSelected -> actions.contains(SelectDogout)
            EndAction -> actions.contains(EndActionWhenReady)
            EndSetup -> actions.contains(EndSetupWhenReady)
            EndTurn -> actions.contains(EndTurnWhenReady)
            is FieldSquareSelected -> {
                actions.singleInstanceOfOrNull<SelectFieldLocation>()?.squares.orEmpty().any {
                    it.x == action.x && it.y == action.y
                }
            }
            is ForegoActivationSelected -> {
                actions.any { it is SelectForgoActivation && it.players.contains(action.player) }
            }
            is InducementSelected -> TODO()
            is MoveTypeSelected -> {
                actions.singleInstanceOfOrNull<SelectMoveType>()?.types.orEmpty().contains(action.moveType)
            }
            is NoRerollSelected -> {
                actions.singleInstanceOfOrNull<SelectNoReroll>()?.dicePoolId == action.dicePoolId
            }
            is PlayerActionSelected -> {
                actions.singleInstanceOfOrNull<SelectPlayerAction>()?.actions.orEmpty().any { it.type == action.action }
            }
            is PlayerDeselected -> {
                actions.singleInstanceOfOrNull<DeselectPlayer>()?.players.orEmpty().any { it.id == action.playerId }
            }
            is PlayerSelected -> {
                actions.singleInstanceOfOrNull<SelectPlayer>()?.players.orEmpty().contains(action.playerId)
            }
            is RandomPlayersSelected -> {
                actions.singleInstanceOfOrNull<SelectRandomPlayers>()?.let { actionRequest ->
                    if (actionRequest.players.size < action.players.size) return false
                    action.players.all { player ->
                        actionRequest.players.contains(player)
                    }
                } ?: false
            }
            is RerollOptionSelected -> {
                actions.singleInstanceOfOrNull<SelectRerollOption>()?.options.orEmpty().contains(action.option)
            }
            Revert -> (id.value >= 1)
            is SkillSelected -> {
                actions.singleInstanceOfOrNull<SelectSkill>()?.skills.orEmpty().contains(action.skill)
            }
            Undo -> (id.value >= 1)
            is PlayersSelected -> {
                actions.singleInstanceOfOrNull<SelectPlayers>()?.players?.containsAll(action.players) ?: false
            }

            is DevModeGameAction -> false // Dev Actions should never be handled here
        }
    }

    private fun checkDiceRequest(type: Dice, actions: List<GameActionDescriptor>): Boolean {
        for (descriptor in actions) {
            if (descriptor is RollDice) {
                if (descriptor.dice.size != 1) return false
                if (descriptor.dice.first() != type) return false
                return true
            }
        }
        return false
    }
}
