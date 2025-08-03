package manual

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.model.DieId
import com.jervisffb.engine.model.Field
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.ui.createDefaultAwayTeam
import com.jervisffb.ui.createDefaultHomeTeam
import com.jervisffb.ui.game.dialogs.circle.ActionWheelViewModel
import com.jervisffb.ui.game.dialogs.circle.DiceMenuItem
import com.jervisffb.ui.game.dialogs.circle.MenuExpandMode
import com.jervisffb.ui.game.icons.ActionIcon
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.view.ActionWheelMenu
import com.jervisffb.utils.runBlocking
import org.junit.Test
import kotlin.test.Ignore

@Ignore // Must run manually
class AnimateTest() {
    @Test
    fun runTest() {
        val rules = StandardBB2020Rules()
        val game = Game(
            rules,
            createDefaultHomeTeam(rules),
            createDefaultAwayTeam(rules),
            Field.createForRuleset(rules),
        )
        val actionWheel = createActionWheel(game.homeTeam)
        application {
            val density = LocalDensity.current
            runBlocking {
                IconFactory.initializeFumbblMapping()
                IconFactory.initialize(density, game.homeTeam, game.awayTeam)
            }
            val windowState = rememberWindowState()
            Window(onCloseRequest = ::exitApplication, state = windowState) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    // BackgroundImageLayer(FieldDetails.NICE)
                    Box(
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        CircularMenuDemo(actionWheel)
                    }
                }
            }
        }
    }

    private fun createActionWheel(team: Team): ActionWheelViewModel {
        val actionWheel = ActionWheelViewModel(
            team = team,
            center = null,
        )
        actionWheel.topMenu.apply {
            addDiceButton(
                id = DieId("1"),
                diceValue = 1.d6,
                options = D6Result.allOptions(),
                preferLtr = true,
                expandable = true,
                animatingFrom = 6.d6,
            )
            addDiceButton(
                id = DieId("2"),
                diceValue = 1.dblock,
                options = DBlockResult.allOptions(),
                preferLtr = false,
                expandable = true,
                animatingFrom = 4.dblock,
            )
        }
        actionWheel.bottomMenu.apply {
            addActionButton(
                label = { "Pro" },
                icon = ActionIcon.BLOCK,
                onClick = { parent, item ->
                    /* TODO */
                },
                expandMode = MenuExpandMode.TWO_WAY,
            ).apply {
                addActionButton(
                    label = { "Cancel" },
                    icon = ActionIcon.CANCEL,
                    onClick = { parent, item ->
                        // parent!!.closeSubMenu()
                    }
                )
                addActionButton(
                    label = { "Roll" },
                    icon = ActionIcon.CONFIRM,
                    onClick = { parent, item ->
                        // actionWheel.rolldice()
                        // val dice = actionWheel.rolldice()
                    }
                )
            }
            addActionButton(
                label = { "Team Reroll" },
                icon = ActionIcon.TEAM_REROLL,
                onClick = { parent, item ->
                    when (val item = actionWheel.topMenu.menuItems.random()) {
                        is DiceMenuItem<*> -> {
                            item.animationDone = false
                        }
                        else -> { /* Do nothing */ }
                    }
                }
            )
        }
        actionWheel.topMessage = "Use Dodge?"
        return actionWheel
    }
}

@Composable
fun CircularMenuDemo(actionWheel: ActionWheelViewModel) {
    ActionWheelMenu(actionWheel)
}
