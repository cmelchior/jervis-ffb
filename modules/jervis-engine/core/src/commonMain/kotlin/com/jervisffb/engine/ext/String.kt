package com.jervisffb.engine.ext

import com.jervisffb.engine.model.BallId
import com.jervisffb.engine.model.ChallengeId
import com.jervisffb.engine.model.DicePoolId
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerNo

// Easy conversion of types

inline val String.ballId: BallId
    get() = BallId(this)

inline val String.challengeId: ChallengeId
    get() = ChallengeId(this)

inline val Int.dicePoolId: DicePoolId
    get() = DicePoolId(this)

inline val String.playerId: PlayerId
    get() = PlayerId(this)

inline val Int.playerNo: PlayerNo
    get() = PlayerNo(this)

