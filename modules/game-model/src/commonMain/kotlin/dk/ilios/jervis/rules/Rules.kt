package dk.ilios.jervis.rules

interface Rules {
    // Game length setup

    val halfsPrGame: Int
        get() = 2
    val turnsPrHalf: Int
        get() = 8


    // Field description

    // Total width of the field
    val fieldWidth
        get() = 26

    // Total height of the field
    val fieldHeight
        get() = 15

    // Height of the Wide Zone at the top and bottom of the field
    val wideZone
        get() = 4

    // Width of the End Zone at each end of the field
    val endZone
        get() = 1

    // From end of field (including endZone)
    val lineOfScrimmage
        get() = 13

    // Blood Bowl 7
    // Total width of the field
//    val fieldWidth = 2
//    val fieldHeight = 11
//    val wideZone = 2
//    val endZone = 1
//    val lineOfScrimmage = 7
}