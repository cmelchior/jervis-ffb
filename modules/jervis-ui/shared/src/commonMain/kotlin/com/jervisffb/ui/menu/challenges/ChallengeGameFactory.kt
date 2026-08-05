package com.jervisffb.ui.menu.challenges

import cafe.adriel.voyager.navigator.Navigator
import com.jervisffb.engine.GameSettings
import com.jervisffb.engine.challenge.Challenge
import com.jervisffb.ui.game.UiGameClientType
import com.jervisffb.ui.game.state.LocalActionProvider
import com.jervisffb.ui.game.state.ManualActionProvider
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.ChallengeGame
import com.jervisffb.ui.menu.GameScreen
import com.jervisffb.ui.menu.GameScreenModel
import com.jervisffb.ui.menu.TeamActionMode

/**
 * Turns a [Challenge] into a running game.
 *
 * The coach drives both teams, the same way a hotseat game works. For a
 * challenge bounded by a turn limit, the challenge resolves before the opponent
 * gets a turn, so there is nothing to hand over to.
 *
 * Every call runs [Challenge.setup] again and produces a completely fresh game.
 */
object ChallengeGameFactory {

    fun createScreenModel(menuViewModel: MenuViewModel, challenge: Challenge): GameScreenModel {
        return createScreenModel(menuViewModel, challenge, challenge, attachToMenu = true)
    }

    /**
     * Builds a game that exists only to be looked at. E.g., preview on the
     * challenge details page.
     *
     * The caller owns the result and must call `onDispose()` when it is done
     * with it.
     */
    fun createPreviewScreenModel(menuViewModel: MenuViewModel, challenge: Challenge): GameScreenModel {
        return createScreenModel(menuViewModel, challenge, challenge, attachToMenu = false)
    }

    private fun createScreenModel(
        menuViewModel: MenuViewModel,
        challenge: Challenge,
        setup: Challenge,
        attachToMenu: Boolean,
    ): GameScreenModel {
        val gameController = setup.createGame()
        val gameSettings = GameSettings(gameRules = gameController.rules, isHotseatGame = true)
        val actionProvider = LocalActionProvider(
            gameController,
            gameSettings,
            ManualActionProvider(gameController, menuViewModel, TeamActionMode.HOME_TEAM, gameSettings),
            ManualActionProvider(gameController, menuViewModel, TeamActionMode.AWAY_TEAM, gameSettings),
        )
        return GameScreenModel(
            uiClientType = UiGameClientType.HOTSEAT,
            uiMode = TeamActionMode.ALL_TEAMS,
            gameController = gameController,
            homeTeam = gameController.state.homeTeam,
            awayTeam = gameController.state.awayTeam,
            actionProvider = actionProvider,
            mode = ChallengeGame(challenge),
            menuViewModel = menuViewModel,
            onEngineInitialized = {
                // A preview is never "the current game", so it does not claim
                // the menu's controller. GameScreenModel skips this entirely
                // when initialized with attachToMenu = false; the guard here is
                // for clarity at the call site.
                if (attachToMenu) {
                    menuViewModel.controller = gameController
                }
            },
        ).also {
            it.gameAcceptedByAllPlayers()
        }
    }

    // Starts a challenge, pushing the game screen on top of the challenge details.s
    fun start(navigator: Navigator, menuViewModel: MenuViewModel, challenge: Challenge) {
        val model = createScreenModel(menuViewModel, challenge)
        navigator.push(GameScreen(menuViewModel, model))
    }

    /**
     * Restarts a challenge from its starting position, replacing the running
     * game. Voyager disposes of the replaced game.
     */
    fun restart(navigator: Navigator, menuViewModel: MenuViewModel, challenge: Challenge) {
        val model = createScreenModel(menuViewModel, challenge)
        navigator.replace(GameScreen(menuViewModel, model))
    }
}
