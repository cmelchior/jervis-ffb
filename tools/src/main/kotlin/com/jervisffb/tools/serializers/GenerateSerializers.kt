package com.jervisffb.tools.serializers

import io.github.classgraph.ClassGraph
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.reflect.KClass

@Suppress("KotlinPrintToLogpoint")
fun main(args: Array<String>) {
    val output = Path.of(args.firstOrNull() ?: "tools/build/generated/serializers").createDirectories()
    val files = listOf(
        "coreSerializers.kt" to source("core", "com.jervisffb.engine.serialization", "coreSerializerModule"),
        "commonSerializers.kt" to source("common", "com.jervisffb.engine.common.serialization", "commonSerializerModule"),
        "bb2020Serializers.kt" to source("bb2020", "com.jervisffb.engine.bb2020.serialization", "bb2020SerializerModule"),
        "bb2025Serializers.kt" to source("bb2025", "com.jervisffb.engine.bb2025.serialization", "bb2025SerializerModule"),
    )
    val report = files.joinToString("\n\n") { (name, text) -> "===== $name =====\n$text" }
    files.forEach { (name, text) -> output.resolve(name).writeText(text) }
    output.resolve("serializers.txt").writeText(report)
    println(report)
    println("\nWrote ${files.size} Kotlin sources and serializers.txt to $output")
}

private data class Node(
    val type: KClass<*>,
    val leaves: List<KClass<*>>,
    val children: List<Node>,
)

private val roots = listOf(
    // Unclear what th best strategy is for these
    // com.jervisffb.engine.fsm.Procedure::class,
    com.jervisffb.engine.actions.GameAction::class,
    com.jervisffb.engine.actions.InducementSelection::class,
    com.jervisffb.engine.challenge.ChallengeScore::class,
    com.jervisffb.engine.common.AbstractRules::class,
    com.jervisffb.engine.model.PlayerState::class,
    com.jervisffb.engine.model.SkillValue::class,
    com.jervisffb.engine.model.inducements.BiasedReferee::class,
    com.jervisffb.engine.model.inducements.InducementEffect::class,
    com.jervisffb.engine.model.inducements.InfamousCoachingStaff::class,
    com.jervisffb.engine.model.inducements.Spell::class,
    com.jervisffb.engine.model.inducements.settings.Inducement::class,
    com.jervisffb.engine.model.inducements.settings.InducementGroup::class,
    com.jervisffb.engine.model.inducements.settings.SingleInducement::class,
    com.jervisffb.engine.model.inducements.settings.TeamPlayerInducement::class,
    com.jervisffb.engine.model.inducements.wizards.Wizard::class,
    com.jervisffb.engine.model.locations.Location::class,
    com.jervisffb.engine.model.modifiers.PlayerStatusEffect::class,
    com.jervisffb.engine.rules.Rules::class,
    com.jervisffb.engine.rules.builder.BallSelectorRule::class,
    com.jervisffb.engine.rules.builder.StadiumRule::class,
    com.jervisffb.engine.rules.common.actions.ActionType::class,
    com.jervisffb.engine.rules.common.actions.TeamActions::class,
    com.jervisffb.engine.rules.common.pathfinder.PathFinder::class,
    com.jervisffb.engine.rules.common.procedures.DieRoll::class,
    com.jervisffb.engine.rules.common.roster.Position::class,
    com.jervisffb.engine.rules.common.roster.SpecialRules::class,
    com.jervisffb.engine.rules.common.skills.SkillSettings::class,
    com.jervisffb.engine.rules.common.tables.ArgueTheCallTable::class,
    com.jervisffb.engine.rules.common.tables.CasualtyTable::class,
    com.jervisffb.engine.rules.common.tables.InjuryTable::class,
    com.jervisffb.engine.rules.common.tables.KickOffTable::class,
    com.jervisffb.engine.rules.common.tables.LastingInjuryTable::class,
    com.jervisffb.engine.rules.common.tables.PrayersToNuffleTable::class,
    com.jervisffb.engine.rules.common.tables.RangeRuler::class,
    com.jervisffb.engine.rules.common.tables.WeatherTable::class,
    com.jervisffb.engine.serialization.SerializedPlayer::class,
    com.jervisffb.engine.serialization.SerializedTeam::class,
    com.jervisffb.engine.sprites.SpriteSource::class,
    com.jervisffb.engine.statistics.probability.scorer.ProbabilityScoreResult::class,
)

private val ignored = setOf(
    com.jervisffb.engine.actions.CalculatedAction::class,
    com.jervisffb.engine.model.PitchSquare::class,
    com.jervisffb.engine.rules.AbstractDummyRules::class,
    com.jervisffb.engine.rules.common.procedures.DummyProcedure::class,
    com.jervisffb.engine.rules.common.procedures.ErrorProcedure::class,
)

private val nonSerializableAbstractRules = setOf(
    "com.jervisffb.engine.bb2020.BB2020Rules",
    "com.jervisffb.engine.bb2025.BB2025Rules",
)

private fun customRulesSerializerFor(type: KClass<*>): String? = when (type.qualifiedName) {
    "com.jervisffb.engine.bb2020.StandardBB2020Rules" -> "com.jervisffb.engine.bb2020.StandardBB2020RulesSerializer"
    "com.jervisffb.engine.bb2020.FumbblBB2020Rules" -> "com.jervisffb.engine.bb2020.FumbblBB2020RulesSerializer"
    "com.jervisffb.engine.bb2020.BB72020Rules" -> "com.jervisffb.engine.bb2020.BB72020RulesSerializer"
    "com.jervisffb.engine.bb2025.StandardBB2025Rules" -> "com.jervisffb.engine.bb2025.StandardBB2025RulesSerializer"
    else -> null
}

private fun discover(): List<Node> = ClassGraph()
    .enableAllInfo()
    .acceptPackages("com.jervisffb.engine")
    .scan()
    .use { graph ->
        fun visit(type: KClass<*>): Node {
            val classes = if (type.java.isInterface) {
                graph.getClassesImplementing(type.java)
            } else {
                graph.getSubclasses(type.java)
            }.map { Class.forName(it.name).kotlin }.filterNot(ignored::contains)
            val (open, leaves) = classes.partition {
                it.java.isInterface || it.isAbstract || it.isSealed
            }
            return Node(type, leaves, open.map(::visit))
        }
        roots.map(::visit)
    }

private fun String.containsAny(matches: List<String>): Boolean {
    return matches.any { this.contains(it) }
}

private fun bucket(type: KClass<*>): String = type.qualifiedName.orEmpty().let { name ->
    val lowerName = name.lowercase()
    when {
        ".bb2020." in lowerName -> "bb2020"
        ".bb2025." in lowerName -> "bb2025"
        lowerName.endsWith(".procedures.actions.punt.puntaction") -> "bb2025"
        ".engine.common." in lowerName -> "common"
        else -> "core"
    }
}

private fun Node.hasContent(bucket: String): Boolean =
    leaves.any { bucket(it) == bucket } || children.any { it.hasContent(bucket) }

private fun Node.emit(builder: StringBuilder, bucket: String, indent: Int) {
    if (!hasContent(bucket)) return
    val current = " ".repeat(indent * 4)
    val child = " ".repeat((indent + 1) * 4)
    builder.appendLine("${current}polymorphic(${type.qualifiedName}::class) {")
    leaves.filter { bucket(it) == bucket }.forEach {
        val serializer = customRulesSerializerFor(it)
        if (serializer == null) {
            builder.appendLine("${child}subclass(${it.qualifiedName}::class)")
        } else {
            builder.appendLine("${child}subclass(${it.qualifiedName}::class, $serializer)")
        }
    }
    children.filterNot { it.type.qualifiedName in nonSerializableAbstractRules }
        .forEach { it.emit(builder, bucket, indent + 1) }
    builder.appendLine("$current}")
}

private fun source(bucket: String, packageName: String, valueName: String): String = buildString {
    appendLine("package $packageName")
    appendLine()
    appendLine("import kotlinx.serialization.modules.SerializersModule")
    appendLine("import kotlinx.serialization.modules.polymorphic")
    appendLine("import kotlinx.serialization.modules.subclass")
    appendLine()
    appendLine("/** Generated by :tools:generateSerializers. DO NOT MODIFY! */")
    appendLine("val $valueName = SerializersModule {")
    discover().forEach { it.emit(this, bucket, 1) }
    appendLine("}")
}
