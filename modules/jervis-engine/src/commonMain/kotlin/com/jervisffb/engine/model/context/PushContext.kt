package com.jervisffb.engine.model.context

import com.jervisffb.engine.model.Ball
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.common.skills.SkillType

data class PushContext(
    // Player starting the first push
    val firstPusher: Player,
    // First player being pushed
    val firstPushee: Player,
    // Tracks if the attacker is using Juggernaut.
    val isAttackerUsingJuggernaut: Boolean,
    // firstPushee is knocked down after the pushback has resolved. So if they
    // have the ball, it will bounce.
    val isDefenderKnockedDown: Boolean,
    // Is the push part of a multiple block
    val isMultipleBlock: Boolean,
    // Chain of pushes. For a single push, this contains one element
    // Should only be modified from within the `PushStep` procedure.
    val pushChain: MutableList<PushData>,
    // If `true`, the `firstPusher` will use Fend, preventing follow-up
    var defenderIsUsingFend: Boolean = false,
    // If `true`, the `firstPusher` will use Taunt, forcing a follow-up
    var defenderIsUsingTaunt: Boolean = false,
    // If `true`, the `firstPusher` will follow up after resolving the rest of the chain.
    var followsUp: Boolean = false,
    // Track any balls that must bounce after the push is resolved.
    // Either because a player was pushed into it, or because a trapdoor
    // swallowed a player with a ball. Balls should be added and resolved in
    // order.
    val looseBalls: MutableList<Ball> = mutableListOf(),
    // True, if the last player gets pushed into the crowd.
    val pushedIntoTheCrowd: Boolean = false
) : ProcedureContext {

    val isFirstBlock: Boolean = (pushChain.size == 1)

    // Tracks if the attacker is using Frenzy
    val isAttackerUsingFrenzy: Boolean = firstPusher.hasSkill(SkillType.FRENZY)

    // Tracks if a defender in the Push Chain cannot be moved, and thus stops
    // the Push Chain. This can happen if Stand Firm is used or a player is
    // Rooted.
    val isDefenderImmovable: Boolean
        get() = pushChain.any { it.usedStandFirm || it.defenderIsRooted }

    // Returns last "pusher" in the push chain
    fun pusher(): Player {
        return pushChain.last().pusher
    }

    // Returns the last "pushee" in the chain
    fun pushee(): Player {
        return pushChain.last().pushee
    }

    data class PushData(
        val pusher: Player,
        val pushee: Player,
        // Where is the pushee being pushed from?
        val from: PitchCoordinate,
        // Where is the pushee being pushed to? Should have the same value as
        // `from` if the pushee cannot be pushed. `null` should only represent
        // a push direction has not been determined yet.
        var to: PitchCoordinate? = null,
        val isBlitzing: Boolean = false, // If first pusher is doing a Blitz
        val isChainPush: Boolean = false, // True for every push beyond the first
        var usedGrab: Boolean = false,
        var usedStandFirm: Boolean = false,
        var usedSideStep: Boolean = false,
        var defenderIsRooted: Boolean = false,
        // Set to `true` if we checked the player in this step for scoring
        var checkedForScoringAfterTrapdoors: Boolean = false,
    )
}
