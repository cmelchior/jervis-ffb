package com.jervisffb.engine.model.context

import com.jervisffb.engine.model.Ball
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.locations.FieldCoordinate
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
    // Temporary state tracking the current player being resolved for this push step.
    var fullyResolveInProgress: Player? = null,
    // Track any balls that must bounce after the push is resolved.
    // Either because a player was pushed into it, or because a trapdoor
    // swallowed a player with a ball. Balls should be added and resolved in
    // order.
    val looseBalls: MutableList<Ball> = mutableListOf(),
    // True, if the last player gets pushed into the crowd.
    val pushedIntoTheCrowd: Boolean = false
) : ProcedureContext {

    // Tracks if the attacker is using Juggernaut.
    val isAttackerUsingFrenzy: Boolean = firstPusher.hasSkill(SkillType.FRENZY)

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
        val from: FieldCoordinate,
        // Where is the pushee being pushed to?
        var to: FieldCoordinate? = null, // If `null` push direction has not been selected yet
        val isBlitzing: Boolean = false, // If first pusher is doing a Blitz
        val isChainPush: Boolean = false, // True for every push beyond the first
        var usedGrab: Boolean = false,
        var usedStandFirm: Boolean = false,
        var usedSideStep: Boolean = false,
        // Set to `true` if we checked the player in this step for scoring
        var checkedForScoringAfterTrapdoors: Boolean = false,
    )
}
