package dk.ilios.jervis.rules.roster

interface Race {
    val id: Long
    val name: String
}

interface Roster {
    val name: String
    val numberOfRerolls: Int
    val apothecary: Boolean
    val positions: List<Position>
}






interface BB20216Roster: Roster {

}

