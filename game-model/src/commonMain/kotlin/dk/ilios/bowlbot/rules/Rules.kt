package dk.ilios.bowlbot.rules

interface Rules {
    val halfsPrGame: Int
        get() = 2
    val turnsPrHalf: Int
        get() = 8
}