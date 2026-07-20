package tourplay.rest

import com.jervisffb.engine.bb2020.FumbblBB2020Rules
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
}
