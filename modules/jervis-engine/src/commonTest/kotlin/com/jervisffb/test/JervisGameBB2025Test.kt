package com.jervisffb.test

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.StandardBB2025Rules
import com.jervisffb.engine.rules.bb2020.procedures.FullGame
import com.jervisffb.engine.rules.builder.UndoActionBehavior
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.bb2025.createDefaultGameStateBB2025
import com.jervisffb.test.bb2025.createDefaultHomeTeamBB2025
import com.jervisffb.test.bb2025.humanTeamAwayBB2025
import com.jervisffb.test.ext.rollForward
import kotlin.test.BeforeTest

/**
 * Abstract class for tests that involving testing the flow of events during a
 * real game. This class is specific for the BB2025 ruleset.
 */
abstract class JervisGameBB2025Test {

    open val rules: Rules = StandardBB2025Rules().toBuilder().run {
        undoActionBehavior = UndoActionBehavior.ALLOWED
        build()
    }
    protected lateinit var state: Game
    protected lateinit var controller: GameEngineController
    protected lateinit var homeTeam: Team
    protected lateinit var awayTeam: Team

    @BeforeTest
    open fun setUp() {
        homeTeam = createDefaultHomeTeamBB2025(rules)
        awayTeam = humanTeamAwayBB2025(rules)
        state = createDefaultGameStateBB2025(rules, homeTeam, awayTeam).apply {
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
