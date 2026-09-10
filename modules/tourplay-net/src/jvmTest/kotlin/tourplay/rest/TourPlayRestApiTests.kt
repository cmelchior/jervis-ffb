package tourplay.rest

import com.jervisffb.engine.bb2020.FumbblBB2020Rules
import com.jervisffb.engine.bb2025.BB7Rules2025
import com.jervisffb.engine.bb2025.StandardBB2025Rules
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.serialization.SerializedTeam
import com.jervisffb.engine.sprites.SpriteLocation
import com.jervisffb.tourplay.TourPlayApi
import com.jervisffb.tourplay.TourPlayIconMapping
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TourPlayRestApiTests {

    private lateinit var api: TourPlayApi

    @BeforeTest
    fun setUp() {
        api = TourPlayApi(iconMapping)
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

    // Test for https://github.com/cmelchior/jervis-ffb/issues/62
    @Test
    fun teamLoaderUsesFumbblIcons() = runBlocking {
        // Old World Alliance and Chaos Chosen between them cover roster-qualified entries,
        // bare entries and positions renamed between BB2020 and BB2025.
        listOf(
            214739L to "Old World Alliance",
            176917L to "Chaos Chosen"
        ).forEach { (id, roster) ->
            val rules = StandardBB2025Rules()
            val file = api.loadRoster(id, rules)
            val team = SerializedTeam.deserialize(rules, file.getOrThrow().team, Coach.UNKNOWN)
            assertEquals(roster, team.roster.name)
            team.roster.positions.forEach { position ->
                assertEquals(SpriteLocation.FUMBBL_INI, position.icon?.type, "No icon for ${position.title}")
                assertEquals(SpriteLocation.FUMBBL_INI, position.portrait?.type, "No portrait for ${position.title}")
            }
            assertTrue(team.all { it.icon?.sprite?.type == SpriteLocation.FUMBBL_INI })
        }
    }

    // Test for https://github.com/cmelchior/jervis-ffb/issues/80 (BB7 Team)
    @Test
    fun load227396() = runBlocking {
        val rules = BB7Rules2025()
        val file = api.loadRoster(227396, rules)
        val team = SerializedTeam.deserialize(rules, file.getOrThrow().team, Coach.UNKNOWN)
        assertEquals("Bretonnridge Brawlers", team.name)
    }

    @Test
    fun load176917() = runBlocking {
        val rules = StandardBB2025Rules()
        val file = api.loadRoster(176917, rules)
        val team = SerializedTeam.deserialize(rules, file.getOrThrow().team, Coach.UNKNOWN)
        assertEquals("Khazra’s Den 26", team.name)
    }
}

private val iconMapping: TourPlayIconMapping by lazy {
    // The mapping files are Compose resources owned by `jervis-ui`, which this module does
    // not depend on, so the test reads them straight off disk instead.
    val resourceDir = File("../jervis-ui/shared/src/commonMain/composeResources/files")
    TourPlayIconMapping().apply {
        initialize(
            fumbblIcons = File(resourceDir, "fumbbl/icons.ini").readText(),
            fumbblExtraIcons = File(resourceDir, "fumbbl/icons-extra.ini").readText(),
            tourPlayIconSets = File(resourceDir, "tourplay/icons-iconsets.ini").readText(),
            tourPlayPortraits = File(resourceDir, "tourplay/icons-portraits.ini").readText(),
        )
    }
}
