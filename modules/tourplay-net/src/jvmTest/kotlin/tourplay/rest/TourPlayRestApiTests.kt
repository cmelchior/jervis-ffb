package tourplay.rest

import com.jervisffb.engine.bb2020.FumbblBB2020Rules
import com.jervisffb.engine.bb2025.BB7Rules2025
import com.jervisffb.engine.bb2025.StandardBB2025Rules
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.serialization.SerializedTeam
import com.jervisffb.tourplay.TourPlayApi
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TourPlayRestApiTests {

    private lateinit var api: TourPlayApi

    @BeforeTest
    fun setUp() {
        api = TourPlayApi()
    }

    @Test
    fun teamLoader() = runBlocking {
        val rules = FumbblBB2020Rules()
        val file = api.loadRoster(44442, rules)
        val team = SerializedTeam.deserialize(rules, file.getOrThrow().team, Coach.UNKNOWN)
        assertEquals(team.name, "Lustrian Hurricanes")
    }

    @Test
    fun teamLoader2() = runBlocking {
        val rules = FumbblBB2020Rules()
        val file = api.loadRoster(131784, rules)
        val team = SerializedTeam.deserialize(rules, file.getOrThrow().team, Coach.UNKNOWN)
        assertEquals(team.name, "Gramps' Vamps")
    }

    // Test for https://github.com/cmelchior/jervis-ffb/issues/61
    @Test
    fun load214739() = runBlocking {
        val rules = StandardBB2025Rules()
        val file = api.loadRoster(214739, rules)
        val team = SerializedTeam.deserialize(rules, file.getOrThrow().team, Coach.UNKNOWN)
        assertEquals("Hafenland Capers", team.name)
    }

    // Test for https://github.com/cmelchior/jervis-ffb/issues/80 (BB7 Team)
    @Test
    fun load227396() = runBlocking {
        val rules = BB7Rules2025()
        val file = api.loadRoster(227396, rules)
        val team = SerializedTeam.deserialize(rules, file.getOrThrow().team, Coach.UNKNOWN)
        assertEquals("Bretonnridge Brawlers", team.name)
    }
}
