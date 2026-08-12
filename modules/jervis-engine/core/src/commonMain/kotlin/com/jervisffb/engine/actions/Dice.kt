package com.jervisffb.engine.actions

/**
 * List of the different dice available in Blood Bowl.
 */
enum class Dice(val sides: Int) {
    D2(2),
    D3(3),
    D4(4),
    D6(6),
    D8(8),
    D12(12),
    D16(16),
    D20(20),
    BLOCK(6),
}
