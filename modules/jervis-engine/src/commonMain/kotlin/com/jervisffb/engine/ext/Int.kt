package com.jervisffb.engine.ext

import com.jervisffb.engine.actions.D12Result
import com.jervisffb.engine.actions.D16Result
import com.jervisffb.engine.actions.D2Result
import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D4Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.DBlockResult

// Easy conversion to dice rolls
inline val Int.d2: D2Result get() = D2Result(this)
inline val Int.d3: D3Result get() = D3Result(this)
inline val Int.d4: D4Result get() = D4Result(this)
inline val Int.d6: D6Result get() = D6Result(this)
inline val Int.d8: D8Result get() = D8Result(this)
inline val Int.d12: D12Result get() = D12Result(this)
inline val Int.d16: D16Result get() = D16Result(this)
inline val Int.dblock: DBlockResult get() = DBlockResult(this)
