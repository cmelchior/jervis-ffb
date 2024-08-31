package dk.ilios.jervis.skills

import dk.ilios.jervis.actions.RerollOptionSelected
import dk.ilios.jervis.actions.SelectRerollOption
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerNo
import dk.ilios.jervis.rules.BB2020Rules
import dk.ilios.jervis.rules.skills.BreakTackle
import dk.ilios.jervis.utils.createDefaultGameState
import dk.ilios.jervis.utils.setupTeamsOnField
import kotlin.test.BeforeTest

abstract class SkillTests {

    val rules = BB2020Rules
    lateinit var state: Game
    lateinit var controller: GameController

    @BeforeTest
    open fun setUp() {
        state = createDefaultGameState(rules).apply {
            // Should be on LoS
            homeTeam[PlayerNo(1)]!!.apply {
                addSkill(BreakTackle.Factory.createSkill())
                baseStrenght = 4
            }
            // Should be on LoS
            homeTeam[PlayerNo(2)]!!.apply {
                addSkill(BreakTackle.Factory.createSkill())
                baseStrenght = 5
            }
        }
        controller = GameController(rules, state)
        setupTeamsOnField(controller)
    }

    protected fun useTeamReroll(controller: GameController) =
        RerollOptionSelected(
            controller.getAvailableActions().filterIsInstance<SelectRerollOption>().first().option
        )

    protected fun execute(vararg commands: Command) {
        commands.forEach {
            it.execute(state, controller)
        }
    }
}
