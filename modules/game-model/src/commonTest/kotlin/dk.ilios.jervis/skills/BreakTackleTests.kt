package dk.ilios.jervis.skills

import dk.ilios.jervis.GameFlowTests
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.DiceResults
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.ext.d6
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.PlayerNo
import dk.ilios.jervis.model.context.DodgeRollContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.model.modifiers.BreakTackleModifier
import dk.ilios.jervis.procedures.actions.move.DodgeRoll
import dk.ilios.jervis.rules.skills.BreakTackle
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


/**
 * Class testing usage of the [BreakTackle] skill
 */
class BreakTackleTests: GameFlowTests() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        state.apply {
            // Should be on LoS
            homeTeam[PlayerNo(1)]!!.apply {
                addSkill(BreakTackle.Factory.createSkill())
                baseStrenght = 4
            }
        }
    }

    @Test
    fun useBreakTackleOnDodge() {
        val player = state.homeTeam[PlayerNo(1)]!!
        execute(SetContext(DodgeRollContext(
            player = player,
            startingSquare = FieldCoordinate(12, 5),
            targetSquare = FieldCoordinate(11, 5),
        )))
        controller.startTestMode(DodgeRoll)
        controller.rollForward(
            DiceResults(2.d6), // Dodge roll
            Confirm // Use Break Tackle
        )

        val context = state.getContext<DodgeRollContext>()
        assertEquals(1, context.rollModifiers.size)
        assertEquals(BreakTackleModifier(player.strength), context.rollModifiers.first())
        assertTrue(context.isSuccess)
        assertTrue(player.getSkill<BreakTackle>().used)
    }

    @Test
    fun breakTackleAlsoAppliesToReroll() {
        val player = state.homeTeam[PlayerNo(1)]!!
        execute(SetContext(DodgeRollContext(
            player = player,
            startingSquare = FieldCoordinate(12, 5),
            targetSquare = FieldCoordinate(11, 5),
        )))
        controller.startTestMode(DodgeRoll)

        controller.rollForward(
            DiceResults(1.d6), // Dodge roll
            Confirm, // Use Break Tackle
        )
        controller.rollForward(
            useTeamReroll(controller), // Team reroll
            DiceResults(2.d6), // Dodge reroll
        )
        val context = state.getContext<DodgeRollContext>()
        assertEquals(1, context.rollModifiers.size)
        assertEquals(BreakTackleModifier(player.strength), context.rollModifiers.first())
        assertTrue(player.getSkill<BreakTackle>().used)
        assertTrue(context.isSuccess)
    }

    @Test
    fun breakTackleModifierForS4() {
        assertEquals(1, BreakTackleModifier(4).modifier)
    }

    @Test
    fun breakTackleModifierForS5() {
        assertEquals(2, BreakTackleModifier(5).modifier)
    }
}
