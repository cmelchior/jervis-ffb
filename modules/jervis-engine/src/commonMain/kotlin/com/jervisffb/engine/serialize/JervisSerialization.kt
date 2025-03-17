package com.jervisffb.engine.serialize

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CoinSideSelected
import com.jervisffb.engine.actions.CoinTossResult
import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.D12Result
import com.jervisffb.engine.actions.D16Result
import com.jervisffb.engine.actions.D20Result
import com.jervisffb.engine.actions.D2Result
import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D4Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.DicePoolResultsSelected
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.DogoutSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerDeselected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.RandomPlayersSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.Revert
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Field
import com.jervisffb.engine.model.FieldSquare
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.inducements.BiasedReferee
import com.jervisffb.engine.model.inducements.Spell
import com.jervisffb.engine.model.inducements.StandardBiasedReferee
import com.jervisffb.engine.model.inducements.wizards.Fireball
import com.jervisffb.engine.model.inducements.wizards.HirelingSportsWizard
import com.jervisffb.engine.model.inducements.wizards.Wizard
import com.jervisffb.engine.model.inducements.wizards.Zap
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.locations.FieldCoordinateImpl
import com.jervisffb.engine.model.locations.GiantLocation
import com.jervisffb.engine.model.locations.Location
import com.jervisffb.engine.rules.BB2020TeamActions
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.engine.rules.TeamActions
import com.jervisffb.engine.rules.bb2020.procedures.actions.blitz.BlitzAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BlockAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.foul.FoulAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.MoveAction
import com.jervisffb.engine.rules.bb2020.roster.BB2020Position
import com.jervisffb.engine.rules.bb2020.roster.BB2020Roster
import com.jervisffb.engine.rules.bb2020.skills.AnimalSavagery
import com.jervisffb.engine.rules.bb2020.skills.Block
import com.jervisffb.engine.rules.bb2020.skills.BloodLust
import com.jervisffb.engine.rules.bb2020.skills.BoneHead
import com.jervisffb.engine.rules.bb2020.skills.BreakTackle
import com.jervisffb.engine.rules.bb2020.skills.CatchSkill
import com.jervisffb.engine.rules.bb2020.skills.DivingTackle
import com.jervisffb.engine.rules.bb2020.skills.Dodge
import com.jervisffb.engine.rules.bb2020.skills.Frenzy
import com.jervisffb.engine.rules.bb2020.skills.Horns
import com.jervisffb.engine.rules.bb2020.skills.Leap
import com.jervisffb.engine.rules.bb2020.skills.Loner
import com.jervisffb.engine.rules.bb2020.skills.MightyBlow
import com.jervisffb.engine.rules.bb2020.skills.MultipleBlock
import com.jervisffb.engine.rules.bb2020.skills.Pass
import com.jervisffb.engine.rules.bb2020.skills.PrehensileTail
import com.jervisffb.engine.rules.bb2020.skills.Pro
import com.jervisffb.engine.rules.bb2020.skills.ProjectileVomit
import com.jervisffb.engine.rules.bb2020.skills.ReallyStupid
import com.jervisffb.engine.rules.bb2020.skills.Regeneration
import com.jervisffb.engine.rules.bb2020.skills.SideStep
import com.jervisffb.engine.rules.bb2020.skills.Skill
import com.jervisffb.engine.rules.bb2020.skills.SkillFactory
import com.jervisffb.engine.rules.bb2020.skills.Sprint
import com.jervisffb.engine.rules.bb2020.skills.Stab
import com.jervisffb.engine.rules.bb2020.skills.Stunty
import com.jervisffb.engine.rules.bb2020.skills.SureFeet
import com.jervisffb.engine.rules.bb2020.skills.SureHands
import com.jervisffb.engine.rules.bb2020.skills.Tackle
import com.jervisffb.engine.rules.bb2020.skills.ThickSkull
import com.jervisffb.engine.rules.bb2020.skills.Timmmber
import com.jervisffb.engine.rules.bb2020.skills.Titchy
import com.jervisffb.engine.rules.bb2020.skills.TwoHeads
import com.jervisffb.engine.rules.bb2020.skills.UnchannelledFury
import com.jervisffb.engine.rules.bb2020.skills.Wrestle
import com.jervisffb.engine.rules.bb2020.specialrules.SneakiestOfTheLot
import com.jervisffb.engine.rules.common.pathfinder.BB2020PathFinder
import com.jervisffb.engine.rules.common.pathfinder.PathFinder
import com.jervisffb.engine.rules.common.roster.Position
import com.jervisffb.engine.rules.common.roster.Roster
import com.jervisffb.utils.platformFileSystem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import okio.Path
import okio.buffer
import okio.use

data class GameFileData(
    val homeTeam: Team,
    val awayTeam: Team,
    val game: GameEngineController,
    val actions: List<GameAction>,
)

/**
 * Class encapsulating the the logic for serializing and deserializing a Jervis game file.
 *
 * TODO Pretty annoying to keep this up to date. Figure out if there is a way to automate this.
 *  Perhaps a Gradle task that autogenerate it?
 */
object JervisSerialization {
    val jervisEngineModule =
        SerializersModule {
            polymorphic(Skill::class) {
                // Skills
                subclass(AnimalSavagery::class)
                subclass(Block::class)
                subclass(BloodLust::class)
                subclass(BoneHead::class)
                subclass(BreakTackle::class)
                subclass(CatchSkill::class)
                subclass(DivingTackle::class)
                subclass(Dodge::class)
                subclass(Frenzy::class)
                subclass(Horns::class)
                subclass(Leap::class)
                subclass(Loner::class)
                subclass(MightyBlow::class)
                subclass(MultipleBlock::class)
                subclass(Pass::class)
                subclass(PrehensileTail::class)
                subclass(Pro::class)
                subclass(ProjectileVomit::class)
                subclass(ReallyStupid::class)
                subclass(Regeneration::class)
                subclass(SideStep::class)
                subclass(Sprint::class)
                subclass(Stab::class)
                subclass(Stunty::class)
                subclass(SureFeet::class)
                subclass(SureHands::class)
                subclass(Tackle::class)
                subclass(ThickSkull::class)
                subclass(Timmmber::class)
                subclass(Titchy::class)
                subclass(TwoHeads::class)
                subclass(Titchy::class)
                subclass(UnchannelledFury::class)
                subclass(Wrestle::class)

                //Special Rules
                subclass(SneakiestOfTheLot::class)
            }
            polymorphic(SkillFactory::class) {
                // Player Skills
                subclass(AnimalSavagery.Factory::class)
                subclass(Block.Factory::class)
                subclass(BloodLust.Factory::class)
                subclass(BoneHead.Factory::class)
                subclass(BreakTackle.Factory::class)
                subclass(CatchSkill.Factory::class)
                subclass(DivingTackle.Factory::class)
                subclass(Dodge.Factory::class)
                subclass(Frenzy.Factory::class)
                subclass(Horns.Factory::class)
                subclass(Leap.Factory::class)
                subclass(Loner.Factory::class)
                subclass(MightyBlow.Factory::class)
                subclass(MultipleBlock.Factory::class)
                subclass(Pass.Factory::class)
                subclass(PrehensileTail.Factory::class)
                subclass(Pro.Factory::class)
                subclass(ProjectileVomit.Factory::class)
                subclass(ReallyStupid.Factory::class)
                subclass(Regeneration.Factory::class)
                subclass(SideStep.Factory::class)
                subclass(Sprint.Factory::class)
                subclass(Stab.Factory::class)
                subclass(Stunty.Factory::class)
                subclass(SureFeet.Factory::class)
                subclass(SureHands.Factory::class)
                subclass(Tackle.Factory::class)
                subclass(ThickSkull.Factory::class)
                subclass(Timmmber.Factory::class)
                subclass(Titchy.Factory::class)
                subclass(TwoHeads.Factory::class)
                subclass(Titchy.Factory::class)
                subclass(UnchannelledFury.Factory::class)
                subclass(Wrestle.Factory::class)

                // Special Rules
                subclass(SneakiestOfTheLot.Factory::class)
            }
            polymorphic(Procedure::class) {
                subclass(MoveAction::class)
                subclass(BlitzAction::class)
                subclass(BlockAction::class)
                subclass(FoulAction::class)
            }
            polymorphic(Rules::class) {
                subclass(StandardBB2020Rules::class)
            }
            polymorphic(Roster::class) {
                subclass(BB2020Roster::class)
            }
            polymorphic(Position::class) {
                subclass(BB2020Position::class)
            }
            polymorphic(Location::class) {
                subclass(DogOut::class)
                subclass(GiantLocation::class)
                subclass(FieldSquare::class)
                subclass(FieldCoordinateImpl::class)
            }
            polymorphic(FieldCoordinate::class) {
                subclass(FieldCoordinateImpl::class)
            }
            polymorphic(GameAction::class) {
                subclass(BlockTypeSelected::class)
                subclass(Cancel::class)
                subclass(CoinSideSelected::class)
                subclass(CoinTossResult::class)
                subclass(CompositeGameAction::class)
                subclass(Confirm::class)
                subclass(Continue::class)
                subclass(D12Result::class)
                subclass(D16Result::class)
                subclass(D20Result::class)
                subclass(D2Result::class)
                subclass(D3Result::class)
                subclass(D4Result::class)
                subclass(D6Result::class)
                subclass(D8Result::class)
                subclass(DBlockResult::class)
                subclass(DicePoolResultsSelected::class)
                subclass(DiceRollResults::class)
                subclass(DirectionSelected::class)
                subclass(DogoutSelected::class)
                subclass(EndAction::class)
                subclass(EndSetup::class)
                subclass(EndTurn::class)
                subclass(FieldSquareSelected::class)
                subclass(MoveTypeSelected::class)
                subclass(NoRerollSelected::class)
                subclass(PlayerActionSelected::class)
                subclass(PlayerDeselected::class)
                subclass(PlayerSelected::class)
                subclass(RandomPlayersSelected::class)
                subclass(RerollOptionSelected::class)
                subclass(Revert::class)
                subclass(Undo::class)
            }
            polymorphic(Wizard::class) {
                subclass(HirelingSportsWizard::class)
            }
            polymorphic(Spell::class) {
                subclass(Fireball::class)
                subclass(Zap::class)
            }
            polymorphic(BiasedReferee::class) {
                subclass(StandardBiasedReferee::class)
            }
            polymorphic(TeamActions::class) {
                subclass(BB2020TeamActions::class)
            }
            polymorphic(PathFinder::class) {
                subclass(BB2020PathFinder::class)
            }
        }

    private val jsonFormat =
        Json {
            useArrayPolymorphism = true
            serializersModule = jervisEngineModule
            prettyPrint = true
        }

    fun createTeamSnapshot(team: Team): JsonElement {
        return jsonFormat.encodeToJsonElement(team)
    }

    fun saveToFile(
        controller: GameEngineController,
        file: Path,
    ) {
        val fileData =
            JervisGameFile(
                JervisMetaData(FILE_FORMAT_VERSION),
                JervisConfiguration(controller.rules),
                JervisGameData(controller.initialHomeTeamState!!, controller.initialAwayTeamState!!, controller.history.flatMap { it.steps.map { it.action }}),
            )
        val fileContent = jsonFormat.encodeToString(fileData)
        platformFileSystem.sink(file).use { fileSink ->
            fileSink.buffer().use {
                it.writeUtf8(fileContent)
            }
        }
    }

    /**
     * Load a Jervis Game File and prepare the game state from it.
     */
    fun loadFromFile(file: Path): Result<GameFileData> {
        try {
            val fileContent =
                platformFileSystem.source(file).use { fileSource ->
                    fileSource.buffer().readUtf8()
                }
            val fileData = jsonFormat.decodeFromString<JervisGameFile>(fileContent)
            val rules = fileData.configuration.rules
            val homeTeam = jsonFormat.decodeFromJsonElement<Team>(fileData.game.homeTeam)
            homeTeam.noToPlayer.values.forEach { it.team = homeTeam }
            homeTeam.notifyDogoutChange()
            val awayTeam = jsonFormat.decodeFromJsonElement<Team>(fileData.game.awayTeam)
            awayTeam.noToPlayer.values.forEach { it.team = awayTeam }
            awayTeam.notifyDogoutChange()
            val state = Game(rules, homeTeam, awayTeam, Field.createForRuleset(rules))
            val controller = GameEngineController(state)
            val gameData = GameFileData(homeTeam, awayTeam, controller, fileData.game.actions)
            return Result.success(gameData)
        } catch (ex: Exception) {
            return Result.failure(ex)
        }
    }

    /**
     * Make sure that all [Team] and [Game] references are set after deserializing a Team.
     * Hopefully, this can be removed eventually, but it requires changes to the deserializer.
     */
    fun fixStateRefs(state: Game): Game {
        state.homeTeam.forEach { it.team = state.homeTeam }
        state.awayTeam.forEach { it.team = state.awayTeam }
        state.homeTeam.teamIsHomeTeam = true
        state.homeTeam.teamIsAwayTeam = false
        state.homeTeam.setGameReference(state)
        state.awayTeam.teamIsHomeTeam = false
        state.awayTeam.teamIsAwayTeam = true
        state.awayTeam.setGameReference(state)
        state.homeTeam.notifyDogoutChange()
        state.awayTeam.notifyDogoutChange()
        return state
    }

    fun fixTeamRefs(team: Team): Team {
        team.forEach { it.team = team }
        team.notifyDogoutChange()
        return team
    }

    // Remap object references like to Player in GameActions so they all point to the same instance
    private fun remapActionRefs(
        actions: List<GameAction>,
        state: Game,
    ): List<GameAction> {
        return actions.map { action ->
            when (action) {
//                is PlayerSelected -> {
//                    val isHomeTeam = state.homeTeam.firstOrNull { it.id == action.player.id } != null
//                    val playerNo = action.player.number
//                    val team = if (isHomeTeam) state.homeTeam else state.awayTeam
//                    val player = team[playerNo]!!
//                    PlayerSelected(player)
//                }
                else -> action
            }
        }
    }
}
