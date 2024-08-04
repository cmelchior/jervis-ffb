package dk.ilios.jervis.fumbblcli.codegenerator

/**
 * Create the Kotlin Serializer class for Model Changes defined by
 * the ModelChangeId enum class in the FUMBBL Client
 */

fun main(vararg args: String) {
    ModelChangeGenerator().let {
        it.createModelChangeClasses()
//        it.createReportScaffoldingClasses()
    }
}

class ModelChangeGenerator {
    val data =
        """
        ACTING_PLAYER_MARK_SKILL_USED("actingPlayerMarkSkillUsed", ModelChangeDataType.SKILL),
        ACTING_PLAYER_MARK_SKILL_UNUSED("actingPlayerMarkSkillUnused", ModelChangeDataType.SKILL),
        ACTING_PLAYER_SET_CURRENT_MOVE("actingPlayerSetCurrentMove", ModelChangeDataType.INTEGER),
        ACTING_PLAYER_SET_DODGING("actingPlayerSetDodging", ModelChangeDataType.BOOLEAN),
        ACTING_PLAYER_SET_GOING_FOR_IT("actingPlayerSetGoingForIt", ModelChangeDataType.BOOLEAN),
        ACTING_PLAYER_SET_HAS_BLOCKED("actingPlayerSetHasBlocked", ModelChangeDataType.BOOLEAN),
        ACTING_PLAYER_SET_HAS_FED("actingPlayerSetHasFed", ModelChangeDataType.BOOLEAN),
        ACTING_PLAYER_SET_HAS_FOULED("actingPlayerSetHasFouled", ModelChangeDataType.BOOLEAN),
        ACTING_PLAYER_SET_HAS_JUMPED("actingPlayerSetHasJumped", ModelChangeDataType.BOOLEAN),
        ACTING_PLAYER_SET_HAS_MOVED("actingPlayerSetHasMoved", ModelChangeDataType.BOOLEAN),
        ACTING_PLAYER_SET_HAS_PASSED("actingPlayerSetHasPassed", ModelChangeDataType.BOOLEAN),
        ACTING_PLAYER_SET_JUMPING("actingPlayerSetLeaping", ModelChangeDataType.BOOLEAN),
        ACTING_PLAYER_SET_OLD_PLAYER_STATE("actingPlayerSetOldPlayerState", ModelChangeDataType.PLAYER_STATE),
        ACTING_PLAYER_SET_PLAYER_ACTION("actingPlayerSetPlayerAction", ModelChangeDataType.PLAYER_ACTION),
        ACTING_PLAYER_SET_PLAYER_ID("actingPlayerSetPlayerId", ModelChangeDataType.STRING),
        ACTING_PLAYER_SET_STANDING_UP("actingPlayerSetStandingUp", ModelChangeDataType.BOOLEAN),
        ACTING_PLAYER_SET_STRENGTH("actingPlayerSetStrength", ModelChangeDataType.INTEGER),
        ACTING_PLAYER_SET_SUFFERING_ANIMOSITY("actingPlayerSetSufferingAnimosity", ModelChangeDataType.BOOLEAN),
        ACTING_PLAYER_SET_SUFFERING_BLOOD_LUST("actingPlayerSetSufferingBloodLust", ModelChangeDataType.BOOLEAN),
        FIELD_MODEL_ADD_BLOOD_SPOT("fieldModelAddBloodSpot", ModelChangeDataType.BLOOD_SPOT),
        FIELD_MODEL_ADD_CARD("fieldModelAddCard", ModelChangeDataType.CARD),
        FIELD_MODEL_ADD_CARD_EFFECT("fieldModelAddCardEffect", ModelChangeDataType.CARD_EFFECT),
        FIELD_MODEL_ADD_DICE_DECORATION("fieldModelAddDiceDecoration", ModelChangeDataType.DICE_DECORATION),
        FIELD_MODEL_ADD_INTENSIVE_TRAINING("fieldModelAddIntensiveTraining", ModelChangeDataType.SKILL),
        FIELD_MODEL_ADD_FIELD_MARKER("fieldModelAddFieldMarker", ModelChangeDataType.FIELD_MARKER),
        FIELD_MODEL_ADD_MOVE_SQUARE("fieldModelAddMoveSquare", ModelChangeDataType.MOVE_SQUARE),
        FIELD_MODEL_ADD_PLAYER_MARKER("fieldModelAddPlayerMarker", ModelChangeDataType.PLAYER_MARKER),
        FIELD_MODEL_ADD_PRAYER("fieldModelAddPrayer", ModelChangeDataType.STRING),
        FIELD_MODEL_ADD_SKILL_ENHANCEMENTS("fieldModelAddSkillEnhancements", ModelChangeDataType.STRING),
        FIELD_MODEL_ADD_PUSHBACK_SQUARE("fieldModelAddPushbackSquare", ModelChangeDataType.PUSHBACK_SQUARE),
        FIELD_MODEL_ADD_TRACK_NUMBER("fieldModelAddTrackNumber", ModelChangeDataType.TRACK_NUMBER),
        FIELD_MODEL_ADD_TRAP_DOOR("fieldModelAddTrapDoor", ModelChangeDataType.TRAP_DOOR),
        FIELD_MODEL_ADD_WISDOM("fieldModelAddWisdom", ModelChangeDataType.SKILL),
        FIELD_MODEL_KEEP_DEACTIVATED_CARD("fieldModelKeepDeactivatedCard", ModelChangeDataType.CARD),
        FIELD_MODEL_REMOVE_CARD("fieldModelRemoveCard", ModelChangeDataType.CARD),
        FIELD_MODEL_REMOVE_CARD_EFFECT("fieldModelRemoveCardEffect", ModelChangeDataType.CARD_EFFECT),
        FIELD_MODEL_REMOVE_DICE_DECORATION("fieldModelRemoveDiceDecoration", ModelChangeDataType.DICE_DECORATION),
        FIELD_MODEL_REMOVE_FIELD_MARKER("fieldModelRemoveFieldMarker", ModelChangeDataType.FIELD_MARKER),
        FIELD_MODEL_REMOVE_MOVE_SQUARE("fieldModelRemoveMoveSquare", ModelChangeDataType.MOVE_SQUARE),
        FIELD_MODEL_REMOVE_PLAYER("fieldModelRemovePlayer", ModelChangeDataType.FIELD_COORDINATE),
        FIELD_MODEL_REMOVE_PLAYER_MARKER("fieldModelRemovePlayerMarker", ModelChangeDataType.PLAYER_MARKER),
        FIELD_MODEL_REMOVE_PRAYER("fieldModelRemovePrayer", ModelChangeDataType.STRING),
        FIELD_MODEL_REMOVE_SKILL_ENHANCEMENTS("fieldModelRemoveSkillEnhancements", ModelChangeDataType.STRING),
        FIELD_MODEL_REMOVE_PUSHBACK_SQUARE("fieldModelRemovePushbackSquare", ModelChangeDataType.PUSHBACK_SQUARE),
        FIELD_MODEL_REMOVE_TRACK_NUMBER("fieldModelRemoveTrackNumber", ModelChangeDataType.TRACK_NUMBER),
        FIELD_MODEL_REMOVE_TRAP_DOOR("fieldModelRemoveTrapDoor", ModelChangeDataType.TRAP_DOOR),
        FIELD_MODEL_SET_BALL_COORDINATE("fieldModelSetBallCoordinate", ModelChangeDataType.FIELD_COORDINATE),
        FIELD_MODEL_SET_BALL_IN_PLAY("fieldModelSetBallInPlay", ModelChangeDataType.BOOLEAN),
        FIELD_MODEL_SET_BALL_MOVING("fieldModelSetBallMoving", ModelChangeDataType.BOOLEAN),
        FIELD_MODEL_SET_BLITZ_STATE("fieldModelSetBlitzState", ModelChangeDataType.BLITZ_STATE),
        FIELD_MODEL_SET_BOMB_COORDINATE("fieldModelSetBombCoordinate", ModelChangeDataType.FIELD_COORDINATE),
        FIELD_MODEL_SET_BOMB_MOVING("fieldModelSetBombMoving", ModelChangeDataType.BOOLEAN),
        FIELD_MODEL_SET_PLAYER_COORDINATE("fieldModelSetPlayerCoordinate", ModelChangeDataType.FIELD_COORDINATE),
        FIELD_MODEL_SET_PLAYER_STATE("fieldModelSetPlayerState", ModelChangeDataType.PLAYER_STATE),
        FIELD_MODEL_SET_RANGE_RULER("fieldModelSetRangeRuler", ModelChangeDataType.RANGE_RULER),
        FIELD_MODEL_SET_TARGET_SELECTION_STATE("fieldModelSetTargetSelectionState", ModelChangeDataType.TARGET_SELECTION_STATE),
        FIELD_MODEL_SET_WEATHER("fieldModelSetWeather", ModelChangeDataType.WEATHER),
        GAME_SET_ADMIN_MODE("gameSetAdminMode", ModelChangeDataType.BOOLEAN),
        GAME_SET_CONCEDED_LEGALLY("gameSetConcededLegally", ModelChangeDataType.BOOLEAN),
        GAME_SET_CONCESSION_POSSIBLE("gameSetConcessionPossible", ModelChangeDataType.BOOLEAN),
        GAME_SET_DEFENDER_ACTION("gameSetDefenderAction", ModelChangeDataType.PLAYER_ACTION),
        GAME_SET_DEFENDER_ID("gameSetDefenderId", ModelChangeDataType.STRING),
        GAME_SET_DIALOG_PARAMETER("gameSetDialogParameter", ModelChangeDataType.DIALOG_PARAMETER),
        GAME_SET_FINISHED("gameSetFinished", ModelChangeDataType.DATE),
        GAME_SET_HALF("gameSetHalf", ModelChangeDataType.INTEGER),
        GAME_SET_HOME_FIRST_OFFENSE("gameSetHomeFirstOffense", ModelChangeDataType.BOOLEAN),
        GAME_SET_HOME_PLAYING("gameSetHomePlaying", ModelChangeDataType.BOOLEAN),
        GAME_SET_ID("gameSetId", ModelChangeDataType.LONG),
        GAME_SET_LAST_DEFENDER_ID("gameSetLastDefenderId", ModelChangeDataType.STRING),
        GAME_SET_LAST_TURN_MODE("gameSetLastTurnMode", ModelChangeDataType.TURN_MODE),
        GAME_SET_PASS_COORDINATE("gameSetPassCoordinate", ModelChangeDataType.FIELD_COORDINATE),
        GAME_SET_SCHEDULED("gameSetScheduled", ModelChangeDataType.DATE),
        GAME_SET_SETUP_OFFENSE("gameSetSetupOffense", ModelChangeDataType.BOOLEAN),
        GAME_SET_STARTED("gameSetStarted", ModelChangeDataType.DATE),
        GAME_SET_TESTING("gameSetTesting", ModelChangeDataType.BOOLEAN),
        GAME_SET_THROWER_ID("gameSetThrowerId", ModelChangeDataType.STRING),
        GAME_SET_THROWER_ACTION("gameSetThrowerAction", ModelChangeDataType.PLAYER_ACTION),
        GAME_SET_TIMEOUT_ENFORCED("gameSetTimeoutEnforced", ModelChangeDataType.BOOLEAN),
        GAME_SET_TIMEOUT_POSSIBLE("gameSetTimeoutPossible", ModelChangeDataType.BOOLEAN),
        GAME_SET_TURN_MODE("gameSetTurnMode", ModelChangeDataType.TURN_MODE),
        GAME_SET_WAITING_FOR_OPPONENT("gameSetWaitingForOpponent", ModelChangeDataType.BOOLEAN),
        GAME_OPTIONS_ADD_OPTION("gameOptionsAddOption", ModelChangeDataType.GAME_OPTION),
        INDUCEMENT_SET_ACTIVATE_CARD("inducementSetActivateCard", ModelChangeDataType.CARD),
        INDUCEMENT_SET_ADD_AVAILABLE_CARD("inducementSetAddAvailableCard", ModelChangeDataType.CARD),
        INDUCEMENT_SET_ADD_INDUCEMENT("inducementSetAddInducement", ModelChangeDataType.INDUCEMENT),
        INDUCEMENT_SET_CARD_CHOICES("inducementSetCardChoices", ModelChangeDataType.CARD_CHOICES),
        INDUCEMENT_SET_DEACTIVATE_CARD("inducementSetDeactivateCard", ModelChangeDataType.CARD),
        INDUCEMENT_SET_ADD_PRAYER("inducementSetAddPrayer", ModelChangeDataType.PRAYER),
        INDUCEMENT_SET_REMOVE_AVAILABLE_CARD("inducementSetRemoveAvailableCard", ModelChangeDataType.CARD),
        INDUCEMENT_SET_REMOVE_INDUCEMENT("inducementSetRemoveInducement", ModelChangeDataType.INDUCEMENT),
        INDUCEMENT_SET_REMOVE_PRAYER("inducementSetRemovePrayer", ModelChangeDataType.PRAYER),
        PLAYER_MARK_SKILL_USED("playerMarkSkillUsed", ModelChangeDataType.SKILL),
        PLAYER_MARK_SKILL_UNUSED("playerMarkSkillUnused", ModelChangeDataType.SKILL),
        PLAYER_RESULT_SET_BLOCKS("playerResultSetBlocks", ModelChangeDataType.INTEGER),
        PLAYER_RESULT_SET_CASUALTIES("playerResultSetCasualties", ModelChangeDataType.INTEGER),
        PLAYER_RESULT_SET_CASUALTIES_WITH_ADDITIONAL_SPP("playerResultSetCasualtiesWithAdditionalSpp", ModelChangeDataType.INTEGER),
        PLAYER_RESULT_SET_COMPLETIONS("playerResultSetCompletions", ModelChangeDataType.INTEGER),
        PLAYER_RESULT_SET_COMPLETIONS_WITH_ADDITIONAL_SPP("playerResultSetCompletionsWithAdditionalSpp", ModelChangeDataType.INTEGER),
        PLAYER_RESULT_SET_CURRENT_SPPS("playerResultSetCurrentSpps", ModelChangeDataType.INTEGER),
        PLAYER_RESULT_SET_DEFECTING("playerResultSetDefecting", ModelChangeDataType.BOOLEAN),
        PLAYER_RESULT_SET_FOULS("playerResultSetFouls", ModelChangeDataType.INTEGER),
        PLAYER_RESULT_SET_HAS_USED_SECRET_WEAPON("playerResultSetHasUsedSecretWeapon", ModelChangeDataType.BOOLEAN),
        PLAYER_RESULT_SET_INTERCEPTIONS("playerResultSetInterceptions", ModelChangeDataType.INTEGER),
        PLAYER_RESULT_SET_DEFLECTIONS("playerResultSetDeflections", ModelChangeDataType.INTEGER),
        PLAYER_RESULT_SET_PASSING("playerResultSetPassing", ModelChangeDataType.INTEGER),
        PLAYER_RESULT_SET_PLAYER_AWARDS("playerResultSetPlayerAwards", ModelChangeDataType.INTEGER),
        PLAYER_RESULT_SET_RUSHING("playerResultSetRushing", ModelChangeDataType.INTEGER),
        PLAYER_RESULT_SET_SEND_TO_BOX_BY_PLAYER_ID("playerResultSetSendToBoxByPlayerId", ModelChangeDataType.STRING),
        PLAYER_RESULT_SET_SEND_TO_BOX_HALF("playerResultSetSendToBoxHalf", ModelChangeDataType.INTEGER),
        PLAYER_RESULT_SET_SEND_TO_BOX_REASON("playerResultSetSendToBoxReason", ModelChangeDataType.SEND_TO_BOX_REASON),
        PLAYER_RESULT_SET_SEND_TO_BOX_TURN("playerResultSetSendToBoxTurn", ModelChangeDataType.INTEGER),
        PLAYER_RESULT_SET_SERIOUS_INJURY("playerResultSetSeriousInjury", ModelChangeDataType.SERIOUS_INJURY),
        PLAYER_RESULT_SET_SERIOUS_INJURY_DECAY("playerResultSetSeriousInjuryDecay", ModelChangeDataType.SERIOUS_INJURY),
        PLAYER_RESULT_SET_TOUCHDOWNS("playerResultSetTouchdowns", ModelChangeDataType.INTEGER),
        PLAYER_RESULT_SET_TURNS_PLAYED("playerResultSetTurnsPlayed", ModelChangeDataType.INTEGER),
        TEAM_RESULT_SET_CONCEDED("teamResultSetConceded", ModelChangeDataType.BOOLEAN),
        TEAM_RESULT_SET_DEDICATED_FANS_MODIFIER("teamResultDedicatedFansModifier", ModelChangeDataType.INTEGER),
        TEAM_RESULT_SET_FAME("teamResultSetFame", ModelChangeDataType.INTEGER),
        TEAM_RESULT_SET_FAN_FACTOR("teamResultSetFanFactor", ModelChangeDataType.INTEGER),
        TEAM_RESULT_SET_BADLY_HURT_SUFFERED("teamResultSetBadlyHurtSuffered", ModelChangeDataType.INTEGER),
        TEAM_RESULT_SET_FAN_FACTOR_MODIFIER("teamResultSetFanFactorModifier", ModelChangeDataType.INTEGER),
        TEAM_RESULT_SET_PENALTY_SCORE("teamResultSetPenaltyScore", ModelChangeDataType.INTEGER),
        TEAM_RESULT_SET_PETTY_CASH_TRANSFERRED("teamResultSetPettyCashTransferred", ModelChangeDataType.INTEGER),
        TEAM_RESULT_SET_PETTY_CASH_USED("teamResultSetPettyCashUsed", ModelChangeDataType.INTEGER),
        TEAM_RESULT_SET_RAISED_DEAD("teamResultSetRaisedDead", ModelChangeDataType.INTEGER),
        TEAM_RESULT_SET_RIP_SUFFERED("teamResultSetRipSuffered", ModelChangeDataType.INTEGER),
        TEAM_RESULT_SET_SCORE("teamResultSetScore", ModelChangeDataType.INTEGER),
        TEAM_RESULT_SET_SERIOUS_INJURY_SUFFERED("teamResultSetSeriousInjurySuffered", ModelChangeDataType.INTEGER),
        TEAM_RESULT_SET_SPECTATORS("teamResultSetSpectators", ModelChangeDataType.INTEGER),
        TEAM_RESULT_SET_SPIRALLING_EXPENSES("teamResultSetSpirallingExpenses", ModelChangeDataType.INTEGER),
        TEAM_RESULT_SET_TEAM_VALUE("teamResultSetTeamValue", ModelChangeDataType.INTEGER),
        TEAM_RESULT_SET_WINNINGS("teamResultSetWinnings", ModelChangeDataType.INTEGER),
        TURN_DATA_SET_APOTHECARIES("turnDataSetApothecaries", ModelChangeDataType.INTEGER),
        TURN_DATA_SET_BLITZ_USED("turnDataSetBlitzUsed", ModelChangeDataType.BOOLEAN),
        TURN_DATA_SET_BOMB_USED("turnDataSetBombUsed", ModelChangeDataType.BOOLEAN),
        TURN_DATA_SET_FIRST_TURN_AFTER_KICKOFF("turnDataSetFirstTurnAfterKickoff", ModelChangeDataType.BOOLEAN),
        TURN_DATA_SET_FOUL_USED("turnDataSetFoulUsed", ModelChangeDataType.BOOLEAN),
        TURN_DATA_SET_HAND_OVER_USED("turnDataSetHandOverUsed", ModelChangeDataType.BOOLEAN),
        TURN_DATA_SET_LEADER_STATE("turnDataSetLeaderState", ModelChangeDataType.LEADER_STATE),
        TURN_DATA_SET_PASS_USED("turnDataSetPassUsed", ModelChangeDataType.BOOLEAN),
        TURN_DATA_SET_PLAGUE_DOCTORS("turnDataSetPlagueDoctors", ModelChangeDataType.INTEGER),
        TURN_DATA_SET_KTM_USED("turnDataSetKtmUsed", ModelChangeDataType.BOOLEAN),
        TURN_DATA_SET_RE_ROLLS("turnDataSetReRolls", ModelChangeDataType.INTEGER),
        TURN_DATA_SET_RE_ROLLS_BRILLIANT_COACHING_ONE_DRIVE("turnDataSetReRollsBrilliantCoachingOneDrive", ModelChangeDataType.INTEGER),
        TURN_DATA_SET_RE_ROLLS_PUMP_UP_THE_CROWD_ONE_DRIVE("turnDataSetReRollsPumpUpTheCrowdOneDrive", ModelChangeDataType.INTEGER),
        TURN_DATA_SET_RE_ROLLS_SINGLE_USE("turnDataSetReRollsSingleUse", ModelChangeDataType.INTEGER),
        TURN_DATA_SET_RE_ROLL_USED("turnDataSetReRollUsed", ModelChangeDataType.BOOLEAN),
        TURN_DATA_SET_TURN_NR("turnDataSetTurnNr", ModelChangeDataType.INTEGER),
        TURN_DATA_SET_TURN_STARTED("turnDataSetTurnStarted", ModelChangeDataType.BOOLEAN),
        TURN_DATA_SET_WANDERING_APOTHECARIES("turnDataSetWanderingApothecaries", ModelChangeDataType.INTEGER),
        TURN_DATA_SET_COACH_BANNED("turnDataSetCoachBanned", ModelChangeDataType.BOOLEAN);        
        """.trimIndent()

// These options are not covered. Need to check where/how they are used

    fun createModelChangeClasses() {
        val pattern = """([A-Z_]+)\("([a-zA-Z]+)",\s*ModelChangeDataType\.([A-Za-z_]+)\)[,;]?""".toRegex()
        data
            .split("\n")
            .map { it.trim() }
            .map { str ->
                val matchResult: MatchResult = pattern.matchEntire(str) ?: throw IllegalArgumentException("Didn't match: $str")
                val (enumName, stringName, type) = matchResult.destructured
                val valueEnum = type.removeSuffix("ModelChangeDataType.")

//    BLITZ_STATE("blitzState"),
//    CARD("card"),
//    CARD_CHOICES("cardChoices"),
//    CARD_EFFECT("cardEffect"),
//    DIALOG_ID("dialogId"),
//    FIELD_MARKER("fieldMarker"),
//    GAME_OPTION("gameOption"),
//    LEADER_STATE("leaderState"),
//    NULL("null"),
//    PRAYER("prayer"),
//    SEND_TO_BOX_REASON("sendToBoxReason"),
//    SERIOUS_INJURY("seriousInjury"),
//    SKILL("skill"),
//    STRING("string"),
//    TRAP_DOOR("trapDoor"),
//    WEATHER("weather");

                // Determine value type based on ModelDataChangeType
                // However this doesn't take into account nullability,
                // so we override these in the next section
                var valueType =
                    when {
                        (valueEnum == "BLOOD_SPOT") -> "BloodSpot"
                        (valueEnum == "BOOLEAN") -> "Boolean"
                        (valueEnum == "DATE") -> "LocalDateTime?"
                        (valueEnum == "DIALOG_PARAMETER") -> "DialogOptions?"
                        (valueEnum == "DICE_DECORATION") -> "DiceDecoration"
                        (valueEnum == "FIELD_COORDINATE") -> "FieldCoordinate?"
                        (valueEnum == "INDUCEMENT") -> "Inducement"
                        (valueEnum == "INDUCEMENT_SET_ADD_PRAYER") -> "String" // Prayer
                        (valueEnum == "INTEGER") -> "Int"
                        (valueEnum == "LONG") -> "Long"
                        (valueEnum == "MOVE_SQUARE") -> "MoveSquare"
                        (valueEnum == "PLAYER_ACTION") -> "PlayerAction"
                        (valueEnum == "PLAYER_MARKER") -> "PlayerMarker"
                        (valueEnum == "PLAYER_STATE") -> "PlayerState"
                        (valueEnum == "PUSHBACK_SQUARE") -> "PushBackSquare"
                        (valueEnum == "RANGE_RULER") -> "RangeRuler?"
                        (valueEnum == "TARGET_SELECTION_STATE") -> "TargetSelectionState?"
                        (valueEnum == "TRACK_NUMBER") -> "TrackNumber"
                        else -> "String?"
                    }

                valueType =
                    when {
                        (enumName == "ACTING_PLAYER_SET_PLAYER_ID") -> "PlayerId"
                        (enumName == "ACTING_PLAYER_MARK_SKILL_UNUSED") -> "String"
                        (enumName == "ACTING_PLAYER_MARK_SKILL_USED") -> "String"
                        (enumName == "FIELD_MODEL_ADD_TRAP_DOOR") -> "TrapDoor"
                        (enumName == "FIELD_MODEL_REMOVE_FIELD_MARKER") -> "FieldMarker"
                        (enumName == "FIELD_MODEL_REMOVE_PLAYER") -> "FieldCoordinate?"
                        (enumName == "FIELD_MODEL_REMOVE_TRAP_DOOR") -> "TrapDoor"
                        (enumName == "FIELD_MODEL_SET_BALL_COORDINATE") -> "FieldCoordinate?"
                        (enumName == "FIELD_MODEL_SET_BLITZ_STATE") -> "TargetSelectionState?"
                        (enumName == "FIELD_MODEL_SET_PLAYER_COORDINATE") -> "FieldCoordinate?"
                        (enumName == "FIELD_MODEL_SET_WEATHER") -> "String"
                        (enumName == "GAME_SET_DEFENDER_ACTION") -> "PlayerAction?"
                        (enumName == "GAME_SET_DEFENDER_ACTION") -> "PlayerAction?"
                        (enumName == "GAME_SET_LAST_TURN_MODE") -> "TurnMode?"
                        (enumName == "GAME_SET_PASS_COORDINATE") -> "FieldCoordinate?"
                        (enumName == "GAME_SET_PASS_COORDINATE") -> "FieldCoordinate?"
                        (enumName == "GAME_SET_THROWER_ACTION") -> "PlayerAction?"
                        (enumName == "GAME_SET_TURN_MODE") -> "TurnMode"
                        (enumName == "TURN_DATA_SET_LEADER_STATE") -> "String"
                        else -> valueType
                    }

                val keyType =
                    when {
                        stringName.startsWith("actingPlayer") -> "Nothing?"
                        stringName.startsWith("gameSet") -> "String?" // Some of these are also Nothing?
                        stringName.endsWith("FieldModelRemoveSkillEnhancements") -> "String"
                        stringName.endsWith("SetPlayerState") -> "String"
                        stringName.endsWith("SetPlayerCoordinate") -> "String"
                        stringName.endsWith("actingPlayerSetPlayerId") -> "PlayerId?"
                        stringName.endsWith("actingPlayerSetPlayerAction") -> "PlayerAction?"
                        stringName.startsWith("playerResultSet") -> "String"
                        stringName.endsWith("fieldModelRemovePlayer") -> "PlayerId"
                        stringName.endsWith("fieldModelAddPrayer") -> "String"
                        else -> "String?"
                    }

                val className = stringName.capitalize()

                println(
                    """
                    @Serializable
                    @SerialName("$stringName")
                    data class $className(
                        @SerialName("modelChangeId")
                        override val id: ModelChangeId = ModelChangeId.$enumName,
                        @SerialName("modelChangeKey")
                        override val key: $keyType,
                        @SerialName("modelChangeValue")
                        override val value: $valueType
                    ): ModelChange
                    
                    """.trimIndent(),
                )
            }
    }

    val reports =
        """
        ALL_YOU_CAN_EAT("allYouCanEat"),
        ALWAYS_HUNGRY_ROLL("alwaysHungryRoll"),
        ANIMAL_SAVAGERY("animalSavagery"),
        ANIMOSITY_ROLL("animosityRoll"),
        APOTHECARY_CHOICE("apothecaryChoice"),
        APOTHECARY_ROLL("apothecaryRoll"),
        ARGUE_THE_CALL("argueTheCall"),
        BALEFUL_HEX("balefulHex"),
        BIASED_REF("biasedRef"),
        BITE_SPECTATOR("biteSpectator"),
        BLITZ_ROLL("blitzRoll"),
        BLOCK("block"),
        BLOCK_CHOICE("blockChoice"),
        BLOCK_RE_ROLL("blockReRoll"),
        BLOCK_ROLL("blockRoll"),
        BLOOD_LUST_ROLL("bloodLustRoll"),
        BOMB_EXPLODES_AFTER_CATCH("bombExplodesAfterCatch"),
        BOMB_OUT_OF_BOUNDS("bombOutOfBounds"),
        BRIBERY_AND_CORRUPTION_RE_ROLL("briberyAndCorruptionReRoll"),
        BRIBES_ROLL("bribesRoll"),
        BRILLIANT_COACHING_RE_ROLLS_LOST("brilliantCoachingReRoll"),
        CARDS_AND_INDUCEMENTS_BOUGHT("cardsAndInducementsBought"),
        CARDS_BOUGHT("cardsBought"),
        CARD_DEACTIVATED("cardDeactivated"),
        CARD_EFFECT_ROLL("cardEffectRoll"),
        CATCH_ROLL("catchRoll"),
        CLOUD_BURSTER("cloudBurster"),
        CHAINSAW_ROLL("chainsawRoll"),
        COIN_THROW("coinThrow"),
        CONFUSION_ROLL("confusionRoll"),
        DAUNTLESS_ROLL("dauntlessRoll"),
        DEDICATED_FANS("dedicatedFans"),
        DEFECTING_PLAYERS("defectingPlayers"),
        DODGE_ROLL("dodgeRoll"),
        DOUBLE_HIRED_STAFF("doubleHiredStaff"),
        DOUBLE_HIRED_STAR_PLAYER("doubleHiredStarPlayer"),
        ESCAPE_ROLL("escapeRoll"),
        EVENT("event"),
        FAN_FACTOR("fanFactor"),
        FAN_FACTOR_ROLL_POST_MATCH("fanFactorRoll"),
        FOUL("foul"),
        FOUL_APPEARANCE_ROLL("foulAppearanceRoll"),
        FREE_PETTY_CASH("freePettyCash"),
        FUMBBL_RESULT_UPLOAD("fumbblResultUpload"),
        FUMBLEROOSKIE("fumblerooskie"),
        GAME_OPTIONS("gameOptions"),
        GO_FOR_IT_ROLL("goForItRoll"),
        HAND_OVER("handOver"),
        HIT_AND_RUN("hitAndRun"),
        HYPNOTIC_GAZE_ROLL("hypnoticGazeRoll"),
        INDOMITABLE("indomitable"),
        INDUCEMENT("inducement"),
        INDUCEMENTS_BOUGHT("inducementsBought"),
        INJURY("injury"),
        INTERCEPTION_ROLL("interceptionRoll"),
        JUMP_ROLL("leapRoll"),
        JUMP_UP_ROLL("jumpUpRoll"),
        KICKOFF_CHEERING_FANS("cheeringFans"),
        KICKOFF_EXTRA_RE_ROLL("extraReRoll"),
        KICKOFF_OFFICIOUS_REF("kickoffOfficiousRef"),
        KICKOFF_PITCH_INVASION("kickoffPitchInvasion"),
        KICKOFF_RESULT("kickoffResult"),
        KICKOFF_RIOT("kickoffRiot"),
        KICKOFF_SCATTER("kickoffScatter"),
        KICKOFF_SEQUENCE_ACTIVATIONS_COUNT("kickoffSequenceActivationsCount"),
        KICKOFF_SEQUENCE_ACTIVATIONS_EXHAUSTED("kickoffSequenceActivationsExhausted"),
        KICKOFF_THROW_A_ROCK("kickoffThrowARock"),
        KICKOFF_TIMEOUT("kickoffTimeout"),
        KICK_TEAM_MATE_FUMBLE("kickTeamMateFumble"),
        KICK_TEAM_MATE_ROLL("kickTeamMateRoll"),
        LEADER("leader"),
        LOOK_INTO_MY_EYES_ROLL("lookIntoMyEyesRoll"),
        MASTER_CHEF_ROLL("masterChefRoll"),
        MODIFIED_DODGE_RESULT_SUCCESSFUL("modifiedDodgeResultSuccessful"),
        MODIFIED_PASS_RESULT("modifiedPassResult"),
        MOST_VALUABLE_PLAYERS("mostValuablePlayers"),
        NERVES_OF_STEEL("nervesOfSteel"),
        NONE("none"),
        NO_PLAYERS_TO_FIELD("noPlayersToField"),
        OFFICIOUS_REF_ROLL("officiousRefRoll"),
        OLD_PRO("oldPro"),
        PASS_BLOCK("passBlock"),
        PASS_DEVIATE("passDeviate"),
        PASS_ROLL("passRoll"),
        PENALTY_SHOOTOUT("penaltyShootout"),
        PETTY_CASH("pettyCash"),
        PICK_ME_UP("pickMeUp"),
        PICK_UP_ROLL("pickUpRoll"),
        PILING_ON("pilingOn"),
        PLACE_BALL_DIRECTION("placedBallDirection"),
        PLAYER_ACTION("playerAction"),
        PLAYER_EVENT("playerEvent"),
        PLAY_CARD("playCard"),
        PRAYER_AMOUNT("prayerAmount"),
        PRAYER_END("prayerEnd"),
        PRAYER_ROLL("prayerRoll"),
        PRAYER_WASTED("prayerWasted"),
        PROJECTILE_VOMIT("projectileVomit"),
        PUMP_UP_THE_CROWD_RE_ROLL("pumpUpTheCrowdReRoll"),
        PUMP_UP_THE_CROWD_RE_ROLLS_LOST("pumpUpTheCrowdReRollLost"),
        PUSHBACK("pushback"),
        QUICK_SNAP_ROLL("quickSnapRoll"),
        RAIDING_PARTY("raidingParty"),
        RAISE_DEAD("raiseDead"),
        RECEIVE_CHOICE("receiveChoice"),
        REFEREE("referee"),
        REGENERATION_ROLL("regenerationRoll"),
        RE_ROLL("reRoll"),
        RIGHT_STUFF_ROLL("rightStuffRoll"),
        RIOTOUS_ROOKIES("riotousRookies"),
        SAFE_THROW_ROLL("safeThrowRoll"),
        SCATTER_BALL("scatterBall"),
        SCATTER_PLAYER("scatterPlayer"),
        SECRET_WEAPON_BAN("secretWeaponBan"),
        SELECT_BLITZ_TARGET("selectBlitzTarget"),
        SELECT_GAZE_TARGET("selectGazeTarget"),
        SKILL_USE("skillUse"),
        SKILL_USE_OTHER_PLAYER("skillUseOtherPlayer"),
        SKILL_WASTED("skillWasted"),
        SOLID_DEFENCE_ROLL("solidDefenceRoll"),
        SPECTATORS("spectators"),
        SPELL_EFFECT_ROLL("spellEffectRoll"),
        STALLER_DETECTED("stallerDetected"),
        STAND_UP_ROLL("standUpRoll"),
        START_HALF("startHalf"),
        SWARMING_PLAYERS_ROLL("swarmingPlayersRoll"),
        SWOOP_PLAYER("swoopPlayer"),
        TENTACLES_SHADOWING_ROLL("tentaclesShadowingRoll"),
        THROWN_KEG("thrownKeg"),
        THROW_AT_STALLING_PLAYER("throwAtStallingPlayer"),
        THROW_IN("throwIn"),
        THROW_TEAM_MATE_ROLL("throwTeamMateRoll"),
        TIMEOUT_ENFORCED("timeoutEnforced"),
        TRAP_DOOR("trapDoor"),
        TURN_END("turnEnd"),
        TWO_FOR_ONE("twoForOne"),
        WEATHER("weather"),
        WEATHER_MAGE_RESULT("weatherMageResult"),
        WEATHER_MAGE_ROLL("weatherMageRoll"),
        WEEPING_DAGGER_ROLL("weepingDaggerRoll"),
        WINNINGS("winnings"),
        WINNINGS_ROLL("winningsRoll"),
        WIZARD_USE("wizardUse");        
        """.trimIndent()

    fun createReportScaffoldingClasses() {
        val pattern = """([A-Z_]+)\("([a-zA-Z]+)"\)[,;]?""".toRegex()
        reports
            .split("\n")
            .map { it.trim() }
            .map { str ->
                val matchResult: MatchResult = pattern.matchEntire(str) ?: throw IllegalArgumentException("Didn't match: $str")
                val (enumName, stringName) = matchResult.destructured
                val className = stringName.capitalize()

                println(
                    """
                    @Serializable
                    @SerialName("$stringName")
                    data class ${className}Report(
                         override val reportId: ReportId = ReportId.$enumName,
                    ): Report
                    
                    """.trimIndent(),
                )
            }
    }
}
