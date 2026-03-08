package com.jervisffb.ui.game.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jervis.generated.SettingsKeys
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.DogoutSelected
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.locations.GiantLocation
import com.jervisffb.engine.rules.builder.GameType
import com.jervisffb.engine.rules.common.procedures.SetupTeam
import com.jervisffb.engine.rules.common.procedures.SetupTeamContext
import com.jervisffb.engine.rules.common.procedures.StartOfDriveSequence
import com.jervisffb.engine.serialize.JervisSerialization
import com.jervisffb.engine.serialize.JervisSetupFile
import com.jervisffb.ui.CacheManager
import com.jervisffb.ui.IssueTracker
import com.jervisffb.ui.SETTINGS_MANAGER
import com.jervisffb.ui.SoundEffect
import com.jervisffb.ui.SoundManager
import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.menu.BackNavigationHandler
import com.jervisffb.ui.menu.TeamActionMode
import com.jervisffb.ui.menu.intro.CreditData
import com.jervisffb.ui.utils.saveFile
import com.jervisffb.utils.PROP_UNCAUGHT_ERROR_MESSAGE
import com.jervisffb.utils.PROP_UNCAUGHT_ERROR_STACKTRACE
import com.jervisffb.utils.PROP_UNCAUGHT_ERROR_TITLE
import com.jervisffb.utils.canBeHost
import com.jervisffb.utils.jervisLogger
import com.jervisffb.utils.multiThreadDispatcher
import com.jervisffb.utils.singleThreadDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Map between "Automated Actions" (features) and their corresponding settings keys.
enum class Feature(val settingsKey: String) {
    DO_NOT_REROLL_SUCCESSFUL_ACTIONS(SettingsKeys.JERVIS_AUTO_ACTION_ACTIONS_DO_NOT_REROLL_SUCCESSFUL_ACTIONS_VALUE),
    SELECT_KICKING_PLAYER(SettingsKeys.JERVIS_AUTO_ACTION_ACTIONS_SELECT_KICKING_PLAYER_VALUE),
    END_PLAYER_ACTION_IF_ONLY_OPTION(SettingsKeys.JERVIS_AUTO_ACTION_ACTIONS_END_PLAYER_ACTION_IF_ONLY_OPTION_VALUE),
    SELECT_BLOCK_TYPE_IF_ONLY_OPTION(SettingsKeys.JERVIS_AUTO_ACTION_ACTIONS_SELECT_BLOCK_TYPE_IF_ONE_OPTION_VALUE),
    PUSH_PLAYER_INTO_CROWD(SettingsKeys.JERVIS_AUTO_ACTION_ACTIONS_PUSH_PLAYER_INTO_CROWD_VALUE),
    ALWAYS_USE_BIG_HAND(SettingsKeys.JERVIS_AUTO_ACTION_SKILLS_ALWAYS_USE_BIG_HAND_VALUE),
    ALWAYS_USE_VERY_LONG_LEGS(SettingsKeys.JERVIS_AUTO_ACTION_SKILLS_ALWAYS_USE_VERY_LONG_LEGS_VALUE),
    USE_DIRTY_PLAYER_ON_ARMOUR(SettingsKeys.JERVIS_AUTO_ACTION_SKILLS_USE_DIRTY_PLAYER_ON_ARMOUR_ROLL_VALUE),
    USE_DIRTY_PLAYER_ON_INJURY(SettingsKeys.JERVIS_AUTO_ACTION_SKILLS_USE_DIRTY_PLAYER_ON_INJURY_ROLL_VALUE),
    USE_CATCH_SKILL_REROLL(SettingsKeys.JERVIS_AUTO_ACTION_SKILLS_USE_CATCH_SKILL_REROLL_VALUE),
    USE_PASS_SKILL_REROLL(SettingsKeys.JERVIS_AUTO_ACTION_SKILLS_USE_PASS_SKILL_REROLL_VALUE),
    ALWAYS_USE_SAFE_PASS(SettingsKeys.JERVIS_AUTO_ACTION_SKILLS_ALWAYS_USE_SAFE_PASS_VALUE),
    ALWAYS_USE_TACKLE_ON_STUMBLE(SettingsKeys.JERVIS_AUTO_ACTION_SKILLS_ALWAYS_USE_TACKLE_IN_STUMBLE_VALUE),
    ALWAYS_USE_TACKLE_ON_DODGE(SettingsKeys.JERVIS_AUTO_ACTION_SKILLS_ALWAYS_USE_TACKLE_TO_PREVENT_DODGE_VALUE),
    USE_SURE_HANDS_REROLL(SettingsKeys.JERVIS_AUTO_ACTION_SKILLS_USE_SURE_HAND_REROLL_VALUE),
    USE_SURE_HANDS_ON_STRIP_BALL(SettingsKeys.JERVIS_AUTO_ACTION_SKILLS_USE_SURE_HAND_PUSHBACK_VALUE),
    ALWAYS_USE_THICK_SKULL(SettingsKeys.JERVIS_AUTO_ACTION_SKILLS_ALWAYS_USE_THICK_SKULL_VALUE),
    ALWAYS_USE_LEAP_MODIFIER(SettingsKeys.JERVIS_AUTO_ACTION_SKILLS_ALWAYS_USE_LEAP_MODIFIER_VALUE),
    ALWAYS_USE_SIDESTEP(SettingsKeys.JERVIS_AUTO_ACTION_SKILLS_ALWAYS_USE_SIDESTEP_VALUE),
    ALWAYS_USE_GRAB(SettingsKeys.JERVIS_AUTO_ACTION_SKILLS_ALWAYS_USE_GRAB_VALUE),
    ALWAYS_USE_STAND_FIRM(SettingsKeys.JERVIS_AUTO_ACTION_SKILLS_ALWAYS_USE_STAND_FIRM_VALUE),
    ALWAYS_USE_STEADY_FOOTING(SettingsKeys.JERVIS_AUTO_ACTION_SKILLS_ALWAYS_STEADY_FOOTING_VALUE),
    ALWAYS_USE_STRIP_BALL(SettingsKeys.JERVIS_AUTO_ACTION_SKILLS_ALWAYS_STRIP_BALL_VALUE),
    USE_MIGHTY_BLOW_ON_ARMOUR(SettingsKeys.JERVIS_AUTO_ACTION_SKILLS_USE_MIGHTY_BLOW_ON_ARMOUR_ROLL_VALUE),
    USE_MIGHTY_BLOW_ON_INJURY(SettingsKeys.JERVIS_AUTO_ACTION_SKILLS_USE_MIGHTY_BLOW_ON_INJURY_ROLL_VALUE),
    ALWAYS_USE_STRONG_ARM(SettingsKeys.JERVIS_AUTO_ACTION_SKILLS_ALWAYS_STRONG_ARM_VALUE),
    ALWAYS_USE_SNEAKY_GIT(SettingsKeys.JERVIS_AUTO_ACTION_SKILLS_ALWAYS_USE_SNEAKY_GIT_VALUE),
    ALWAYS_USE_EYE_GOUGE(SettingsKeys.JERVIS_AUTO_ACTION_SKILLS_ALWAYS_USE_EYE_GOUGE_VALUE),
    ALWAYS_USE_SAFE_PAIR_OF_HANDS(SettingsKeys.JERVIS_AUTO_ACTION_SKILLS_ALWAYS_USE_SAFE_PAIR_OF_HANDS_VALUE),
}

data class ErrorDialog(
    val visible: Boolean,
    val title: String,
    val error: Throwable? = null,
)

data class ReportIssueDialogData(
    val visible: Boolean,
    val title: String,
    val body: String,
    val hint: String = "",
    val error: Throwable? = null,
    val gameState: GameEngineController? = null
) {
    companion object {
        val HIDDEN = ReportIssueDialogData(false, "", "")
    }
}

/**
 * View Model controlling the full Jervis Menu System. This probably has too much responsibility right now,
 * but we do need something that cuts across a lot of concerns since the menu can impact all parts of the UI.
 */
class MenuViewModel {
    companion object {
        val LOG = jervisLogger()
    }

    // Some place to store the last exception thrown by the Game Engine (if any)
    var lastActionException: Throwable? = null

    // Expose a flow
    val setupAvailable: MutableStateFlow<GameType?> = MutableStateFlow(null)

    var controller: GameEngineController? = null
    lateinit var uiState: UiGameController

    val p2pHostAvaiable: Boolean = canBeHost()

    val isSettingsDialogVisible: StateFlow<Boolean>
        field = MutableStateFlow(false)

    val isAboutDialogVisible: StateFlow<Boolean>
        field = MutableStateFlow<Boolean>(false)

    val isErrorDialogVisible: StateFlow<ErrorDialog>
        field = MutableStateFlow(ErrorDialog(false, "",))

    val isReportIssueDialogVisible: StateFlow<ReportIssueDialogData>
        field = MutableStateFlow(ReportIssueDialogData.HIDDEN)

    val creditData: CreditData = CreditData()

    // Scope for lauching tasks directly related to navigating the UI
    val uiScope = CoroutineScope(CoroutineName("UI") + Dispatchers.Default)
    val navigatorContext = CoroutineScope(CoroutineName("ScreenNavigator") + singleThreadDispatcher("menuThread"))
    // Scope for launching background tasks for Menu actions
    val backgroundContext = CoroutineScope(SupervisorJob() + CoroutineName("ScreenBackground") + multiThreadDispatcher("menuBackgroundThread"))

    var enableAutomatedActions by mutableStateOf(true)

    init {
        try {
            if (SETTINGS_MANAGER.hasKey(PROP_UNCAUGHT_ERROR_TITLE)) {
                val dialogData = ReportIssueDialogData(
                    visible = true,
                    title = SETTINGS_MANAGER.getStringOrNull(PROP_UNCAUGHT_ERROR_TITLE) ?: "Uncaught application error",
                    body = buildString {
                        appendLine(SETTINGS_MANAGER.getStringOrNull(PROP_UNCAUGHT_ERROR_MESSAGE) ?: "")
                        appendLine()
                        appendLine("````")
                        appendLine(SETTINGS_MANAGER.getStringOrNull(PROP_UNCAUGHT_ERROR_STACKTRACE) ?: "")
                        appendLine("````")
                    },
                    hint = "Jervis crashed unexpectedly last time it was used. Please report this issue to the developer.",
                    error = null,
                    gameState = null
                )
                isReportIssueDialogVisible.value = dialogData
            }
        } catch (_: Throwable) {
            /* Ignore, we should never crash when reporting previous errors */
        } finally {
            IssueTracker.clearUncaughtException()
        }
    }

    fun showAboutDialog(visible: Boolean) {
        isAboutDialogVisible.value = visible
    }

    fun showErrorDialog(message: String, error: Throwable? = null) {
        isErrorDialogVisible.value = ErrorDialog(
            visible = true,
            title = message,
            error = error,
        )
    }

    fun hideErrorDialog() {
        isErrorDialogVisible.value = ErrorDialog(visible = false, title = "",)
    }

    fun showReportIssueDialog(title: String, body: String, error: Throwable? = null, gameState: GameEngineController? = null) {
        isReportIssueDialogVisible.value = ReportIssueDialogData(
            visible = true,
            title = title,
            body = body,
            error = error,
            gameState = gameState
        )
    }

    fun hideReportIssueDialog() {
        isReportIssueDialogVisible.value = ReportIssueDialogData.HIDDEN
    }

    fun backToLastScreen() {
        BackNavigationHandler.execute()
    }

    fun openSettings(bool: Boolean = true) {
        isSettingsDialogVisible.value = bool
    }

    fun serializeGameState(includeDebugState: Boolean): String {
        return JervisSerialization.serializeGameStateToJson(controller!!, includeDebugState)
    }

    fun undoAction() {
        val team = when (uiState.uiMode) {
            TeamActionMode.HOME_TEAM -> uiState.state.homeTeam.id
            TeamActionMode.AWAY_TEAM -> uiState.state.awayTeam.id
            TeamActionMode.ALL_TEAMS -> null // No team restrictions when undoing on a Client controlling both teams
        }
        if (uiState.gameController.isUndoAvailable(team = team)) {
            uiState.userSelectedAction(Undo)
        } else {
            SoundManager.play(SoundEffect.ERROR)
        }
    }

    fun isFeatureEnabled(feature: Feature): Boolean {
        if (!enableAutomatedActions) return false
        val isUndoing = controller?.lastActionWasUndo() ?: false
        return !isUndoing && SETTINGS_MANAGER.getBoolean(feature.settingsKey, false)
    }

    fun loadSetup(setup: JervisSetupFile) {
        if (setup.gameType == GameType.DUNGEON_BOWL) error("Dungeon Bowl Setups not supported yet")
        if (controller == null) {
            // It shouldn't be possible to call "Load Setup" with out a game controller,
            // but just in case it happens.
            LOG.w { "Load Setup called before game was started." }
            SoundManager.play(SoundEffect.ERROR)
            return
        }
        val allowedTeam = when (uiState.uiMode) {
            TeamActionMode.HOME_TEAM -> uiState.state.homeTeam.id
            TeamActionMode.AWAY_TEAM -> uiState.state.awayTeam.id
            TeamActionMode.ALL_TEAMS -> null // No team restrictions when undoing on a Client controlling both teams
        }
        val game = controller ?: error("No game controller was found")
        val team = game.state.getContext<SetupTeamContext>().team
        if (allowedTeam != null && allowedTeam != team.id) {
            // This client is not considered the "active" client, which means it isn't allowed
            // to load setup formations
            SoundManager.play(SoundEffect.ERROR)
            return
        }

        val rules = game.rules
        val setupActions = setup.formation.flatMap { (playerNo, relativeCoordinate) ->
            // Ignore player setup if either player or coordinate is not valid
            val playerAvailable = (
                team.noToPlayer.contains(playerNo)
                    && ((team[playerNo].state == PlayerState.RESERVE) || (team[playerNo].state == PlayerState.STANDING))
            )

            // Map to field coordinate
            val fieldCoordinate = when (team.isHomeTeam()) {
                true -> {
                    val x = rules.lineOfScrimmageHome - relativeCoordinate.dist
                    val y = relativeCoordinate.y
                    FieldCoordinate(x, y)
                }
                false -> {
                    val x = rules.lineOfScrimmageAway + relativeCoordinate.dist
                    val y = relativeCoordinate.y
                    FieldCoordinate(x, y)
                }
            }
            val isValidCoordinate = rules.isInSetupArea(team, fieldCoordinate)

            if (playerAvailable && isValidCoordinate) {
                listOf(
                    PlayerSelected(team[playerNo].id),
                    FieldSquareSelected(fieldCoordinate)
                )
            } else {
                emptyList()
            }
        }.let { setupActions ->
            // Applying a setup to a random game state is tricky, so we attempt
            // to start from a known state by resetting the state. This is done
            // by first moving all players back to the dogout.
            // This is a best effort attempt, but invalid setups should not crash the game,
            // Instead they should show up as errors in the Developer Console in the in-game
            // menu.
            val resetStateActions = buildList {
                val context = game.state.getContext<SetupTeamContext>()

                // In some cases, a player was already selected when starting a setup.
                // So we need to take that into account as well.
                if (controller?.currentNode() == SetupTeam.PlacePlayer) {
                    val deselectAction = when (context.currentPlayer!!.location) {
                        DogOut -> DogoutSelected
                        is FieldCoordinate -> FieldSquareSelected(context.currentPlayer!!.coordinates)
                        is GiantLocation -> TODO()
                    }
                    add(deselectAction)
                }

                // Move any players on the field back to the dogout.
                context.team.forEach { player ->
                    if (player.location.isOnField(rules)) {
                        add(PlayerSelected(player.id))
                        add(DogoutSelected)
                    }
                }
            }
            resetStateActions + setupActions
        }

        // Treat the entire setup as one action. Which makes it easy to undo again
        uiState.userSelectedAction(CompositeGameAction(setupActions))
    }

    // Called by the UiGameController whenever a new snapshot is created. This can be used to determine
    // which menu actions should be enabled/disabled.
    fun updateUiState(uiSnapshot: UiGameSnapshot) {
        // Enable/Disable Setup options
        val setupKickingTeam = uiSnapshot.stack.containsNode(StartOfDriveSequence.SetupKickingTeam)
        val setupReceivingTeam = uiSnapshot.stack.containsNode(StartOfDriveSequence.SetupReceivingTeam)
        val teamControlledByClient = when (uiState.uiMode) {
            TeamActionMode.HOME_TEAM -> setupKickingTeam && uiSnapshot.game.kickingTeam.isHomeTeam()
            TeamActionMode.AWAY_TEAM -> setupReceivingTeam && uiSnapshot.game.receivingTeam.isHomeTeam()
            TeamActionMode.ALL_TEAMS -> true
        }
        if ((setupReceivingTeam || setupKickingTeam) && teamControlledByClient) {
            setupAvailable.value = uiState.rules.gameType
        } else {
            setupAvailable.value = null
        }
    }

    fun showSaveGameDialog(includeDebugState: Boolean = false) {
        saveFile(
            dialogTitle = "Save Game File",
            fileName = JervisSerialization.getGameFileName(uiState.gameController, includeDebugState),
            fileContent = serializeGameState(includeDebugState),
        )
    }
}

/**
 * Object responsible for managing team setups.
 * For now, we only load Setup files once. You need to restart the application
 * to re-fill the cache. This approach should probably be refactored at some
 * point, but having setups in-memory makes them a lot faster to access
 */
object Setups {

    private val setups = mutableSetOf<JervisSetupFile>()

    suspend fun initialize() {
        val fileSetups = CacheManager.loadSetups()
        this.setups.addAll(fileSetups)
    }

    fun getSetups(type: GameType): List<JervisSetupFile> {
        return setups
            .filter { it.gameType == type }
            .sortedBy { it.name }
    }
}



