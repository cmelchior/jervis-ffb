package com.jervisffb.utils

import kotlin.reflect.KClass

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object ReflectionUtils {
    actual fun <E: Any> getEnumConstants(kClass: KClass<E>): Array<E> {
        return kClass.java.enumConstants
    }
}
