package dk.ilios.jervis.ext

import dk.ilios.jervis.actions.D12Result
import dk.ilios.jervis.actions.D16Result
import dk.ilios.jervis.actions.D2Result
import dk.ilios.jervis.actions.D3Result
import dk.ilios.jervis.actions.D4Result
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.D8Result


// Easy conversion to dice rolls
inline val Int.d2: D2Result get() = D2Result(this)
inline val Int.d3: D3Result get() = D3Result(this)
inline val Int.d4: D4Result get() = D4Result(this)
inline val Int.d6: D6Result get() = D6Result(this)
inline val Int.d8: D8Result get() = D8Result(this)
inline val Int.d12: D12Result get() = D12Result(this)
inline val Int.d16: D16Result get() = D16Result(this)
