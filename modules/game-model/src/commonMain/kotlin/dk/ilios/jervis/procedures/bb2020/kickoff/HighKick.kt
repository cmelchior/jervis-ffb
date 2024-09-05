package dk.ilios.jervis.procedures.bb2020.kickoff

import compositeCommandOf
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.reports.LogCategory
import dk.ilios.jervis.reports.SimpleLogEntry
import dk.ilios.jervis.rules.Rules

/**
 * Procedure for handling the Kick-Off Event: "HighKick" as described on page 41
 * of the rulebook.
 */
// For High Kick:
// Following the strict ordering of the rules, the Kick-Off Event is resolved
// before "What Goes Up, Must Come Down". This means that the touchback rule cannot
// yet be applied when High Kick is resolved. Also, no-where is it stated that
// the high kick player cannot enter the opponents field. So in theory it would be
// allowed to move a player into the opponents field, resolve the ball coming down,
// which would result in a touchback. And then automatically give it to the player
// who was moved onto the opponents field.
//
// However, this seems against the spirits of the rules and are probably an oversight,
// So disallowing it for now unless someone can surface an official reference that
// contradicts this.
//
// Another node: If it is just rules that are unclear, and the touchback is awarded as soon as
// the ball leaves the kicking teams half, then this also impacts things like Blitz,
// where you
object HighKick : Procedure() {
    override val initialNode: Node = GiveBribe

    override fun onEnterProcedure(
        state: Game,
        rules: Rules,
    ): Command? = null

    override fun onExitProcedure(
        state: Game,
        rules: Rules,
    ): Command? = null

    object GiveBribe : ComputationNode() {
        // TODO Figure out how to do this
        override fun apply(
            state: Game,
            rules: Rules,
        ): Command {
            return compositeCommandOf(
                SimpleLogEntry("Do High Kick!", category = LogCategory.GAME_PROGRESS),
                ExitProcedure(),
            )
        }
    }
}
