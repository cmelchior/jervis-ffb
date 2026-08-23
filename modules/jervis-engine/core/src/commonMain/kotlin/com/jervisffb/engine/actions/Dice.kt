package com.jervisffb.engine.actions

/**
 * List of the different dice available in Blood Bowl.
 */
enum class Dice(val sides: Int, val allOptions: List<DieResult>) {
    D2(2, D2Result.allOptions()),
    D3(3, D3Result.allOptions()),
    D4(4, D4Result.allOptions()),
    D6(6, D6Result.allOptions()),
    D8(8, D8Result.allOptions()),
    D12(12, D12Result.allOptions()),
    D16(16, D16Result.allOptions()),
    D20(20, D20Result.allOptions()),
    BLOCK(6, DBlockResult.allOptions()),
}
