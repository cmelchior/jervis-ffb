package com.jervisffb.engine.rules.builder

import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.rules.bb2020.procedures.TheFUMBBLKickOff
import com.jervisffb.engine.rules.bb2020.procedures.TheKickOff
import com.jervisffb.engine.rules.bb2020.procedures.actions.foul.FoulAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.foul.FumbblFoulAction
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.UseBB11Apothecary
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.UseBB7Apothecary

enum class DiceRollOwner {
    // The server logic is responsible for rolling the dice.
    ROLL_ON_SERVER,
    // Client is responsible for controlling the dice roll by either
    // letting the user choose the result or rolling behind the users
    // back.
    ROLL_ON_CLIENT
}

enum class UndoActionBehavior {
    NOT_ALLOWED,
    ONLY_NON_RANDOM_ACTIONS,
    ALLOWED
}

// Interface describing enums that contains a reference to a Procedure.
// This is mostly a work-around, so we avoid referencing procedures directly
// in JSON. While doing this, wouldn't be _that_ bad since procedures should be
// stateless. It does force us to register Procedures as a polymorphic serialization
// class in the serialization module, which is annoying to maintain.
//
// WARNING: This means we can change procedures referenced without breaking
// already serialized games, but if that changes the event flow, it is a
// breaking change.
interface ProcedureHolder {
    val procedure: Procedure
}

enum class FoulActionBehavior(override val procedure: Procedure) : ProcedureHolder {
    STRICT(FoulAction), // Select target when starting the foul action
    FUMBBL(FumbblFoulAction), // Select the foul player, just before rolling for the foul
}

enum class KickingPlayerBehavior(override val procedure: Procedure): ProcedureHolder {
    STRICT(TheKickOff), // Player should be selected by the Client
    FUMBBL(TheFUMBBLKickOff) // Player is selected automatically by the server.
}

enum class UseApothecaryBehavior(override val procedure: Procedure): ProcedureHolder {
    STANDARD(UseBB11Apothecary), // Use the standard apothecary behavior from the rulebook.
    BB7(UseBB7Apothecary) // Use the BB7 apothecary behavior from Death Zone.
}
