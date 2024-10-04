package dk.ilios.jervis.model.inducements

enum class Timing {
    START_OF_TURN, // Before any players are activated
    END_OF_TURN, // After own turn "has ended(?)"
    END_OF_OPPONENT_TURN, // After "opponent's turn has ended(?)"
    START_OF_OPPONENT_TURN, // Before any players are activated
    END_OF_DRIVE, // During step 3 in End Of Drive Sequence
    START_OF_DRIVE,
    SPECIAL // The timing effect cannot easily be handled generically and needs to be manually checked
}
