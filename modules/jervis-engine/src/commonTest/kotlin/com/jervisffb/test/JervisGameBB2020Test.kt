package com.jervisffb.test

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.engine.rules.builder.UndoActionBehavior
import com.jervisffb.engine.rules.common.procedures.FullGame
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.bb2020.createDefaultGameStateBB2020
import com.jervisffb.test.bb2020.createDefaultHomeTeamBB2020
import com.jervisffb.test.bb2020.humanTeamAwayBB2020
import com.jervisffb.test.ext.rollForward
import kotlin.test.BeforeTest

/**
 * Abstract class for tests that involving testing the flow of events during a
 * real game. This class is specific for the BB2020 ruleset.
 */
abstract class JervisGameBB2020Test {

    open val rules: Rules = StandardBB2020Rules().toBuilder().run {
        undoActionBehavior = UndoActionBehavior.ALLOWED
        build()
    }
    protected open lateinit var state: Game
    protected open lateinit var controller: GameEngineController
    protected open lateinit var homeTeam: Team
    protected open lateinit var awayTeam: Team

    @BeforeTest
    open fun setUp() {
        homeTeam = createDefaultHomeTeamBB2020(rules)
        awayTeam = humanTeamAwayBB2020(rules)
        state = createDefaultGameStateBB2020(rules, homeTeam, awayTeam).apply {
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
}
