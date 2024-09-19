package dk.ilios.jervis.skills

import dk.ilios.jervis.JervisGameTest
import dk.ilios.jervis.actions.CalculatedAction
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.DiceResults
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.MoveType
import dk.ilios.jervis.actions.MoveTypeSelected
import dk.ilios.jervis.actions.PlayerActionSelected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.RerollOptionSelected
import dk.ilios.jervis.defaultKickOffHomeTeam
import dk.ilios.jervis.defaultPregame
import dk.ilios.jervis.defaultSetup
import dk.ilios.jervis.ext.d6
import dk.ilios.jervis.ext.playerId
import dk.ilios.jervis.model.locations.FieldCoordinate
import dk.ilios.jervis.model.PlayerNo
import dk.ilios.jervis.model.context.DodgeRollContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.model.modifiers.BreakTackleModifier
import dk.ilios.jervis.procedures.FullGame
import dk.ilios.jervis.rules.PlayerActionType
import dk.ilios.jervis.rules.skills.BreakTackle
import dk.ilios.jervis.rules.skills.DiceRerollOption
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


/**
 * Class testing usage of the [BreakTackle] skill
 */
class BreakTackleTests: JervisGameTest() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        state.apply {
            // Should be on LoS
            awayTeam[PlayerNo(1)].apply {
                addSkill(BreakTackle())
                baseStrenght = 4
            }
        }
    }

    @Test
    fun useBreakTackleOnDodge() {
        val player = state.getPlayerById("A1".playerId)
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(),
            // Dodge A1 away using Break Tackle
            PlayerSelected(player),
            PlayerActionSelected(PlayerActionType.MOVE),
            MoveTypeSelected(MoveType.STANDARD),
            FieldSquareSelected(FieldCoordinate(14, 5)),
            DiceResults(2.d6), // Dodge roll, is not enough
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
        val player = state.getPlayerById("A1".playerId)
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(),
            // Dodge A1 away using Break Tackle
            PlayerSelected(player),
            PlayerActionSelected(PlayerActionType.MOVE),
            MoveTypeSelected(MoveType.STANDARD),
            FieldSquareSelected(FieldCoordinate(14, 5)),
            DiceResults(1.d6), // Dodge roll, is not enough
            Confirm, // Use Break Tackle, still not enough
            CalculatedAction { state, rules ->
                val context = state.getContext<DodgeRollContext>()
                val roll = context.roll!!
                val reroll = context.player.team.availableRerolls.first()
                RerollOptionSelected(DiceRerollOption(reroll, listOf(roll)))
            },
            DiceResults(2.d6), // Dodge roll, should now succeed
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
