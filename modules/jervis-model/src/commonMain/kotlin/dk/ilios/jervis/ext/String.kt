package dk.ilios.jervis.ext

import dk.ilios.jervis.model.PlayerId
import dk.ilios.jervis.model.PlayerNo

// Easy conversion of types
inline val String.playerId: PlayerId
    get() = PlayerId(this)

inline val Int.playerNo: PlayerNo
    get() = PlayerNo(this)
