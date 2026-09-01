package com.jervisffb.test.bb2025

import com.jervisffb.engine.bb2025.StandardBB2025Rules
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.SkillValue
import com.jervisffb.engine.rules.common.skills.SkillType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for parsing serialized skill ids in [com.jervisffb.engine.rules.common.skills.SkillSettings].
 * Serialized team data stores skills as strings (see SerializedPlayer.serialize → SkillId.serialize),
 * so the parser must round-trip every format [SkillId.serialize] can emit.
 */
class SkillSettingsTests {

    val rules = StandardBB2025Rules()

    @Test
    fun getSkillId_unvaluedSkill() {
        val skillId = rules.skillSettings.getSkillId("BLOCK")
        assertEquals(SkillType.BLOCK, skillId?.type)
        assertEquals(SkillValue.None, skillId?.value)
    }

    @Test
    fun getSkillId_parenthesizedIntAdjustment() {
        val skillId = rules.skillSettings.getSkillId("MIGHTY_BLOW(+1)")
        assertEquals(SkillType.MIGHTY_BLOW, skillId?.type)
        assertEquals(SkillValue.IntAdjustment(1), skillId?.value)
    }

    @Test
    fun getSkillId_bareIntAdjustmentSuffix() {
        // SkillId.serialize() emits IntAdjustment values as a bare "+N" suffix,
        // e.g. "MIGHTY_BLOW+1". This must parse back to the same skill.
        val skillId = rules.skillSettings.getSkillId("MIGHTY_BLOW+1")
        assertEquals(SkillType.MIGHTY_BLOW, skillId?.type)
        assertEquals(SkillValue.IntAdjustment(1), skillId?.value)
    }

    @Test
    fun getSkillId_bareNegativeIntAdjustmentSuffix() {
        val skillId = rules.skillSettings.getSkillId("DIRTY_PLAYER-1")
        assertEquals(SkillType.DIRTY_PLAYER, skillId?.type)
        assertEquals(SkillValue.IntAdjustment(-1), skillId?.value)
    }

    @Test
    fun getSkillId_intTarget() {
        val skillId = rules.skillSettings.getSkillId("LONER(4+)")
        assertEquals(SkillType.LONER, skillId?.type)
        assertEquals(SkillValue.IntTarget(4), skillId?.value)
    }

    @Test
    fun getSkillId_serializedFormRoundTrips() {
        val original = SkillId(SkillType.MIGHTY_BLOW, SkillValue.IntAdjustment(1))
        val parsed = rules.skillSettings.getSkillId(original.serialize())
        assertEquals(original, parsed)
    }

    @Test
    fun getSkillId_unknownSkill_returnsNull() {
        assertNull(rules.skillSettings.getSkillId("NOT_A_SKILL"))
    }
}
