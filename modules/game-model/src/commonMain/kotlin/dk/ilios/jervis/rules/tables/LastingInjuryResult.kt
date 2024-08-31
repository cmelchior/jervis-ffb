package dk.ilios.jervis.rules.tables

import dk.ilios.jervis.model.modifiers.StatModifier

enum class LastingInjuryResult(val title: String, val modifier: StatModifier) {
    HEAD_INJURY("Head Injury", StatModifier.AV(1)),
    SMASHED_KNEE("Smashed Knee", StatModifier.MA(-1)),
    BROKEN_ARM("Broken Arm", StatModifier.PA(1)),
    NECK_INJURY("Neck Injury", StatModifier.AG(1)),
    DISLOCATED_SHOULDER("Dislocated Shoulder", StatModifier.ST(-1)),
}
