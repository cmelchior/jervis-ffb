package com.jervisffb.ui.menu.components.setup

import cafe.adriel.voyager.core.model.ScreenModel
import com.jervisffb.engine.model.BallType
import com.jervisffb.engine.model.PitchType
import com.jervisffb.engine.model.StadiumType
import com.jervisffb.engine.rules.bb2020.tables.SpringWeatherTable
import com.jervisffb.engine.rules.bb2020.tables.StandardKickOffEventTable
import com.jervisffb.engine.rules.bb2020.tables.StandardWeatherTable
import com.jervisffb.engine.rules.bb2020.tables.SummerWeatherTable
import com.jervisffb.engine.rules.bb2020.tables.WinterWeatherTable
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.p2p.host.KickOffTableEntry
import com.jervisffb.ui.menu.p2p.host.NoStadium
import com.jervisffb.ui.menu.p2p.host.NoUnusualBall
import com.jervisffb.ui.menu.p2p.host.PitchEntry
import com.jervisffb.ui.menu.p2p.host.RollForStadiumUsed
import com.jervisffb.ui.menu.p2p.host.RollOnUnusualBallTable
import com.jervisffb.ui.menu.p2p.host.SpecificStadium
import com.jervisffb.ui.menu.p2p.host.SpecificUnusualBall
import com.jervisffb.ui.menu.p2p.host.StadiumEntry
import com.jervisffb.ui.menu.p2p.host.UnusualBallEntry
import com.jervisffb.ui.menu.p2p.host.WeatherTableEntry
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * View controller for the game setup component. This component is responsible for all the UI control needed
 * to configure the rules of a game.
 */
class SetupGameComponentModel(private val menuViewModel: MenuViewModel) : ScreenModel {

    val isSetupValid = MutableStateFlow<Boolean>(true)

    val selectedWeatherTable = MutableStateFlow<WeatherTableEntry?>(null)
    val selectedKickOffTable = MutableStateFlow<KickOffTableEntry?>(null)
    val selectedUnusualBall = MutableStateFlow<UnusualBallEntry?>(null)
    val selectedPitch = MutableStateFlow<PitchEntry?>(null)

    val weatherTables = listOf(
        "Rulebook" to listOf(
            WeatherTableEntry("Standard", StandardWeatherTable, true),
        ),
        "Death Zone" to listOf(
            WeatherTableEntry("Spring", SpringWeatherTable, false),
            WeatherTableEntry("Summer", SummerWeatherTable, false),
            WeatherTableEntry("Autumn", SummerWeatherTable, false),
            WeatherTableEntry("Winter", WinterWeatherTable, false),
            WeatherTableEntry("Subterranean", StandardWeatherTable, false),
            WeatherTableEntry("Primordial", StandardWeatherTable, false),
            WeatherTableEntry("Graveyard", StandardWeatherTable, false),
            WeatherTableEntry("Desolate Wasteland", StandardWeatherTable, false),
            WeatherTableEntry("Mountainous", StandardWeatherTable, false),
            WeatherTableEntry("Coastal", StandardWeatherTable, false),
            WeatherTableEntry("Desert", StandardWeatherTable, false),
        )
    )

    val kickOffTables = listOf(
        "Rulebook" to listOf(
            KickOffTableEntry("Standard", StandardKickOffEventTable, true),
        ),
        "Spike Magazine 15 (Amazons)" to listOf(
            KickOffTableEntry("Temple-City", StandardKickOffEventTable, false),
        )
    )

    val unusualBallList = listOf(
        "Rulebook" to listOf(
            UnusualBallEntry("Normal Ball", NoUnusualBall, true)
        ),
        "Death Zone" to listOf(
            UnusualBallEntry("Roll On Unusual Balls Table", RollOnUnusualBallTable, false),
            UnusualBallEntry("Explodin'", SpecificUnusualBall(BallType.EXPLODIN), false),
            UnusualBallEntry("Deamonic", SpecificUnusualBall(BallType.DEAMONIC), false),
            UnusualBallEntry("Stacked Lunch", SpecificUnusualBall(BallType.STACKED_LUNCH), false),
            UnusualBallEntry("Draconic", SpecificUnusualBall(BallType.DRACONIC), false),
            UnusualBallEntry("Spiteful Sprite", SpecificUnusualBall(BallType.SPITEFUL_SPRITE), false),
            UnusualBallEntry("Master-hewn", SpecificUnusualBall(BallType.MASTER_HEWN), false),
            UnusualBallEntry("Extra Spiky", SpecificUnusualBall(BallType.EXTRA_SPIKY), false),
            UnusualBallEntry("Greedy Nurgling", SpecificUnusualBall(BallType.GREEDY_NURGLING), false),
            UnusualBallEntry("Dark Majesty", SpecificUnusualBall(BallType.DARK_MAJESTY), false),
            UnusualBallEntry("Shady Special", SpecificUnusualBall(BallType.SHADY_SPECIAL), false),
            UnusualBallEntry("Soulstone", SpecificUnusualBall(BallType.SOULSTONE), false),
            UnusualBallEntry("Frozen", SpecificUnusualBall(BallType.FROZEN_BALL), false),
            UnusualBallEntry("Sacred Egg", SpecificUnusualBall(BallType.SACRED_EGG), false),
            UnusualBallEntry("Snotling Ball-suite", SpecificUnusualBall(BallType.SNOTLING_BALL_SUIT), false),
            UnusualBallEntry("Limpin' Squig", SpecificUnusualBall(BallType.LIMPIN_SQUIG), false),
            UnusualBallEntry("Warpstone Brazier", SpecificUnusualBall(BallType.WARPSTONE_BRAZIER), false),
        ),
        "Spike Magazine 14 (Norse)" to listOf(
            UnusualBallEntry("Hammer of Legend", SpecificUnusualBall(BallType.HAMMER_OF_LEGEND), false),
            UnusualBallEntry("The Runestone", SpecificUnusualBall(BallType.THE_RUNESTONE), false),
        ),
        "Spike Magazine 15 (Amazons)" to listOf(
            UnusualBallEntry("Crystal Skull", SpecificUnusualBall(BallType.CRYSTAL_SKULL), false),
            UnusualBallEntry("Snake-swallowed", SpecificUnusualBall(BallType.SNAKE_SWALLOWED), false),
        ),
    )

    val pitches = listOf(
        "Rulebook" to listOf(
            PitchEntry("Standard", PitchType.STANDARD, true),
        ),
        "Spike Magazine 14 (Norse)" to listOf(
            PitchEntry("Frozen Lake", PitchType.FROZEN_LAKE, false),
        ),
        "Spike Magazine 15 (Amazons)" to listOf(
            PitchEntry("Overgrown Jungle", PitchType.OVERGROWN_JUNGLE, false),
        )
    )

    val stadia = listOf(
        "Death Zone" to listOf(
            StadiumEntry("Disabled", NoStadium, true),
            StadiumEntry("Enabled", RollForStadiumUsed, false),
        ),
        "Unusual Playing Surface" to listOf(
            StadiumEntry("Ankle-Deep Water", SpecificStadium(StadiumType.ANKLE_DEEP_WATER), false),
            StadiumEntry("Sloping Pitch", SpecificStadium(StadiumType.SLOPING_PITCH), false),
            StadiumEntry("Ice", SpecificStadium(StadiumType.ICE), false),
            StadiumEntry("Astrogranite", SpecificStadium(StadiumType.ASTROGRANITE), false),
            StadiumEntry("Uneven Footing", SpecificStadium(StadiumType.UNEVEN_FOOTING), false),
            StadiumEntry("Solid Stone", SpecificStadium(StadiumType.SOLID_STONE), false),
        ),
    )

    private fun checkValidSetup() {
        var isValid = true
        isSetupValid.value = isValid
    }

    fun setWeatherTable(entry: WeatherTableEntry) {
        selectedWeatherTable.value = entry
        checkValidSetup()
    }

    fun setKickOffTable(entry: KickOffTableEntry) {
        selectedKickOffTable.value = entry
        checkValidSetup()
    }

    fun setUnusualBall(entry: UnusualBallEntry) {
        selectedUnusualBall.value = entry
        checkValidSetup()
    }

    fun setPitch(entry: PitchEntry) {
        selectedPitch.value = entry
        checkValidSetup()
    }
}
