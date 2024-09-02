package dk.ilios.jervis.ext

import dk.ilios.jervis.model.PlayerId

// Easy conversion of types
inline val String.playerId: PlayerId
    get() = PlayerId(this)
