package com.jervisffb.ui.game.dialogs

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import cafe.adriel.voyager.core.model.ScreenModel
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.actions.InducementSelection
import com.jervisffb.engine.actions.InducementsSelected
import com.jervisffb.engine.common.inducements.BiasedRefereeInducement
import com.jervisffb.engine.common.inducements.InducementSelectionCommon
import com.jervisffb.engine.common.inducements.InducementTypeCommon
import com.jervisffb.engine.common.inducements.InfamousCoachingStaffInducement
import com.jervisffb.engine.common.inducements.SimpleInducement
import com.jervisffb.engine.common.inducements.StandardMercenaryInducement
import com.jervisffb.engine.common.inducements.StarPlayerInducement
import com.jervisffb.engine.common.inducements.WizardInducement
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.WizardId
import com.jervisffb.engine.model.inducements.biasedreferee.BiasedRefereeType
import com.jervisffb.engine.model.inducements.infamouscoach.InfamousCoachingStaffType
import com.jervisffb.engine.model.inducements.settings.InducementGroup
import com.jervisffb.engine.model.inducements.settings.InducementType
import com.jervisffb.engine.model.inducements.settings.SingleInducement
import com.jervisffb.engine.rules.common.roster.Position
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.sprites.SpriteSource
import com.jervisffb.engine.utils.dedupSkillsByType
import kotlin.math.max
import kotlin.math.min

/**
 * This file contains classes and the view model needed to buy inducements.
 */

data class BuyInducementsDialog(
    val team: Team,
    val treasury: Int,
    val pettyCash: Int,
    val nextActionId: GameActionId,
    override var owner: Team? = null,
) : UserInputDialog

sealed interface CartKey {
    val type: InducementType

    data class Simple(override val type: InducementType) : CartKey
    data class Wizard(val id: WizardId) : CartKey {
        override val type: InducementType = InducementTypeCommon.WIZARD
    }
    data class BiasedReferee(val refereeType: BiasedRefereeType) : CartKey {
        override val type: InducementType = InducementTypeCommon.BIASED_REFEREE
    }
    data class InfamousCoach(val coachType: InfamousCoachingStaffType) : CartKey {
        override val type: InducementType = InducementTypeCommon.INFAMOUS_COACHING_STAFF
    }
    data class StarPlayer(val position: PositionId) : CartKey {
        override val type: InducementType = InducementTypeCommon.STAR_PLAYERS
    }
    // Mercenary entries are unique per addition — two mercenaries of the same position
    // with the same skill still count as two separate purchases. The id is a monotonic
    // counter allocated by the ViewModel.
    data class Mercenary(val id: Int) : CartKey {
        override val type: InducementType = InducementTypeCommon.STANDARD_MERCENARY_PLAYERS
    }
}

data class MercenaryCartEntry(
    val id: Int,
    val position: Position,
    val skill: SkillId?,
    val intrinsicSkill: SkillId?,
    val displayName: String,
    val price: Int,
) {
    // Skill lists where duplicates have been removed.
    val positionalSkillList: List<SkillId>
    val extraSkillList: List<SkillId>

    init {
        dedupSkillsByType(position.skills, listOfNotNull(intrinsicSkill, skill)).let {
            positionalSkillList = it.positionalSkills
            extraSkillList = it.extraSkills
        }
    }

}

/**
 * Pool of names used when creating mercenaries.
 */
val MERCENARY_NAMES: List<String> = listOf(
    "Grimtooth",
    "Slylock",
    "Nib the Twisted",
    "Vex Barkstone",
    "Vorn the Vile",
    "Grott Bonebreaker",
    "Splint the Cruel",
    "Krug the Hammer",
    "Skullface McGrim",
    "Uggh the Foul",
    "Zed Grimshaw",
    "Morg the Belligerent",
    "Fangor the Grim",
    "Ryke Blackblade",
    "Ashen One",
    "Corvin the Hollow",
    "Drex the Nameless",
    "Halix Coldheart",
    "Jarn the Wanderer",
    "Kessa Nightshade",
    "Lyr the Forsaken",
    "Mox the Silent",
    "Nyx Ravenmark",
    "Orn the Unbroken",
    "Pyre Ashfoot",
    "Quill the Shiv",
    "Ravik Duskmantle",
    "Sable the Sly",
    "Tarn Ironjaw",
    "Ulric the Blooded",
    "Varn the Battered",
    "Wex the Wretched",
    "Xarn Grimhelm",
    "Yorick the Bold",
    "Zorr the Reckless",
    "Cinder the Wraith",
    "Dregan the Scourge",
    "Ember Blackthorn",
    "Fenrir the Bold",
    "Gorric the Maimed",
)

// Class representing a single inducement row in one of the inducement groups.
data class GroupItemView(
    val key: CartKey,
    val name: String,
    val price: Int,
    val inducement: SingleInducement<*>,
    // Custom icon source for this specific inducement (star player portraits, etc.).
    val iconSource: SpriteSource?,
)

// Class representing inducements in the "Cart", i.e. when they are visible to
// the left of the "Buy" button at the bottom of the inducement selection dialog.
data class CartEntryView(
    val key: CartKey,
    val type: InducementType,
    val name: String,
    val tooltipName: String,
    val count: Int,
    val totalPrice: Int,
    val iconSource: SpriteSource?,
)

class BuyInducementsViewModel(
    val gameController: GameEngineController,
    private val dialog: BuyInducementsDialog
) : ScreenModel {

    private val team: Team = dialog.team
    private val settings = team.game.rules.inducements
    private val cart: SnapshotStateMap<CartKey, InducementSelection<*>> = mutableStateMapOf()
    private val activeDrawerGroupState = mutableStateOf<InducementType?>(null)
    val activeDrawerGroup: InducementType? by activeDrawerGroupState
    private var mercenaryIdCounter = 0
    private val mercenaryDisplayNames: SnapshotStateMap<Int, String> = mutableStateMapOf()
    private val mercenaryPositionsById: Map<PositionId, Position> = team.roster.positions.associateBy { it.id }

    val simpleInducements: List<SimpleInducement> =
        settings.values
            .filter { it.enabled }
            .filterIsInstance<SimpleInducement>()
            .filter { it.availableToTeam() }
            .sortedBy { it.name }

    val groupInducements: List<InducementGroup<*, *, *>> =
        settings.values
            .filter { it.enabled }
            .filterIsInstance<InducementGroup<*, *, *>>()
            .filter { group ->
                group.items.any { it.enabled && it.availableToTeam() }
            }

    val mercenaryInducement: StandardMercenaryInducement? =
        (settings[InducementTypeCommon.STANDARD_MERCENARY_PLAYERS] as? StandardMercenaryInducement)
            ?.takeIf { it.enabled && mercenaryPositionsById.isNotEmpty() }

    val mercenaryPositions: List<Position> = mercenaryPositionsById.values.sortedBy { it.title }

    fun primarySkillsFor(position: Position): List<Pair<SkillId, String>> {
        val skillSettings = team.game.rules.skillSettings
        return position.primary.flatMap { category ->
            skillSettings.getAvailableSkills(category)
                // Loner is intrinsic to every mercenary and cannot be re-picked.
                .filter { it.type != SkillType.LONER }
                .map { it.defaultSkillId to it.name }
        }
    }

    val totalPrice: Int by derivedStateOf {
        cart.values.sumOf { it.getPrice(team) }
    }

    // The cart entries should be in the same order as inducement lists are.
    val cartEntries: List<CartEntryView> by derivedStateOf {
        val out = mutableListOf<CartEntryView>()
        simpleInducements.forEach { inducement ->
            val key = CartKey.Simple(inducement.type)
            val selection = cart[key] ?: return@forEach
            out += CartEntryView(
                key = key,
                type = key.type,
                name = inducement.name,
                tooltipName = inducement.name,
                count = selection.count,
                totalPrice = selection.getPrice(team),
                iconSource = null,
            )
        }
        groupInducements.forEach { group ->
            group.items.forEach { item ->
                val key = cartKeyFor(item) ?: return@forEach
                val selection = cart[key] ?: return@forEach
                out += CartEntryView(
                    key = key,
                    type = key.type,
                    name = item.name,
                    tooltipName = item.name,
                    count = selection.count,
                    totalPrice = selection.getPrice(team),
                    iconSource = item.iconSource(),
                )
            }
        }
        mercenaries.forEach { merc ->
            val nameWithSkill = merc.skill?.let { "${merc.displayName} + ${it.toNiceString()}" } ?: merc.displayName
            val tooltip = merc.skill?.toNiceString()
                ?.let { "Mercenary ${merc.position.titleSingular} + $it" }
                ?: "Mercenary ${merc.position.titleSingular}"
            out += CartEntryView(
                key = CartKey.Mercenary(merc.id),
                type = InducementTypeCommon.STANDARD_MERCENARY_PLAYERS,
                name = nameWithSkill,
                tooltipName = tooltip,
                count = 1,
                totalPrice = merc.price,
                iconSource = merc.position.icon,
            )
        }
        out.asReversed()
    }

    val mercenaries: List<MercenaryCartEntry> by derivedStateOf {
        cart.entries.mapNotNull { (key, selection) ->
            if (key !is CartKey.Mercenary || selection !is InducementSelectionCommon.Mercenary) return@mapNotNull null
            val firstSkill = selection.extraSkills.firstOrNull()
            val displayName = mercenaryDisplayNames[key.id] ?: "Mercenary"
            MercenaryCartEntry(
                id = key.id,
                position = selection.position,
                skill = firstSkill,
                intrinsicSkill = SkillType.LONER.idTarget(4),
                displayName = displayName,
                price = selection.getPrice(team),
            )
        }.sortedBy { it.id }
    }

    val remainingBudget: Int by derivedStateOf {
        dialog.pettyCash + dialog.treasury - totalPrice
    }

    fun canAfford(inducement: SingleInducement<*>): Boolean {
        return inducement.getPrice(team) <= remainingBudget
    }

    val canBuy: Boolean by derivedStateOf {
        val budget = dialog.pettyCash + dialog.treasury
        val withinBudget = totalPrice <= budget
        val countByType = cart.entries.groupBy { it.key.type }
            .mapValues { entry -> entry.value.sumOf { it.value.count } }
        val withinLimits = countByType.all { (type, count) ->
            count <= (settings[type]?.max ?: Int.MAX_VALUE)
        }
        cart.isNotEmpty() && withinBudget && withinLimits
    }

    fun mercenaryPrice(position: Position, skill: SkillId?): Int {
        val settings = mercenaryInducement ?: return 0
        val skillPrice = if (skill != null) settings.skillCost else 0
        return position.cost + settings.extraCost + skillPrice
    }

    fun canAffordMercenary(position: Position, skill: SkillId?): Boolean {
        return mercenaryPrice(position, skill) <= remainingBudget
    }

    fun isMercenaryLimitReached(): Boolean {
        val settings = mercenaryInducement ?: return true
        return mercenaries.size >= settings.max
    }

    // A positional slot is only free if the roster limit ([Position.quantity])
    // has not been used up by non-injured team players plus mercenaries already
    // in the cart. Players who miss the game (injuries) don't take up a slot.
    fun isPositionLimitReached(position: Position): Boolean {
        val onTeam = team.count { it.position.id == position.id && !it.missNextGame }
        val inCart = mercenaries.count { it.position.id == position.id }
        return (onTeam + inCart) >= position.quantity
    }

    fun addMercenary(position: Position, skill: SkillId?, displayName: String) {
        val id = mercenaryIdCounter++
        mercenaryDisplayNames[id] = displayName
        cart[CartKey.Mercenary(id)] = InducementSelectionCommon.Mercenary(
            position = position,
            extraSkills = if (skill != null) listOf(skill) else emptyList(),
        )
    }

    fun removeMercenary(id: Int) {
        cart.remove(CartKey.Mercenary(id))
        mercenaryDisplayNames.remove(id)
    }

    fun simpleCount(type: InducementType): Int {
        val entry = cart[CartKey.Simple(type)] as? InducementSelectionCommon.Simple
        return entry?.count ?: 0
    }

    fun changeSimpleCount(inducement: SimpleInducement, delta: Int) {
        val key = CartKey.Simple(inducement.type)
        val current = simpleCount(inducement.type)
        val budgetLeft = dialog.pettyCash + dialog.treasury - totalPrice
        val next = when {
            delta > 0 -> {
                val price = inducement.getPrice(team)
                val affordable = if (price > 0) {
                    current + min(delta, budgetLeft / price)
                } else {
                    current + delta
                }
                min(inducement.max, affordable)
            }
            else -> max(0, current + delta)
        }
        if (next == 0) {
            cart.remove(key)
        } else {
            cart[key] = InducementSelectionCommon.Simple(inducement.type, next)
        }
    }

    fun groupItemsInCart(group: InducementGroup<*, *, *>): List<GroupItemView> {
        return group.items.mapNotNull { item ->
            val key = cartKeyFor(item) ?: return@mapNotNull null
            if (cart.containsKey(key)) {
                GroupItemView(key, item.name, item.getPrice(team), item, item.iconSource())
            } else {
                null
            }
        }
    }

    fun availableItemsInGroup(group: InducementGroup<*, *, *>): List<GroupItemView> {
        return group.items
            .filter { it.enabled }
            .filter { it.availableToTeam() }
            .mapNotNull { item ->
                val key = cartKeyFor(item) ?: return@mapNotNull null
                GroupItemView(key, item.name, item.getPrice(team), item, item.iconSource())
            }
    }

    fun isInCart(key: CartKey): Boolean = cart.containsKey(key)

    fun isGroupFull(group: InducementGroup<*, *, *>): Boolean {
        return groupItemsInCart(group).size >= group.max
    }

    fun toggleGroupItem(item: GroupItemView) {
        val key = item.key
        if (cart.containsKey(key)) {
            cart.remove(key)
        } else {
            cart[key] = buildSelection(item.inducement) ?: return
        }
    }

    fun openDrawer(type: InducementType) {
        activeDrawerGroupState.value = type
    }

    fun closeDrawer() {
        activeDrawerGroupState.value = null
    }

    fun submit(): InducementsSelected = InducementsSelected(cart.values.toList())

    private fun cartKeyFor(inducement: SingleInducement<*>): CartKey? {
        return when (inducement) {
            is WizardInducement -> CartKey.Wizard(inducement.wizard.id)
            is BiasedRefereeInducement -> CartKey.BiasedReferee(inducement.referee.type)
            is InfamousCoachingStaffInducement -> CartKey.InfamousCoach(inducement.staff.type)
            is StarPlayerInducement -> CartKey.StarPlayer(inducement.starPlayer.id)
            else -> null
        }
    }

    private fun buildSelection(inducement: SingleInducement<*>): InducementSelection<*>? {
        return when (val key = cartKeyFor(inducement)) {
            is CartKey.Wizard -> InducementSelectionCommon.Wizard(key.id)
            is CartKey.BiasedReferee -> InducementSelectionCommon.BiasedReferee(key.refereeType)
            is CartKey.InfamousCoach -> InducementSelectionCommon.InfamousCoach(key.coachType)
            is CartKey.StarPlayer -> InducementSelectionCommon.StarPlayer(key.position)
            else -> null
        }
    }

    private fun SingleInducement<*>.availableToTeam(): Boolean {
        return requirements.isEmpty() || team.specialRules.any { it in requirements }
    }

    private fun SingleInducement<*>.iconSource(): SpriteSource? {
        return when (this) {
            is StarPlayerInducement -> starPlayer.icon
            else -> null
        }
    }
}
