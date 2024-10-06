package com.jervisffb.fumbbl.net

import com.jervisffb.fumbbl.net.model.Game
import com.jervisffb.fumbbl.net.model.InducementSet
import com.jervisffb.fumbbl.net.model.PlayerResult
import com.jervisffb.fumbbl.net.model.TeamResult
import com.jervisffb.fumbbl.net.model.TurnData
import com.jervisffb.fumbbl.net.model.change.ActingPlayerMarkSkillUnused
import com.jervisffb.fumbbl.net.model.change.ActingPlayerMarkSkillUsed
import com.jervisffb.fumbbl.net.model.change.ActingPlayerSetCurrentMove
import com.jervisffb.fumbbl.net.model.change.ActingPlayerSetDodging
import com.jervisffb.fumbbl.net.model.change.ActingPlayerSetGoingForIt
import com.jervisffb.fumbbl.net.model.change.ActingPlayerSetHasBlocked
import com.jervisffb.fumbbl.net.model.change.ActingPlayerSetHasFed
import com.jervisffb.fumbbl.net.model.change.ActingPlayerSetHasFouled
import com.jervisffb.fumbbl.net.model.change.ActingPlayerSetHasJumped
import com.jervisffb.fumbbl.net.model.change.ActingPlayerSetHasMoved
import com.jervisffb.fumbbl.net.model.change.ActingPlayerSetHasPassed
import com.jervisffb.fumbbl.net.model.change.ActingPlayerSetLeaping
import com.jervisffb.fumbbl.net.model.change.ActingPlayerSetOldPlayerState
import com.jervisffb.fumbbl.net.model.change.ActingPlayerSetPlayerAction
import com.jervisffb.fumbbl.net.model.change.ActingPlayerSetPlayerId
import com.jervisffb.fumbbl.net.model.change.ActingPlayerSetStandingUp
import com.jervisffb.fumbbl.net.model.change.ActingPlayerSetStrength
import com.jervisffb.fumbbl.net.model.change.ActingPlayerSetSufferingAnimosity
import com.jervisffb.fumbbl.net.model.change.ActingPlayerSetSufferingBloodLust
import com.jervisffb.fumbbl.net.model.change.FieldModelAddBloodSpot
import com.jervisffb.fumbbl.net.model.change.FieldModelAddCard
import com.jervisffb.fumbbl.net.model.change.FieldModelAddCardEffect
import com.jervisffb.fumbbl.net.model.change.FieldModelAddDiceDecoration
import com.jervisffb.fumbbl.net.model.change.FieldModelAddMoveSquare
import com.jervisffb.fumbbl.net.model.change.FieldModelAddPlayerMarker
import com.jervisffb.fumbbl.net.model.change.FieldModelAddPrayer
import com.jervisffb.fumbbl.net.model.change.FieldModelAddPushbackSquare
import com.jervisffb.fumbbl.net.model.change.FieldModelAddTrackNumber
import com.jervisffb.fumbbl.net.model.change.FieldModelAddTrapDoor
import com.jervisffb.fumbbl.net.model.change.FieldModelRemoveCard
import com.jervisffb.fumbbl.net.model.change.FieldModelRemoveCardEffect
import com.jervisffb.fumbbl.net.model.change.FieldModelRemoveDiceDecoration
import com.jervisffb.fumbbl.net.model.change.FieldModelRemoveFieldMarker
import com.jervisffb.fumbbl.net.model.change.FieldModelRemoveMoveSquare
import com.jervisffb.fumbbl.net.model.change.FieldModelRemovePlayer
import com.jervisffb.fumbbl.net.model.change.FieldModelRemovePlayerMarker
import com.jervisffb.fumbbl.net.model.change.FieldModelRemovePrayer
import com.jervisffb.fumbbl.net.model.change.FieldModelRemovePushbackSquare
import com.jervisffb.fumbbl.net.model.change.FieldModelRemoveSkillEnhancements
import com.jervisffb.fumbbl.net.model.change.FieldModelRemoveTrackNumber
import com.jervisffb.fumbbl.net.model.change.FieldModelRemoveTrapDoor
import com.jervisffb.fumbbl.net.model.change.FieldModelSetBallCoordinate
import com.jervisffb.fumbbl.net.model.change.FieldModelSetBallInPlay
import com.jervisffb.fumbbl.net.model.change.FieldModelSetBallMoving
import com.jervisffb.fumbbl.net.model.change.FieldModelSetBlitzState
import com.jervisffb.fumbbl.net.model.change.FieldModelSetBombCoordinate
import com.jervisffb.fumbbl.net.model.change.FieldModelSetBombMoving
import com.jervisffb.fumbbl.net.model.change.FieldModelSetPlayerCoordinate
import com.jervisffb.fumbbl.net.model.change.FieldModelSetPlayerState
import com.jervisffb.fumbbl.net.model.change.FieldModelSetRangeRuler
import com.jervisffb.fumbbl.net.model.change.FieldModelSetTargetSelectionState
import com.jervisffb.fumbbl.net.model.change.FieldModelSetWeather
import com.jervisffb.fumbbl.net.model.change.GameSetAdminMode
import com.jervisffb.fumbbl.net.model.change.GameSetConcededLegally
import com.jervisffb.fumbbl.net.model.change.GameSetConcessionPossible
import com.jervisffb.fumbbl.net.model.change.GameSetDefenderAction
import com.jervisffb.fumbbl.net.model.change.GameSetDefenderId
import com.jervisffb.fumbbl.net.model.change.GameSetDialogParameter
import com.jervisffb.fumbbl.net.model.change.GameSetFinished
import com.jervisffb.fumbbl.net.model.change.GameSetHalf
import com.jervisffb.fumbbl.net.model.change.GameSetHomeFirstOffense
import com.jervisffb.fumbbl.net.model.change.GameSetHomePlaying
import com.jervisffb.fumbbl.net.model.change.GameSetId
import com.jervisffb.fumbbl.net.model.change.GameSetLastDefenderId
import com.jervisffb.fumbbl.net.model.change.GameSetLastTurnMode
import com.jervisffb.fumbbl.net.model.change.GameSetPassCoordinate
import com.jervisffb.fumbbl.net.model.change.GameSetScheduled
import com.jervisffb.fumbbl.net.model.change.GameSetSetupOffense
import com.jervisffb.fumbbl.net.model.change.GameSetStarted
import com.jervisffb.fumbbl.net.model.change.GameSetTesting
import com.jervisffb.fumbbl.net.model.change.GameSetThrowerAction
import com.jervisffb.fumbbl.net.model.change.GameSetThrowerId
import com.jervisffb.fumbbl.net.model.change.GameSetTimeoutEnforced
import com.jervisffb.fumbbl.net.model.change.GameSetTimeoutPossible
import com.jervisffb.fumbbl.net.model.change.GameSetTurnMode
import com.jervisffb.fumbbl.net.model.change.GameSetWaitingForOpponent
import com.jervisffb.fumbbl.net.model.change.InducementSetAddPrayer
import com.jervisffb.fumbbl.net.model.change.InducementSetRemovePrayer
import com.jervisffb.fumbbl.net.model.change.ModelChange
import com.jervisffb.fumbbl.net.model.change.PlayerMarkSkillUnused
import com.jervisffb.fumbbl.net.model.change.PlayerMarkSkillUsed
import com.jervisffb.fumbbl.net.model.change.PlayerResultSetBlocks
import com.jervisffb.fumbbl.net.model.change.PlayerResultSetCasualties
import com.jervisffb.fumbbl.net.model.change.PlayerResultSetCasualtiesWithAdditionalSpp
import com.jervisffb.fumbbl.net.model.change.PlayerResultSetCompletions
import com.jervisffb.fumbbl.net.model.change.PlayerResultSetCompletionsWithAdditionalSpp
import com.jervisffb.fumbbl.net.model.change.PlayerResultSetCurrentSpps
import com.jervisffb.fumbbl.net.model.change.PlayerResultSetDefecting
import com.jervisffb.fumbbl.net.model.change.PlayerResultSetDeflections
import com.jervisffb.fumbbl.net.model.change.PlayerResultSetFouls
import com.jervisffb.fumbbl.net.model.change.PlayerResultSetHasUsedSecretWeapon
import com.jervisffb.fumbbl.net.model.change.PlayerResultSetInterceptions
import com.jervisffb.fumbbl.net.model.change.PlayerResultSetPassing
import com.jervisffb.fumbbl.net.model.change.PlayerResultSetPlayerAwards
import com.jervisffb.fumbbl.net.model.change.PlayerResultSetRushing
import com.jervisffb.fumbbl.net.model.change.PlayerResultSetSendToBoxByPlayerId
import com.jervisffb.fumbbl.net.model.change.PlayerResultSetSendToBoxHalf
import com.jervisffb.fumbbl.net.model.change.PlayerResultSetSendToBoxReason
import com.jervisffb.fumbbl.net.model.change.PlayerResultSetSendToBoxTurn
import com.jervisffb.fumbbl.net.model.change.PlayerResultSetSeriousInjury
import com.jervisffb.fumbbl.net.model.change.PlayerResultSetSeriousInjuryDecay
import com.jervisffb.fumbbl.net.model.change.PlayerResultSetTouchdowns
import com.jervisffb.fumbbl.net.model.change.PlayerResultSetTurnsPlayed
import com.jervisffb.fumbbl.net.model.change.TeamResultDedicatedFansModifier
import com.jervisffb.fumbbl.net.model.change.TeamResultSetBadlyHurtSuffered
import com.jervisffb.fumbbl.net.model.change.TeamResultSetConceded
import com.jervisffb.fumbbl.net.model.change.TeamResultSetFame
import com.jervisffb.fumbbl.net.model.change.TeamResultSetFanFactor
import com.jervisffb.fumbbl.net.model.change.TeamResultSetFanFactorModifier
import com.jervisffb.fumbbl.net.model.change.TeamResultSetPenaltyScore
import com.jervisffb.fumbbl.net.model.change.TeamResultSetPettyCashTransferred
import com.jervisffb.fumbbl.net.model.change.TeamResultSetPettyCashUsed
import com.jervisffb.fumbbl.net.model.change.TeamResultSetRaisedDead
import com.jervisffb.fumbbl.net.model.change.TeamResultSetRipSuffered
import com.jervisffb.fumbbl.net.model.change.TeamResultSetScore
import com.jervisffb.fumbbl.net.model.change.TeamResultSetSeriousInjurySuffered
import com.jervisffb.fumbbl.net.model.change.TeamResultSetSpectators
import com.jervisffb.fumbbl.net.model.change.TeamResultSetSpirallingExpenses
import com.jervisffb.fumbbl.net.model.change.TeamResultSetTeamValue
import com.jervisffb.fumbbl.net.model.change.TeamResultSetWinnings
import com.jervisffb.fumbbl.net.model.change.TurnDataSetApothecaries
import com.jervisffb.fumbbl.net.model.change.TurnDataSetBlitzUsed
import com.jervisffb.fumbbl.net.model.change.TurnDataSetBombUsed
import com.jervisffb.fumbbl.net.model.change.TurnDataSetCoachBanned
import com.jervisffb.fumbbl.net.model.change.TurnDataSetFirstTurnAfterKickoff
import com.jervisffb.fumbbl.net.model.change.TurnDataSetFoulUsed
import com.jervisffb.fumbbl.net.model.change.TurnDataSetHandOverUsed
import com.jervisffb.fumbbl.net.model.change.TurnDataSetKtmUsed
import com.jervisffb.fumbbl.net.model.change.TurnDataSetLeaderState
import com.jervisffb.fumbbl.net.model.change.TurnDataSetPassUsed
import com.jervisffb.fumbbl.net.model.change.TurnDataSetPlagueDoctors
import com.jervisffb.fumbbl.net.model.change.TurnDataSetReRollUsed
import com.jervisffb.fumbbl.net.model.change.TurnDataSetReRolls
import com.jervisffb.fumbbl.net.model.change.TurnDataSetReRollsBrilliantCoachingOneDrive
import com.jervisffb.fumbbl.net.model.change.TurnDataSetReRollsPumpUpTheCrowdOneDrive
import com.jervisffb.fumbbl.net.model.change.TurnDataSetReRollsSingleUse
import com.jervisffb.fumbbl.net.model.change.TurnDataSetTurnNr
import com.jervisffb.fumbbl.net.model.change.TurnDataSetTurnStarted
import com.jervisffb.fumbbl.net.model.change.TurnDataSetWanderingApothecaries
import com.jervisffb.fumbbl.net.model.change.isHomeData

object ModelChangeProcessor {
    fun apply(
        game: Game,
        change: ModelChange,
    ): Boolean {
//        val dialogParameter: IDialogParameter
//        val skillFactory: SkillFactory = pGame.getFactory(FactoryType.Factory.SKILL) as SkillFactory
        when (change) {
            is ActingPlayerMarkSkillUnused -> game.actingPlayer.markSkillUnused(change.value)
            is ActingPlayerMarkSkillUsed -> game.actingPlayer.markSkillUsed(change.value)
            is ActingPlayerSetCurrentMove -> game.actingPlayer.currentMove = change.value
            is ActingPlayerSetDodging -> game.actingPlayer.dodging = change.value
            is ActingPlayerSetGoingForIt -> game.actingPlayer.goingForIt = change.value
            is ActingPlayerSetHasBlocked -> game.actingPlayer.hasBlocked = change.value
            is ActingPlayerSetHasFed -> game.actingPlayer.hasFed = change.value
            is ActingPlayerSetHasFouled -> game.actingPlayer.hasFouled = change.value
            is ActingPlayerSetHasJumped -> game.actingPlayer.hasJumped = change.value
            is ActingPlayerSetHasMoved -> game.actingPlayer.hasMoved = change.value
            is ActingPlayerSetHasPassed -> game.actingPlayer.hasPassed = change.value
            is ActingPlayerSetLeaping -> game.actingPlayer.jumping = change.value
            is ActingPlayerSetOldPlayerState -> game.actingPlayer.playerStateOld = change.value
            is ActingPlayerSetPlayerAction -> game.actingPlayer.playerAction = change.value
            is ActingPlayerSetPlayerId -> game.actingPlayer.updatePlayerId(change.value)
            is ActingPlayerSetStandingUp -> game.actingPlayer.standingUp = change.value
            is ActingPlayerSetStrength -> game.actingPlayer.strenght = change.value
            is ActingPlayerSetSufferingAnimosity -> game.actingPlayer.sufferingAnimosity = change.value
            is ActingPlayerSetSufferingBloodLust -> game.actingPlayer.sufferingBloodlust = change.value
            is FieldModelAddBloodSpot -> game.fieldModel.addBloodSpot(change.value)
            is FieldModelAddCard -> game.fieldModel.addCard(change.key, change.value)
            is FieldModelAddCardEffect -> game.fieldModel.addCardEffect(change.key, change.value)
            is FieldModelAddDiceDecoration -> game.fieldModel.addDiceDecoration(change.value)
//            is FieldModelAddFieldMarker -> TODO()
//            is FieldModelAddIntensiveTraining -> TODO()
            is FieldModelAddMoveSquare -> game.fieldModel.addMoveSquare(change.value)
            is FieldModelAddPlayerMarker -> game.fieldModel.addPlayerMarker(change.value)
            is FieldModelAddPrayer -> game.fieldModel.addPrayerEnhancements(change.key, change.value)
            is FieldModelAddPushbackSquare -> game.fieldModel.addPushBackSquare(change.value)
//            is FieldModelAddSkillEnhancements -> TODO()
            is FieldModelAddTrackNumber -> game.fieldModel.addTrackNumber(change.value)
            is FieldModelAddTrapDoor -> game.fieldModel.addTrapDoor(change.value)
//            is FieldModelAddWisdom -> TODO()
//            is FieldModelKeepDeactivatedCard -> TODO()
            is FieldModelRemoveCard -> game.fieldModel.removeCard(change.key, change.value)
            is FieldModelRemoveCardEffect -> game.fieldModel.removeCardEffect(change.key, change.value)
            is FieldModelRemoveDiceDecoration -> game.fieldModel.removeDiceDecoration(change.value)
            is FieldModelRemoveFieldMarker -> game.fieldModel.removeFieldMarker(change.value)
            is FieldModelRemoveMoveSquare -> game.fieldModel.removeMoveSquare(change.value)
            is FieldModelRemovePlayer -> game.fieldModel.removePlayer(change.key)
            is FieldModelRemovePlayerMarker -> game.fieldModel.removePlayerMarker(change.value)
            is FieldModelRemovePrayer -> game.fieldModel.removePrayerEnhancement(change.key, change.value)
            is FieldModelRemovePushbackSquare -> game.fieldModel.removePushbackSquare(change.value)
            is FieldModelRemoveSkillEnhancements ->
                game.fieldModel.removeSkillEnhancements(
                    game.getPlayerById(change.key)!!,
                    change.value,
                )
            is FieldModelRemoveTrackNumber -> game.fieldModel.removeTrackNumber(change.value)
            is FieldModelRemoveTrapDoor -> game.fieldModel.removeTrapDoor(change.value)
            is FieldModelSetBallCoordinate -> game.fieldModel.ballCoordinate = change.value
            is FieldModelSetBallInPlay -> game.fieldModel.ballInPlay = change.value
            is FieldModelSetBallMoving -> game.fieldModel.ballMoving = change.value
            is FieldModelSetBlitzState -> game.fieldModel.targetSelectionState = change.value
            is FieldModelSetBombCoordinate -> game.fieldModel.bombCoordinate = change.value
            is FieldModelSetBombMoving -> game.fieldModel.bombMoving = change.value
            is FieldModelSetPlayerCoordinate -> game.fieldModel.setPlayerCoordinate(change.key, change.value!!)
            is FieldModelSetPlayerState -> game.fieldModel.setPlayerState(change.key, change.value)
            is FieldModelSetRangeRuler -> game.fieldModel.rangeRuler = change.value
            is FieldModelSetTargetSelectionState -> game.fieldModel.targetSelectionState = change.value
            is FieldModelSetWeather -> game.fieldModel.weather = change.value
//            is GameOptionsAddOption -> TODO()
            is GameSetAdminMode -> game.adminMode = change.value
            is GameSetConcededLegally -> game.concededLegally = change.value
            is GameSetConcessionPossible -> game.concessionPossible = change.value
            is GameSetDefenderAction -> game.defenderAction = change.value
            is GameSetDefenderId -> game.defenderId = change.value
            is GameSetDialogParameter -> game.dialogParameter = change.value
            is GameSetFinished -> game.finished = change.value
            is GameSetHalf -> game.half = change.value
            is GameSetHomeFirstOffense -> game.homeFirstOffense = change.value
            is GameSetHomePlaying -> game.homePlaying = change.value
            is GameSetId -> game.gameId = change.value
            is GameSetLastDefenderId -> game.lastDefenderId = change.value
            is GameSetLastTurnMode -> game.lastTurnMode = change.value
            is GameSetPassCoordinate -> game.passCoordinate = change.value
            is GameSetScheduled -> game.scheduled = change.value
            is GameSetSetupOffense -> game.setupOffense = change.value
            is GameSetStarted -> game.started = change.value
            is GameSetTesting -> game.testing = change.value
            is GameSetThrowerAction -> game.throwerAction = change.value
            is GameSetThrowerId -> game.throwerId = change.value
            is GameSetTimeoutEnforced -> game.timeoutEnforced = change.value
            is GameSetTimeoutPossible -> game.timeoutPossible = change.value
            is GameSetTurnMode -> game.turnMode = change.value
            is GameSetWaitingForOpponent -> game.waitingForOpponent = change.value
//            is InducementSetActivateCard -> TODO()
//            is InducementSetAddAvailableCard -> TODO()
//            is InducementSetAddInducement -> TODO()
            is InducementSetAddPrayer -> getInducementSet(game, change.isHomeData()).prayers.add(change.value)
//            is InducementSetCardChoices -> TODO()
//            is InducementSetDeactivateCard -> TODO()
//            is InducementSetRemoveAvailableCard -> TODO()
//            is InducementSetRemoveInducement -> TODO()
            is InducementSetRemovePrayer -> getInducementSet(game, change.isHomeData()).prayers.remove(change.value)
            is PlayerMarkSkillUnused -> game.getPlayerById(change.key)!!.markUnused(change.value, game)
            is PlayerMarkSkillUsed -> game.getPlayerById(change.key)!!.markUsed(change.value, game)
            is PlayerResultSetBlocks -> getPlayerResult(game, change.key).blocks = change.value
            is PlayerResultSetCasualties -> getPlayerResult(game, change.key).casualties = change.value
            is PlayerResultSetCasualtiesWithAdditionalSpp -> getPlayerResult(game, change.key).casualtiesWithAdditionalSpp = change.value
            is PlayerResultSetCompletions -> getPlayerResult(game, change.key).completions = change.value
            is PlayerResultSetCompletionsWithAdditionalSpp -> getPlayerResult(game, change.key).completionsWithAdditionalSpp = change.value
            is PlayerResultSetCurrentSpps -> getPlayerResult(game, change.key).currentSpps = change.value
            is PlayerResultSetDefecting -> getPlayerResult(game, change.key).defecting = change.value
            is PlayerResultSetDeflections -> getPlayerResult(game, change.key).deflections = change.value
            is PlayerResultSetFouls -> getPlayerResult(game, change.key).fouls = change.value
            is PlayerResultSetHasUsedSecretWeapon -> getPlayerResult(game, change.key).hasUsedSecretWeapon = change.value
            is PlayerResultSetInterceptions -> getPlayerResult(game, change.key).interceptions = change.value
            is PlayerResultSetPassing -> getPlayerResult(game, change.key).passing = change.value
            is PlayerResultSetPlayerAwards -> getPlayerResult(game, change.key).playerAwards = change.value
            is PlayerResultSetRushing -> getPlayerResult(game, change.key).rushing = change.value
            is PlayerResultSetSendToBoxByPlayerId -> getPlayerResult(game, change.key).sendToBoxByPlayerId = change.value
            is PlayerResultSetSendToBoxHalf -> getPlayerResult(game, change.key).sendToBoxHalf = change.value
            is PlayerResultSetSendToBoxReason -> getPlayerResult(game, change.key).sendToBoxReason = change.value
            is PlayerResultSetSendToBoxTurn -> getPlayerResult(game, change.key).sendToBoxTurn = change.value
            is PlayerResultSetSeriousInjury -> getPlayerResult(game, change.key).seriousInjury = change.value
            is PlayerResultSetSeriousInjuryDecay -> getPlayerResult(game, change.key).seriousInjuryDecay = change.value
            is PlayerResultSetTouchdowns -> getPlayerResult(game, change.key).touchdowns = change.value
            is PlayerResultSetTurnsPlayed -> getPlayerResult(game, change.key).turnsPlayed = change.value
            is TeamResultDedicatedFansModifier -> getTeamResult(game, change.isHomeData()).dedicatedFans = change.value
            is TeamResultSetBadlyHurtSuffered -> getTeamResult(game, change.isHomeData()).badlyHurtSuffered = change.value
            is TeamResultSetConceded -> getTeamResult(game, change.isHomeData()).conceded = change.value
            is TeamResultSetFame -> getTeamResult(game, change.isHomeData()).fame = change.value
            is TeamResultSetFanFactor -> getTeamResult(game, change.isHomeData()).fanFactorModifier = change.value
            is TeamResultSetFanFactorModifier -> getTeamResult(game, change.isHomeData()).fanFactorModifier = change.value
            is TeamResultSetPenaltyScore -> getTeamResult(game, change.isHomeData()).penaltyScore = change.value
            is TeamResultSetPettyCashTransferred -> getTeamResult(game, change.isHomeData()).pettyCashTransferred = change.value
            is TeamResultSetPettyCashUsed -> getTeamResult(game, change.isHomeData()).pettyCashUsed = change.value
            is TeamResultSetRaisedDead -> getTeamResult(game, change.isHomeData()).raisedDead = change.value
            is TeamResultSetRipSuffered -> getTeamResult(game, change.isHomeData()).ripSuffered = change.value
            is TeamResultSetScore -> getTeamResult(game, change.isHomeData()).score = change.value
            is TeamResultSetSeriousInjurySuffered -> getTeamResult(game, change.isHomeData()).seriousInjurySuffered = change.value
            is TeamResultSetSpectators -> getTeamResult(game, change.isHomeData()).spectators = change.value
            is TeamResultSetSpirallingExpenses -> getTeamResult(game, change.isHomeData()).spirallingExpenses = change.value
            is TeamResultSetTeamValue -> getTeamResult(game, change.isHomeData()).teamValue = change.value
            is TeamResultSetWinnings -> getTeamResult(game, change.isHomeData()).winnings = change.value
            is TurnDataSetApothecaries -> getTurnData(game, change.isHomeData()).apothecaries = change.value
            is TurnDataSetBlitzUsed -> getTurnData(game, change.isHomeData()).blitzUsed = change.value
            is TurnDataSetBombUsed -> getTurnData(game, change.isHomeData()).bombUsed = change.value
            is TurnDataSetCoachBanned -> getTurnData(game, change.isHomeData()).coachBanned = change.value
            is TurnDataSetFirstTurnAfterKickoff -> getTurnData(game, change.isHomeData()).firstTurnAfterKickoff = change.value
            is TurnDataSetFoulUsed -> getTurnData(game, change.isHomeData()).foulUsed = change.value
            is TurnDataSetHandOverUsed -> getTurnData(game, change.isHomeData()).handOverUsed = change.value
            is TurnDataSetKtmUsed -> getTurnData(game, change.isHomeData()).ktmUsed = change.value
            is TurnDataSetLeaderState -> getTurnData(game, change.isHomeData()).leaderState = change.value
            is TurnDataSetPassUsed -> getTurnData(game, change.isHomeData()).passUsed = change.value
            is TurnDataSetPlagueDoctors -> getTurnData(game, change.isHomeData()).plagueDoctors = change.value
            is TurnDataSetReRollUsed -> getTurnData(game, change.isHomeData()).reRollUsed = change.value
            is TurnDataSetReRolls -> getTurnData(game, change.isHomeData()).reRolls = change.value
            is TurnDataSetReRollsBrilliantCoachingOneDrive -> getTurnData(game, change.isHomeData()).rerollBrilliantCoachingOneDrive = change.value
            is TurnDataSetReRollsPumpUpTheCrowdOneDrive -> getTurnData(game, change.isHomeData()).rerollPumpUpTheCrowdOneDrive = change.value
            is TurnDataSetReRollsSingleUse -> getTurnData(game, change.isHomeData()).singleUseReRolls = change.value
            is TurnDataSetTurnNr -> getTurnData(game, change.isHomeData()).turnNr = change.value
            is TurnDataSetTurnStarted -> getTurnData(game, change.isHomeData()).turnStarted = change.value
            is TurnDataSetWanderingApothecaries -> getTurnData(game, change.isHomeData()).wanderingApothecaries = change.value
            else -> {
                println("Ignore $change")
                return false
            }
        }
        return true

//        when (pModelChange.modelChangeId) {
//            FIELD_MODEL_ADD_CARD -> {
//                pGame.fieldModel.addCard(pGame.getPlayerById(pModelChange.getKey()), pModelChange.getValue() as Card?)
//                return true
//            }
//
//            FIELD_MODEL_ADD_CARD_EFFECT -> {
//                pGame.fieldModel.addCardEffect(
//                    pGame.getPlayerById(pModelChange.getKey()), pModelChange
//                        .getValue() as CardEffect?
//                )
//                return true
//            }
//
//            FIELD_MODEL_ADD_DICE_DECORATION -> {
//                pGame.fieldModel.add(pModelChange.getValue() as DiceDecoration?)
//                return true
//            }
//
//            FIELD_MODEL_ADD_INTENSIVE_TRAINING -> {
//                pGame.fieldModel.addIntensiveTrainingSkill(pModelChange.getKey(), pModelChange.getValue() as Skill?)
//                return true
//            }
//
//            FIELD_MODEL_ADD_FIELD_MARKER -> {
//                pGame.fieldModel.add(pModelChange.getValue() as FieldMarker?)
//                return true
//            }
//
//            FIELD_MODEL_ADD_MOVE_SQUARE -> {
//                pGame.fieldModel.add(pModelChange.getValue() as MoveSquare?)
//                return true
//            }
//
//            FIELD_MODEL_ADD_PLAYER_MARKER -> {
//                pGame.fieldModel.add(pModelChange.getValue() as PlayerMarker?)
//                return true
//            }
//
//            FIELD_MODEL_ADD_PRAYER -> {
//                pGame.fieldModel.addPrayerEnhancements(
//                    pGame.getPlayerById(pModelChange.getKey()),
//                    Prayer.valueOf(pModelChange.getValue() as String?)
//                )
//                return true
//            }
//
//            FIELD_MODEL_ADD_PUSHBACK_SQUARE -> {
//                pGame.fieldModel.add(pModelChange.getValue() as PushbackSquare?)
//                return true
//            }
//
//            FIELD_MODEL_ADD_SKILL_ENHANCEMENTS -> {
//                pGame.fieldModel.addSkillEnhancements(
//                    pGame.getPlayerById(pModelChange.getKey()),
//                    skillFactory.forName(pModelChange.getValue() as String?)
//                )
//                return true
//            }
//
//            FIELD_MODEL_ADD_TRACK_NUMBER -> {
//                pGame.fieldModel.add(pModelChange.getValue() as TrackNumber?)
//                return true
//            }
//
//            FIELD_MODEL_ADD_TRAP_DOOR -> {
//                pGame.fieldModel.add(pModelChange.getValue() as TrapDoor?)
//                return true
//            }
//
//            FIELD_MODEL_ADD_WISDOM -> {
//                Constant.getGrantAbleSkills(skillFactory).stream()
//                    .filter { swv -> swv.getSkill().equals(pModelChange.getValue()) }
//                    .findFirst().ifPresent { swv -> pGame.fieldModel.addWisdomSkill(pModelChange.getKey(), swv) }
//
//
//                return true
//            }
//
//            FIELD_MODEL_KEEP_DEACTIVATED_CARD -> {
//                pGame.fieldModel.keepDeactivatedCard(
//                    pGame.getPlayerById(pModelChange.getKey()),
//                    pModelChange.getValue() as Card?
//                )
//                return true
//            }
//
//            FIELD_MODEL_REMOVE_CARD -> {
//                pGame.fieldModel.removeCard(
//                    pGame.getPlayerById(pModelChange.getKey()),
//                    pModelChange.getValue() as Card?
//                )
//                return true
//            }
//
//            FIELD_MODEL_REMOVE_CARD_EFFECT -> {
//                pGame.fieldModel.removeCardEffect(
//                    pGame.getPlayerById(pModelChange.getKey()), pModelChange
//                        .getValue() as CardEffect?
//                )
//                return true
//            }
//
//            FIELD_MODEL_REMOVE_DICE_DECORATION -> {
//                pGame.fieldModel.remove(pModelChange.getValue() as DiceDecoration?)
//                return true
//            }
//
//            FIELD_MODEL_REMOVE_FIELD_MARKER -> {
//                pGame.fieldModel.remove(pModelChange.getValue() as FieldMarker?)
//                return true
//            }
//
//            FIELD_MODEL_REMOVE_MOVE_SQUARE -> {
//                pGame.fieldModel.remove(pModelChange.getValue() as MoveSquare?)
//                return true
//            }
//
//            FIELD_MODEL_REMOVE_PLAYER -> {
//                pGame.fieldModel.remove(pGame.getPlayerById(pModelChange.getKey()))
//                return true
//            }
//
//            FIELD_MODEL_REMOVE_PLAYER_MARKER -> {
//                pGame.fieldModel.remove(pModelChange.getValue() as PlayerMarker?)
//                return true
//            }
//
//            FIELD_MODEL_REMOVE_PRAYER -> {
//                pGame.fieldModel.removePrayerEnhancements(
//                    pGame.getPlayerById(pModelChange.getKey()),
//                    Prayer.valueOf(pModelChange.getValue() as String?)
//                )
//                return true
//            }
//
//            FIELD_MODEL_REMOVE_PUSHBACK_SQUARE -> {
//                pGame.fieldModel.remove(pModelChange.getValue() as PushbackSquare?)
//                return true
//            }
//
//            FIELD_MODEL_REMOVE_TRACK_NUMBER -> {
//                pGame.fieldModel.remove(pModelChange.getValue() as TrackNumber?)
//                return true
//            }
//
//            FIELD_MODEL_REMOVE_TRAP_DOOR -> {
//                pGame.fieldModel.remove(pModelChange.getValue() as TrapDoor?)
//                return true
//            }
//
//            FIELD_MODEL_SET_BALL_COORDINATE -> {
//                pGame.fieldModel.setBallCoordinate(pModelChange.getValue() as FieldCoordinate?)
//                return true
//            }
//
//            FIELD_MODEL_SET_BALL_IN_PLAY -> {
//                pGame.fieldModel.setBallInPlay((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            FIELD_MODEL_SET_BALL_MOVING -> {
//                pGame.fieldModel.setBallMoving((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            FIELD_MODEL_SET_BLITZ_STATE, FIELD_MODEL_SET_TARGET_SELECTION_STATE -> {
//                pGame.fieldModel.setTargetSelectionState(pModelChange.getValue() as TargetSelectionState?)
//                return true
//            }
//
//            FIELD_MODEL_SET_BOMB_COORDINATE -> {
//                pGame.fieldModel.setBombCoordinate(pModelChange.getValue() as FieldCoordinate?)
//                return true
//            }
//
//            FIELD_MODEL_SET_BOMB_MOVING -> {
//                pGame.fieldModel.setBombMoving((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            FIELD_MODEL_SET_RANGE_RULER -> {
//                pGame.fieldModel.setRangeRuler(pModelChange.getValue() as RangeRuler?)
//                return true
//            }
//
//            FIELD_MODEL_SET_WEATHER -> {
//                pGame.fieldModel.setWeather(pModelChange.getValue() as Weather?)
//                return true
//            }
//
//            GAME_SET_CONCEDED_LEGALLY -> {
//                pGame.setConcededLegally((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            GAME_SET_DEFENDER_ACTION -> {
//                pGame.setDefenderAction(pModelChange.getValue() as PlayerAction?)
//                return true
//            }
//
//            GAME_SET_DEFENDER_ID -> {
//                pGame.setDefenderId(pModelChange.getKey())
//                return true
//            }
//
//            GAME_SET_FINISHED -> {
//                pGame.setFinished(pModelChange.getValue() as Date?)
//                return true
//            }
//
//            GAME_SET_HALF -> {
//                pGame.setHalf((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            GAME_SET_HOME_FIRST_OFFENSE -> {
//                pGame.setHomeFirstOffense((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            GAME_SET_HOME_PLAYING -> {
//                pGame.setHomePlaying((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            GAME_SET_ID -> {
//                pGame.setId((pModelChange.getValue() as Long?))
//                return true
//            }
//
//            GAME_SET_LAST_DEFENDER_ID -> {
//                pGame.setLastDefenderId(pModelChange.getKey())
//                return true
//            }
//
//            GAME_SET_PASS_COORDINATE -> {
//                pGame.setPassCoordinate(pModelChange.getValue() as FieldCoordinate?)
//                return true
//            }
//
//            GAME_SET_SCHEDULED -> {
//                pGame.setScheduled(pModelChange.getValue() as Date?)
//                return true
//            }
//
//            GAME_SET_TESTING -> {
//                pGame.setTesting((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            GAME_SET_ADMIN_MODE -> {
//                pGame.setAdminMode((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            GAME_SET_THROWER_ID -> {
//                pGame.setThrowerId(pModelChange.getKey())
//                return true
//            }
//
//            GAME_SET_THROWER_ACTION -> {
//                pGame.setThrowerAction(pModelChange.getValue() as PlayerAction?)
//                return true
//            }
//
//            GAME_SET_TIMEOUT_ENFORCED -> {
//                pGame.setTimeoutEnforced((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            GAME_SET_TIMEOUT_POSSIBLE -> {
//                pGame.setTimeoutPossible((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            GAME_SET_TURN_MODE -> {
//                pGame.setTurnMode(pModelChange.getValue() as TurnMode?)
//                return true
//            }
//
//            GAME_SET_WAITING_FOR_OPPONENT -> {
//                pGame.setWaitingForOpponent((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            GAME_OPTIONS_ADD_OPTION -> {
//                pGame.getOptions().addOption(pModelChange.getValue() as IGameOption?)
//                return true
//            }
//
//            INDUCEMENT_SET_ACTIVATE_CARD -> {
//                getInducementSet(pGame, isHomeData(pModelChange)).activateCard(pModelChange.getValue() as Card?)
//                return true
//            }
//
//            INDUCEMENT_SET_ADD_AVAILABLE_CARD -> {
//                getInducementSet(pGame, isHomeData(pModelChange)).addAvailableCard(pModelChange.getValue() as Card?)
//                return true
//            }
//
//            INDUCEMENT_SET_ADD_INDUCEMENT -> {
//                getInducementSet(pGame, isHomeData(pModelChange)).addInducement(pModelChange.getValue() as Inducement?)
//                return true
//            }
//
//            INDUCEMENT_SET_ADD_PRAYER -> {
//                getInducementSet(pGame, isHomeData(pModelChange)).addPrayer(pModelChange.getValue() as Prayer?)
//                return true
//            }
//
//            INDUCEMENT_SET_CARD_CHOICES -> {
//                dialogParameter = pGame.dialogParameter
//                if (dialogParameter is DialogBuyCardsAndInducementsParameter) {
//                    (dialogParameter as DialogBuyCardsAndInducementsParameter).setCardChoices(pModelChange.getValue() as CardChoices?)
//                }
//                return true
//            }
//
//            INDUCEMENT_SET_DEACTIVATE_CARD -> {
//                getInducementSet(pGame, isHomeData(pModelChange)).deactivateCard(pModelChange.getValue() as Card?)
//                return true
//            }
//
//            INDUCEMENT_SET_REMOVE_AVAILABLE_CARD -> {
//                getInducementSet(pGame, isHomeData(pModelChange)).removeAvailableCard(pModelChange.getValue() as Card?)
//                return true
//            }
//
//            INDUCEMENT_SET_REMOVE_INDUCEMENT -> {
//                getInducementSet(
//                    pGame,
//                    isHomeData(pModelChange)
//                ).removeInducement(pModelChange.getValue() as Inducement?)
//                return true
//            }
//
//            INDUCEMENT_SET_REMOVE_PRAYER -> {
//                getInducementSet(pGame, isHomeData(pModelChange)).removePrayer(pModelChange.getValue() as Prayer?)
//                return true
//            }
//
//            PLAYER_MARK_SKILL_USED -> {
//                pGame.getPlayerById(pModelChange.getKey()).markUsed(pModelChange.getValue() as Skill?, pGame)
//                return true
//            }
//
//            PLAYER_MARK_SKILL_UNUSED -> {
//                pGame.getPlayerById(pModelChange.getKey()).markUnused(pModelChange.getValue() as Skill?, pGame)
//                return true
//            }
//
//            PLAYER_RESULT_SET_BLOCKS -> {
//                getPlayerResult(pGame, pModelChange.getKey()).setBlocks((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_CASUALTIES -> {
//                getPlayerResult(pGame, pModelChange.getKey()).setCasualties((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_CASUALTIES_WITH_ADDITIONAL_SPP -> {
//                getPlayerResult(
//                    pGame,
//                    pModelChange.getKey()
//                ).setCasualtiesWithAdditionalSpp((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_COMPLETIONS -> {
//                getPlayerResult(pGame, pModelChange.getKey()).setCompletions((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_COMPLETIONS_WITH_ADDITIONAL_SPP -> {
//                getPlayerResult(
//                    pGame,
//                    pModelChange.getKey()
//                ).setCompletionsWithAdditionalSpp((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_CURRENT_SPPS -> {
//                getPlayerResult(pGame, pModelChange.getKey()).setCurrentSpps((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_DEFECTING -> {
//                getPlayerResult(pGame, pModelChange.getKey()).setDefecting((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_FOULS -> {
//                getPlayerResult(pGame, pModelChange.getKey()).setFouls((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_HAS_USED_SECRET_WEAPON -> {
//                getPlayerResult(
//                    pGame,
//                    pModelChange.getKey()
//                ).setHasUsedSecretWeapon((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_INTERCEPTIONS -> {
//                getPlayerResult(pGame, pModelChange.getKey()).setInterceptions((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_DEFLECTIONS -> {
//                getPlayerResult(pGame, pModelChange.getKey()).setDeflections((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_PASSING -> {
//                getPlayerResult(pGame, pModelChange.getKey()).setPassing((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_PLAYER_AWARDS -> {
//                getPlayerResult(pGame, pModelChange.getKey()).setPlayerAwards((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_RUSHING -> {
//                getPlayerResult(pGame, pModelChange.getKey()).setRushing((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_SEND_TO_BOX_BY_PLAYER_ID -> {
//                getPlayerResult(pGame, pModelChange.getKey()).setSendToBoxByPlayerId(pModelChange.getValue() as String?)
//                return true
//            }
//
//            PLAYER_RESULT_SET_SEND_TO_BOX_HALF -> {
//                getPlayerResult(pGame, pModelChange.getKey()).setSendToBoxHalf((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_SEND_TO_BOX_REASON -> {
//                getPlayerResult(
//                    pGame,
//                    pModelChange.getKey()
//                ).setSendToBoxReason(pModelChange.getValue() as SendToBoxReason?)
//                return true
//            }
//
//            PLAYER_RESULT_SET_SEND_TO_BOX_TURN -> {
//                getPlayerResult(pGame, pModelChange.getKey()).setSendToBoxTurn((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_SERIOUS_INJURY -> {
//                getPlayerResult(
//                    pGame,
//                    pModelChange.getKey()
//                ).setSeriousInjury(pModelChange.getValue() as SeriousInjury?)
//                return true
//            }
//
//            PLAYER_RESULT_SET_SERIOUS_INJURY_DECAY -> {
//                getPlayerResult(
//                    pGame,
//                    pModelChange.getKey()
//                ).setSeriousInjuryDecay(pModelChange.getValue() as SeriousInjury?)
//                return true
//            }
//
//            PLAYER_RESULT_SET_TOUCHDOWNS -> {
//                getPlayerResult(pGame, pModelChange.getKey()).setTouchdowns((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_TURNS_PLAYED -> {
//                getPlayerResult(pGame, pModelChange.getKey()).setTurnsPlayed((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TEAM_RESULT_SET_CONCEDED -> {
//                getTeamResult(pGame, isHomeData(pModelChange)).setConceded((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            TEAM_RESULT_SET_DEDICATED_FANS_MODIFIER -> {
//                getTeamResult(
//                    pGame,
//                    isHomeData(pModelChange)
//                ).setDedicatedFansModifier((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TEAM_RESULT_SET_FAME -> {
//                getTeamResult(pGame, isHomeData(pModelChange)).setFame((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TEAM_RESULT_SET_BADLY_HURT_SUFFERED -> {
//                getTeamResult(pGame, isHomeData(pModelChange)).setBadlyHurtSuffered((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TEAM_RESULT_SET_PENALTY_SCORE -> {
//                getTeamResult(pGame, isHomeData(pModelChange)).setPenaltyScore((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TEAM_RESULT_SET_PETTY_CASH_TRANSFERRED -> {
//                getTeamResult(
//                    pGame,
//                    isHomeData(pModelChange)
//                ).setPettyCashTransferred((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TEAM_RESULT_SET_PETTY_CASH_USED -> {
//                getTeamResult(pGame, isHomeData(pModelChange)).setPettyCashUsed((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TEAM_RESULT_SET_RAISED_DEAD -> {
//                getTeamResult(pGame, isHomeData(pModelChange)).setRaisedDead((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TEAM_RESULT_SET_RIP_SUFFERED -> {
//                getTeamResult(pGame, isHomeData(pModelChange)).setRipSuffered((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TEAM_RESULT_SET_SCORE -> {
//                getTeamResult(pGame, isHomeData(pModelChange)).setScore((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TEAM_RESULT_SET_SERIOUS_INJURY_SUFFERED -> {
//                getTeamResult(
//                    pGame,
//                    isHomeData(pModelChange)
//                ).setSeriousInjurySuffered((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TEAM_RESULT_SET_SPECTATORS -> {
//                getTeamResult(pGame, isHomeData(pModelChange)).setSpectators((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TEAM_RESULT_SET_SPIRALLING_EXPENSES -> {
//                getTeamResult(pGame, isHomeData(pModelChange)).setSpirallingExpenses((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TEAM_RESULT_SET_TEAM_VALUE -> {
//                getTeamResult(pGame, isHomeData(pModelChange)).setTeamValue((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TEAM_RESULT_SET_FAN_FACTOR -> {
//                getTeamResult(pGame, isHomeData(pModelChange)).setFanFactor((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TEAM_RESULT_SET_WINNINGS -> {
//                getTeamResult(pGame, isHomeData(pModelChange)).setWinnings((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TURN_DATA_SET_APOTHECARIES -> {
//                getTurnData(pGame, isHomeData(pModelChange)).setApothecaries((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TURN_DATA_SET_BLITZ_USED -> {
//                getTurnData(pGame, isHomeData(pModelChange)).setBlitzUsed((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            TURN_DATA_SET_BOMB_USED -> {
//                getTurnData(pGame, isHomeData(pModelChange)).setBombUsed((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            TURN_DATA_SET_FIRST_TURN_AFTER_KICKOFF -> {
//                getTurnData(
//                    pGame,
//                    isHomeData(pModelChange)
//                ).setFirstTurnAfterKickoff((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            TURN_DATA_SET_FOUL_USED -> {
//                getTurnData(pGame, isHomeData(pModelChange)).setFoulUsed((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            TURN_DATA_SET_HAND_OVER_USED -> {
//                getTurnData(pGame, isHomeData(pModelChange)).setHandOverUsed((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            TURN_DATA_SET_LEADER_STATE -> {
//                getTurnData(pGame, isHomeData(pModelChange)).setLeaderState(pModelChange.getValue() as LeaderState?)
//                return true
//            }
//
//            TURN_DATA_SET_PASS_USED -> {
//                getTurnData(pGame, isHomeData(pModelChange)).setPassUsed((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            TURN_DATA_SET_KTM_USED -> {
//                getTurnData(pGame, isHomeData(pModelChange)).setKtmUsed((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            TURN_DATA_SET_RE_ROLLS_BRILLIANT_COACHING_ONE_DRIVE -> {
//                getTurnData(
//                    pGame,
//                    isHomeData(pModelChange)
//                ).setReRollsBrilliantCoachingOneDrive((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TURN_DATA_SET_RE_ROLLS_PUMP_UP_THE_CROWD_ONE_DRIVE -> {
//                getTurnData(
//                    pGame,
//                    isHomeData(pModelChange)
//                ).setReRollsPumpUpTheCrowdOneDrive((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TURN_DATA_SET_RE_ROLLS_SINGLE_USE -> {
//                getTurnData(pGame, isHomeData(pModelChange)).setSingleUseReRolls((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TURN_DATA_SET_RE_ROLL_USED -> {
//                getTurnData(pGame, isHomeData(pModelChange)).setReRollUsed((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            TURN_DATA_SET_TURN_NR -> {
//                getTurnData(pGame, isHomeData(pModelChange)).setTurnNr((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TURN_DATA_SET_TURN_STARTED -> {
//                getTurnData(pGame, isHomeData(pModelChange)).setTurnStarted((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            TURN_DATA_SET_COACH_BANNED -> {
//                getTurnData(pGame, isHomeData(pModelChange)).setCoachBanned((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            TURN_DATA_SET_WANDERING_APOTHECARIES -> {
//                getTurnData(pGame, isHomeData(pModelChange)).setWanderingApothecaries((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TURN_DATA_SET_PLAGUE_DOCTORS -> {
//                getTurnData(pGame, isHomeData(pModelChange)).setPlagueDoctors((pModelChange.getValue() as Int?))
//                return true
//            }
//        }
        return false
    }

//    fun transform(pModelChange: ModelChange?): ModelChange? {
//        val bloodSpot: BloodSpot?
//        val diceDecoration: DiceDecoration?
//        val fieldMarker: FieldMarker?
//        val moveSquare: MoveSquare?
//        val playerMarker: PlayerMarker?
//        val pushbackSquare: PushbackSquare?
//        val trackNumber: TrackNumber?
//        val trapDoor: TrapDoor
//        val ballCoordinate: FieldCoordinate?
//        val bombCoordinate: FieldCoordinate?
//        val playerCoordinate: FieldCoordinate?
//        val rangeRuler: RangeRuler?
//        val dialogParameter: IDialogParameter?
//        val passCoordinate: FieldCoordinate?
//        if (pModelChange == null || pModelChange.getChangeId() == null) {
//            return null
//        }
//
//        when (pModelChange.getChangeId()) {
//            FIELD_MODEL_ADD_BLOOD_SPOT -> {
//                bloodSpot = pModelChange.getValue() as BloodSpot?
//                return com.jervisffb.fumbbl.net.model.change.ModelChange(
//                    pModelChange.getChangeId(), pModelChange.getKey(), if ((bloodSpot != null)) bloodSpot
//                        .transform() else null
//                )
//            }
//
//            FIELD_MODEL_ADD_DICE_DECORATION, FIELD_MODEL_REMOVE_DICE_DECORATION -> {
//                diceDecoration = pModelChange.getValue() as DiceDecoration?
//                return com.jervisffb.fumbbl.net.model.change.ModelChange(
//                    pModelChange.getChangeId(), pModelChange.getKey(), if ((diceDecoration != null)) diceDecoration
//                        .transform() else null
//                )
//            }
//
//            FIELD_MODEL_ADD_FIELD_MARKER, FIELD_MODEL_REMOVE_FIELD_MARKER -> {
//                fieldMarker = pModelChange.getValue() as FieldMarker?
//                return com.jervisffb.fumbbl.net.model.change.ModelChange(
//                    pModelChange.getChangeId(), pModelChange.getKey(), if ((fieldMarker != null)) fieldMarker
//                        .transform() else null
//                )
//            }
//
//            FIELD_MODEL_ADD_MOVE_SQUARE, FIELD_MODEL_REMOVE_MOVE_SQUARE -> {
//                moveSquare = pModelChange.getValue() as MoveSquare?
//                return com.jervisffb.fumbbl.net.model.change.ModelChange(
//                    pModelChange.getChangeId(), pModelChange.getKey(), if ((moveSquare != null)) moveSquare
//                        .transform() else null
//                )
//            }
//
//            FIELD_MODEL_ADD_PLAYER_MARKER, FIELD_MODEL_REMOVE_PLAYER_MARKER -> {
//                playerMarker = pModelChange.getValue() as PlayerMarker?
//                return com.jervisffb.fumbbl.net.model.change.ModelChange(
//                    pModelChange.getChangeId(), pModelChange.getKey(), if ((playerMarker != null)) playerMarker
//                        .transform() else null
//                )
//            }
//
//            FIELD_MODEL_ADD_PUSHBACK_SQUARE, FIELD_MODEL_REMOVE_PUSHBACK_SQUARE -> {
//                pushbackSquare = pModelChange.getValue() as PushbackSquare?
//                return com.jervisffb.fumbbl.net.model.change.ModelChange(
//                    pModelChange.getChangeId(), pModelChange.getKey(), if ((pushbackSquare != null)) pushbackSquare
//                        .transform() else null
//                )
//            }
//
//            FIELD_MODEL_ADD_TRACK_NUMBER, FIELD_MODEL_REMOVE_TRACK_NUMBER -> {
//                trackNumber = pModelChange.getValue() as TrackNumber?
//                return com.jervisffb.fumbbl.net.model.change.ModelChange(
//                    pModelChange.getChangeId(), pModelChange.getKey(), if ((trackNumber != null)) trackNumber
//                        .transform() else null
//                )
//            }
//
//            FIELD_MODEL_ADD_TRAP_DOOR, FIELD_MODEL_REMOVE_TRAP_DOOR -> {
//                trapDoor = pModelChange.getValue() as TrapDoor
//                return com.jervisffb.fumbbl.net.model.change.ModelChange(
//                    pModelChange.getChangeId(),
//                    pModelChange.getKey(),
//                    trapDoor.transform()
//                )
//            }
//
//            FIELD_MODEL_SET_BALL_COORDINATE -> {
//                ballCoordinate = pModelChange.getValue() as FieldCoordinate?
//                return com.jervisffb.fumbbl.net.model.change.ModelChange(
//                    pModelChange.getChangeId(), pModelChange.getKey(), if ((ballCoordinate != null)) ballCoordinate
//                        .transform() else null
//                )
//            }
//
//            FIELD_MODEL_SET_BOMB_COORDINATE -> {
//                bombCoordinate = pModelChange.getValue() as FieldCoordinate?
//                return com.jervisffb.fumbbl.net.model.change.ModelChange(
//                    pModelChange.getChangeId(), pModelChange.getKey(), if ((bombCoordinate != null)) bombCoordinate
//                        .transform() else null
//                )
//            }
//
//            FIELD_MODEL_SET_PLAYER_COORDINATE -> {
//                playerCoordinate = pModelChange.getValue() as FieldCoordinate?
//                return com.jervisffb.fumbbl.net.model.change.ModelChange(
//                    pModelChange.getChangeId(), pModelChange.getKey(), if ((playerCoordinate != null)) playerCoordinate
//                        .transform() else null
//                )
//            }
//
//            FIELD_MODEL_SET_RANGE_RULER -> {
//                rangeRuler = pModelChange.getValue() as RangeRuler?
//                return com.jervisffb.fumbbl.net.model.change.ModelChange(
//                    pModelChange.getChangeId(), pModelChange.getKey(), if ((rangeRuler != null)) rangeRuler
//                        .transform() else null
//                )
//            }
//
//            GAME_SET_DIALOG_PARAMETER -> {
//                dialogParameter = pModelChange.getValue() as IDialogParameter?
//                return com.jervisffb.fumbbl.net.model.change.ModelChange(
//                    pModelChange.getChangeId(), pModelChange.getKey(), if ((dialogParameter != null)) dialogParameter
//                        .transform() else null
//                )
//            }
//
//            GAME_SET_HOME_FIRST_OFFENSE, GAME_SET_HOME_PLAYING -> return com.jervisffb.fumbbl.net.model.change.ModelChange(
//                pModelChange.getChangeId(), pModelChange.getKey(),
//                !(pModelChange.getValue() as Boolean?)!!
//            )
//
//            GAME_SET_PASS_COORDINATE -> {
//                passCoordinate = pModelChange.getValue() as FieldCoordinate?
//                return com.jervisffb.fumbbl.net.model.change.ModelChange(
//                    pModelChange.getChangeId(), pModelChange.getKey(), if ((passCoordinate != null)) passCoordinate
//                        .transform() else null
//                )
//            }
//
//            INDUCEMENT_SET_ACTIVATE_CARD, INDUCEMENT_SET_ADD_AVAILABLE_CARD, INDUCEMENT_SET_ADD_INDUCEMENT, INDUCEMENT_SET_ADD_PRAYER, INDUCEMENT_SET_DEACTIVATE_CARD, INDUCEMENT_SET_REMOVE_AVAILABLE_CARD, INDUCEMENT_SET_REMOVE_INDUCEMENT, INDUCEMENT_SET_REMOVE_PRAYER -> return com.jervisffb.fumbbl.net.model.change.ModelChange(
//                pModelChange.getChangeId(), if (isHomeData(pModelChange)) "away" else "home", pModelChange
//                    .getValue()
//            )
//
//            TEAM_RESULT_SET_CONCEDED, TEAM_RESULT_SET_DEDICATED_FANS_MODIFIER, TEAM_RESULT_SET_FAME, TEAM_RESULT_SET_BADLY_HURT_SUFFERED, TEAM_RESULT_SET_FAN_FACTOR_MODIFIER, TEAM_RESULT_SET_PENALTY_SCORE, TEAM_RESULT_SET_PETTY_CASH_TRANSFERRED, TEAM_RESULT_SET_PETTY_CASH_USED, TEAM_RESULT_SET_RAISED_DEAD, TEAM_RESULT_SET_RIP_SUFFERED, TEAM_RESULT_SET_SCORE, TEAM_RESULT_SET_SERIOUS_INJURY_SUFFERED, TEAM_RESULT_SET_SPECTATORS, TEAM_RESULT_SET_SPIRALLING_EXPENSES, TEAM_RESULT_SET_TEAM_VALUE, TEAM_RESULT_SET_FAN_FACTOR, TEAM_RESULT_SET_WINNINGS, TURN_DATA_SET_APOTHECARIES, TURN_DATA_SET_BLITZ_USED, TURN_DATA_SET_BOMB_USED, TURN_DATA_SET_FIRST_TURN_AFTER_KICKOFF, TURN_DATA_SET_FOUL_USED, TURN_DATA_SET_HAND_OVER_USED, TURN_DATA_SET_LEADER_STATE, TURN_DATA_SET_PASS_USED, TURN_DATA_SET_KTM_USED, TURN_DATA_SET_RE_ROLLS, TURN_DATA_SET_RE_ROLLS_BRILLIANT_COACHING_ONE_DRIVE, TURN_DATA_SET_RE_ROLLS_PUMP_UP_THE_CROWD_ONE_DRIVE, TURN_DATA_SET_RE_ROLLS_SINGLE_USE, TURN_DATA_SET_RE_ROLL_USED, TURN_DATA_SET_TURN_NR, TURN_DATA_SET_TURN_STARTED, TURN_DATA_SET_COACH_BANNED, TURN_DATA_SET_WANDERING_APOTHECARIES, TURN_DATA_SET_PLAGUE_DOCTORS -> {
//            }
//            else -> return com.jervisffb.fumbbl.net.model.change.ModelChange(
//                pModelChange.getChangeId(),
//                pModelChange.getKey(),
//                pModelChange.getValue()
//            )
//        }
//        return com.jervisffb.fumbbl.net.model.change.ModelChange(
//            pModelChange.getChangeId(), if (isHomeData(pModelChange)) "away" else "home", pModelChange
//                .getValue()
//        )
//    }

    private fun isHomeData(pChange: ModelChange): Boolean {
        return "home" == pChange.key
    }

    private fun getTeamResult(
        game: Game,
        homeData: Boolean,
    ): TeamResult {
        return if (homeData) game.gameResult.teamResultHome else game.gameResult.teamResultAway
    }

    private fun getTurnData(
        game: Game,
        homeData: Boolean,
    ): TurnData {
        return if (homeData) game.turnDataHome else game.turnDataAway
    }

    private fun getInducementSet(
        pGame: Game,
        pHomeData: Boolean,
    ): InducementSet {
        return if (pHomeData) pGame.turnDataHome.inducementSet else pGame.turnDataAway.inducementSet
    }

    private fun getPlayerResult(
        game: Game,
        playerId: String,
    ): PlayerResult {
        return game.gameResult.getPlayerResult(game.getPlayerById(playerId)!!)
    }
}
