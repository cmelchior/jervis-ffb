package com.jervisffb.engine.ext

import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.SkillId

// Easy conversion of types
inline val String.playerId: PlayerId
    get() = PlayerId(this)

inline val Int.playerNo: PlayerNo
    get() = PlayerNo(this)

inline val String.skillId: SkillId
    get() = SkillId(this)
