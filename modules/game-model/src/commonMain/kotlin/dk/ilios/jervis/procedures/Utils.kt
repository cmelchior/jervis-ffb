package dk.ilios.jervis.procedures

import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.SetPlayerRushesLeft
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.hasSkill
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.Sprint

/**
 * Returns the [Command] for setting the available number of rushes for this action.
 */
fun getSetPlayerRushesCommand(rules: Rules, player: Player): Command {
    val rushesPrAction = if (player.hasSkill<Sprint>()) rules.rushesPrAction + 1 else rules.rushesPrAction
    return SetPlayerRushesLeft(player, rushesPrAction)
}


