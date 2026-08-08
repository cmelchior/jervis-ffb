package com.jervisffb.engine.statistics.probability

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class AlgorithmId(val value: String)

@Serializable
@JvmInline
value class RerollUsagePolicyId(val value: String)
