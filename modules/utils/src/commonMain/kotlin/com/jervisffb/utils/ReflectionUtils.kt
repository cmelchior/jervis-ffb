package com.jervisffb.utils

import kotlin.reflect.KClass

expect object ReflectionUtils {
    fun <E: Any> getEnumConstants(kClass: KClass<E>): Array<E>
}
