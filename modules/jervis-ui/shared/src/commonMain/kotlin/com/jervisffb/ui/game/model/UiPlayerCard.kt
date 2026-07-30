package com.jervisffb.ui.game.model

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.isOnHomeTeam
import com.jervisffb.engine.rules.common.skills.IntAdjustmentSkillFactory
import com.jervisffb.engine.rules.common.skills.IntTargetSkillFactory
import com.jervisffb.engine.rules.common.skills.KeywordSkillFactory
import com.jervisffb.engine.rules.common.skills.NoValueSkillFactory
import com.jervisffb.engine.rules.common.skills.Skill
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.rules.common.skills.SkillFactory
import com.jervisffb.engine.rules.common.skills.SkillSettings
import com.jervisffb.ui.game.view.JervisTheme

data class UiSkillData(
    val skillSettings: SkillSettings,
    val name: String,
    var existingSkill: Skill<*>?,
    val factory: SkillFactory<*>,
    var isEnabled: Boolean,
    val options: UiPlayerCard.UiSkillOptions?,
    val isFactoryButton: Boolean
)

data class UiKeywordData(
    val name: String,
    val keyword: PlayerKeyword,
    var isEnabled: Boolean
)

class UiPlayerCard(
    val player: ModelRef<Player>
) {
    val model: Player
        get() = player.model

    val color = when (player.model.isOnHomeTeam()) {
        true -> JervisTheme.homeTeamColor
        false -> JervisTheme.awayTeamColor
    }

    sealed interface UiSkillOptions {
        val options: List<Comparable<*>>
    }
    data class IntTargetOptions(override val options: List<Int>): UiSkillOptions
    data class IntAdjustmentOptions(override val options: List<Int>): UiSkillOptions
    data class KeywordOptions(override val options: List<PlayerKeyword>): UiSkillOptions

    private fun getSkills(skillSettings: SkillSettings, category: SkillCategory): List<UiSkillData> {
        return skillSettings.getAvailableSkills(category)
            .sortedBy { it.name }
            .flatMap { factory ->
                val rosterSkill = player.model.positionSkills.filter { it.type == factory.type }
                val extraSkill = player.model.extraSkills.filter { it.type == factory.type }
                val options = when (factory) {
                    is IntAdjustmentSkillFactory -> IntAdjustmentOptions((1..6).toList())
                    is IntTargetSkillFactory -> IntTargetOptions((1..6).toList())
                    is KeywordSkillFactory -> KeywordOptions(PlayerKeyword.entries.toList())
                    is NoValueSkillFactory -> null
                }
                val existingSkills = (rosterSkill + extraSkill)

                val skillButtons = mutableListOf<UiSkillData>()
                if (factory is KeywordSkillFactory) {
                    skillButtons.addAll(existingSkills.map { existingSkill ->
                        UiSkillData(
                            skillSettings = skillSettings,
                            name = existingSkill.name,
                            existingSkill = existingSkill,
                            factory = factory,
                            options = options,
                            isEnabled = true,
                            isFactoryButton = false,
                        )
                    })
                    // Factory button (for creating variants)
                    skillButtons.add(UiSkillData(
                        skillSettings = skillSettings,
                        name = factory.name,
                        existingSkill = null,
                        factory = factory,
                        options = options,
                        isEnabled = false,
                        isFactoryButton = true,
                    ))
                } else {
                    val currentSkill = existingSkills.firstOrNull()
                    skillButtons.add(UiSkillData(
                        skillSettings = skillSettings,
                        name = currentSkill?.name ?: factory.name,
                        existingSkill = currentSkill,
                        factory = factory,
                        options = options,
                        isEnabled = (currentSkill != null),
                        isFactoryButton = false,
                    ))
                }
                skillButtons
            }
    }

    fun getSkillSections(): List<Pair<String, List<UiSkillData>>> {
        // UI Order
        val categoryList = listOf(
            SkillCategory.AGILITY,
            SkillCategory.DEVIOUS,
            SkillCategory.GENERAL,
            SkillCategory.MUTATIONS,
            SkillCategory.PASSING,
            SkillCategory.STRENGTH,
            SkillCategory.TRAITS,
            SkillCategory.SPECIAL_RULES,
        )
        // Skills that are unavailble according to the rules, but we still want to have the option of selecting them
        val unavailableSkills = categoryList.minus((player.model.position.primary + player.model.position.secondary).toSet())
        val skillSettings = player.model.team.game.rules.skillSettings
        val skillList = buildList {
            val primarySkills = player.model.position.primary.map { skillCategory ->
                val description = skillCategory.description + " (Primary)"
                val skills = getSkills(skillSettings, skillCategory)
                description to skills
            }
            addAll(primarySkills)

            val secondarySkills = player.model.position.secondary.map { skillCategory ->
                val description = skillCategory.description + " (Secondary)"
                val skills = getSkills(skillSettings, skillCategory)
                description to skills
            }
            addAll(secondarySkills)

            val unavailableSkills = unavailableSkills.map { skillCategory ->
                val description = skillCategory.description
                val skills = getSkills(skillSettings, skillCategory)
                description to skills
            }
            addAll(unavailableSkills)
        }
        return skillList
    }

    fun getKeywords(): List<UiKeywordData> {
        return PlayerKeyword.entries.map {
            UiKeywordData(
                name = it.description,
                keyword = it,
                isEnabled = player.model.keywords.contains(it)
            )
        }
    }
}
