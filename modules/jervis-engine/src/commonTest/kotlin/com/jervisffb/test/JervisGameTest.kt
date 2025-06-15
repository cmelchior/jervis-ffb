package com.jervisffb.test

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.engine.rules.bb2020.procedures.FullGame
import com.jervisffb.engine.rules.bb2020.skills.SkillType
import com.jervisffb.engine.rules.builder.UndoActionBehavior
import com.jervisffb.test.ext.rollForward
import kotlin.test.BeforeTest

/**
 * Abstract class for tests that involving testing the flow of
 * events during a real game.
 *
 * This class makes it easier to setup and manipulate the
 */
abstract class JervisGameTest {

    open val rules: Rules = StandardBB2020Rules().toBuilder().run {
        undoActionBehavior = UndoActionBehavior.ALLOWED
        build()
    }
    protected lateinit var state: Game
    protected lateinit var controller: GameEngineController
    protected lateinit var homeTeam: Team
    protected lateinit var awayTeam: Team

    @BeforeTest
    open fun setUp() {
        homeTeam = createDefaultHomeTeam(rules)
        awayTeam = humanTeamAway(rules)
        state = createDefaultGameState(rules, homeTeam, awayTeam).apply {
            // Should be on LoS
            homeTeam[PlayerNo(1)].apply {
                addSkill(SkillType.BREAK_TACKLE.id())
                strength = 4
            }
            // Should be on LoS
            homeTeam[PlayerNo(2)].apply {
                addSkill(SkillType.BREAK_TACKLE.id())
                strength = 5
            }
        }
        homeTeam = state.homeTeam
        awayTeam = state.awayTeam
        controller = GameEngineController(state)
        controller.startTestMode(FullGame)
    }

    fun startDefaultGame() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(),
        )
    }

    protected fun useTeamReroll(controller: GameEngineController) =
        RerollOptionSelected(
            controller.getAvailableActions().actions.filterIsInstance<SelectRerollOption>().first().options.first()
        )
}
