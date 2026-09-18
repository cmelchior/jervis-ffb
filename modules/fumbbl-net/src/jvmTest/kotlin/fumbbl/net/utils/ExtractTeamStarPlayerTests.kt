package fumbbl.net.utils

import com.jervisffb.engine.bb2025.StandardBB2025Rules
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerType
import com.jervisffb.fumbbl.net.model.ActingPlayer
import com.jervisffb.fumbbl.net.model.FieldModel
import com.jervisffb.fumbbl.net.model.GameOptions
import com.jervisffb.fumbbl.net.model.GameResult
import com.jervisffb.fumbbl.net.model.InducementSet
import com.jervisffb.fumbbl.net.model.Player as FumbblPlayer
import com.jervisffb.fumbbl.net.model.Game as FumbblGame
import com.jervisffb.fumbbl.net.model.PlayerType as FumbblPlayerType
import com.jervisffb.fumbbl.net.model.Roster as FumbblRoster
import com.jervisffb.fumbbl.net.model.RosterPlayer
import com.jervisffb.fumbbl.net.model.SpecialRule
import com.jervisffb.fumbbl.net.model.Team as FumbblTeam
import com.jervisffb.fumbbl.net.model.TeamResult
import com.jervisffb.fumbbl.net.model.TurnData
import com.jervisffb.fumbbl.net.model.TurnMode
import com.jervisffb.fumbbl.net.utils.fromFumbblState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Star Players hired into a replay team are not part of the team roster, so
 * [extractTeam] must fall back to the ruleset inducements (mirroring
 * FumbblApi and SerializedTeam) instead of aborting with
 * "Could not find position". Genuinely unknown positions must still fail
 * loudly. Tested via the public [Game.fromFumbblState] since [extractTeam]
 * is private.
 */
class ExtractTeamStarPlayerTests {

    private val rules = StandardBB2025Rules()

    @Test
    fun `star hire extracts as STAR_PLAYER`() {
        val game = fumbblGame(
            home = fumbblTeam(
                name = "Ogre Home",
                rosterName = "Ogre",
                leagueRule = SpecialRule.BADLANDS_BRAWL,
                positions = listOf(
                    rosterPosition("gnoblar-1", "Gnoblar Lineman"),
                    rosterPosition("ripper-1", "Ripper Bolgrot"),
                ),
                players = listOf(
                    fumbblPlayer("p1", 1, "gnoblar-1", "Gnoblar One"),
                    fumbblPlayer("ripper", 16, "ripper-1", "Ripper"),
                ),
            ),
            away = simpleOgreTeam("Ogre Away"),
        )

        val result = Game.fromFumbblState(rules, game)

        val star = result.homeTeam.firstOrNull { it.name == "Ripper" }
            ?: error("Star hire missing from extracted team")
        assertEquals(PlayerType.STAR_PLAYER, star.type)
        assertEquals("Ripper Bolgrot", star.position.title)
        // The regular lineman still extracts alongside the star.
        assertTrue(result.homeTeam.any { it.name == "Gnoblar One" && it.type == PlayerType.STANDARD })
    }

    @Test
    fun `genuinely unknown position still fails loudly`() {
        val game = fumbblGame(
            home = fumbblTeam(
                name = "Ogre Home",
                rosterName = "Ogre",
                leagueRule = SpecialRule.BADLANDS_BRAWL,
                positions = listOf(rosterPosition("bogus-1", "Not A Real Position")),
                players = listOf(fumbblPlayer("p1", 1, "bogus-1", "Bogus One")),
            ),
            away = simpleOgreTeam("Ogre Away"),
        )

        val failure = assertFailsWith<IllegalStateException> {
            Game.fromFumbblState(rules, game)
        }
        assertTrue(
            failure.message?.contains("Could not find position 'Not A Real Position'") == true,
            "Unexpected message: ${failure.message}",
        )
    }

    @Test
    fun `fumbbl rename still resolves from the roster before stars`() {
        val game = fumbblGame(
            home = fumbblTeam(
                name = "Tomb Kings Home",
                rosterName = "Tomb Kings",
                leagueRule = SpecialRule.SYLVANIAN_SPOTLIGHT,
                positions = listOf(rosterPosition("blitzer-1", "Anointed Blitzer")),
                players = listOf(fumbblPlayer("p1", 1, "blitzer-1", "Blitzer One")),
            ),
            away = simpleOgreTeam("Ogre Away"),
        )

        val result = Game.fromFumbblState(rules, game)

        val player = result.homeTeam.firstOrNull { it.name == "Blitzer One" }
            ?: error("Renamed position missing from extracted team")
        assertEquals("Tomb Kings Blitzer", player.position.titleSingular)
        assertEquals(PlayerType.STANDARD, player.type)
    }

    private fun simpleOgreTeam(name: String): FumbblTeam = fumbblTeam(
        name = name,
        rosterName = "Ogre",
        leagueRule = SpecialRule.BADLANDS_BRAWL,
        positions = listOf(rosterPosition("away-gnoblar-1", "Gnoblar Lineman")),
        players = listOf(fumbblPlayer("away-p1", 1, "away-gnoblar-1", "Away Gnoblar")),
    )

    private fun rosterPosition(positionId: String, positionName: String) = RosterPlayer(
        positionId = positionId,
        positionName = positionName,
        shorthand = "XX",
        displayName = null,
        playerType = null,
        playerGender = null,
        quantity = 16,
        movement = 5,
        strength = 1,
        agility = 3,
        passing = 4,
        armour = 6,
        cost = 15000,
        race = null,
        undead = false,
        thrall = false,
        teamWithPositionId = null,
        nameGenerator = null,
        replacesPosition = null,
        urlPortrait = null,
        urlIconSet = null,
        nrOfIcons = 0,
        skillCategoriesNormal = emptyList(),
        skillCategoriesDouble = emptyList(),
        skillArray = emptyList(),
        skillValues = emptyList(),
        skillDisplayValues = emptyList(),
    )

    private fun fumbblPlayer(playerId: String, playerNr: Int, positionId: String, name: String) = FumbblPlayer(
        playerKind = "rosterPlayer",
        playerId = playerId,
        playerNr = playerNr,
        positionId = positionId,
        playerName = name,
        playerGender = "",
        playerType = FumbblPlayerType.REGULAR,
        movement = 5,
        strength = 1,
        agility = 3,
        passing = 4,
        armour = 6,
        lastingInjuries = emptyList(),
        recoveringInjury = null,
        urlPortrait = null,
        urlIconSet = null,
        nrOfIcons = 0,
        positionIconIndex = 0,
        skillArray = emptyList(),
        temporarySkillsMap = emptyMap(),
        temporaryModifiersMap = emptyMap(),
        temporaryPropertiesMap = emptyMap(),
        skillValuesMap = emptyMap(),
        skillDisplayValuesMap = emptyMap(),
        usedSkills = emptyList(),
    )

    private fun fumbblTeam(
        name: String,
        rosterName: String,
        leagueRule: SpecialRule,
        positions: List<RosterPlayer>,
        players: List<FumbblPlayer>,
    ) = FumbblTeam(
        teamId = "1",
        teamName = name,
        coach = "coach",
        race = rosterName,
        reRolls = 0,
        apothecaries = 0,
        cheerleaders = 0,
        assistantCoaches = 0,
        fanFactor = 0,
        teamValue = 0,
        treasury = 0,
        baseIconPath = "",
        logoUrl = null,
        dedicatedFans = 0,
        players = players.toTypedArray(),
        specialRules = setOf(leagueRule),
        roster = FumbblRoster(
            rosterId = "1",
            rosterName = rosterName,
            reRollCost = 0,
            maxReRolls = 8,
            baseIconPath = "",
            logoUrl = null,
            raisedPositionId = "",
            apothecary = false,
            necromancer = false,
            undead = false,
            riotousPositionId = null,
            nameGenerator = null,
            maxBigGuys = 1,
            positions = positions.toTypedArray(),
        ),
    )

    private fun turnData(home: Boolean) = TurnData(
        homeData = home,
        turnStarted = false,
        turnNr = 0,
        firstTurnAfterKickoff = false,
        reRolls = 0,
        rerollBrilliantCoachingOneDrive = 0,
        apothecaries = 0,
        blitzUsed = false,
        foulUsed = false,
        reRollUsed = false,
        handOverUsed = false,
        passUsed = false,
        coachBanned = false,
        ktmUsed = false,
        bombUsed = false,
        leaderState = "",
        inducementSet = InducementSet(
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
        ),
        wanderingApothecaries = 0,
        rerollPumpUpTheCrowdOneDrive = 0,
        plagueDoctors = 0,
    )

    private fun teamResult() = TeamResult(
        score = 0,
        conceded = false,
        raisedDead = 0,
        spectators = 0,
        fame = 0,
        winnings = 0,
        fanFactorModifier = 0,
        badlyHurtSuffered = 0,
        seriousInjurySuffered = 0,
        ripSuffered = 0,
        spirallingExpenses = 0,
        playerResults = emptyList(),
        pettyCashFromTvDiff = 0,
        pettyCashTransferred = 0,
        pettyCashUsed = 0,
        teamValue = 0,
        treasuryUsedOnInducements = 0,
        fanFactor = 0,
        dedicatedFans = 0,
        penaltyScore = 0,
    )

    private fun fumbblGame(home: FumbblTeam, away: FumbblTeam) = FumbblGame(
        gameId = 1L,
        scheduled = null,
        started = null,
        finished = null,
        homePlaying = true,
        half = 1,
        homeFirstOffense = true,
        setupOffense = false,
        waitingForOpponent = false,
        turnTime = 0,
        gameTime = 0,
        timeoutPossible = false,
        timeoutEnforced = false,
        concessionPossible = false,
        testing = false,
        turnMode = TurnMode.REGULAR,
        lastTurnMode = null,
        defenderId = null,
        lastDefenderId = null,
        defenderAction = null,
        passCoordinate = null,
        throwerId = null,
        throwerAction = null,
        teamState = FumbblGame.TeamState.FULL,
        teamAway = away,
        teamHome = home,
        turnDataAway = turnData(false),
        turnDataHome = turnData(true),
        fieldModel = FieldModel(
            weather = "",
            ballCoordinate = null,
            ballInPlay = false,
            ballMoving = false,
            bombCoordinate = null,
            bombMoving = false,
            bloodspotArray = mutableListOf(),
            pushbackSquareArray = mutableListOf(),
            moveSquareArray = mutableListOf(),
            trackNumberArray = mutableListOf(),
            diceDecorationArray = mutableListOf(),
            fieldMarkerArray = mutableListOf(),
            playerMarkerArray = mutableListOf(),
            playerDataArray = mutableListOf(),
            trapDoors = mutableListOf(),
        ),
        actingPlayer = ActingPlayer(
            playerId = null,
            currentMove = 0,
            goingForIt = false,
            hasBlocked = false,
            hasFed = false,
            hasFouled = false,
            hasMoved = false,
            hasPassed = false,
            playerAction = null,
            standingUp = false,
            sufferingAnimosity = false,
            sufferingBloodlust = false,
            fumblerooskiePending = false,
            usedSkills = mutableListOf(),
            skillsGrantedBy = mutableMapOf(),
            playerStateOld = null,
        ),
        gameResult = GameResult(teamResult(), teamResult()),
        gameOptions = GameOptions(emptyList()),
        dialogParameter = null,
        concededLegally = false,
    )
}
