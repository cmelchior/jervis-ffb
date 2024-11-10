package com.jervisffb.utils

import kotlin.reflect.KClass

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object ReflectionUtils {
    actual fun <E: Any> getEnumConstants(kClass: KClass<E>): Array<E> {
        TODO("Not supported on WasmJS") // Figure out a way to do this (or make sure it isn't used on JS)
    }
    actual fun <T : Any> getTypesInPackage(packageName: String, type: KClass<T>): List<KClass<out T>> {
        TODO("Not yet implemented")
    }
    actual fun isSubclassOf(type: KClass<*>, parentType: KClass<*>): Boolean = TODO()
    actual fun <T: Any> objectInstance(type: KClass<T>): T = TODO()
    actual fun simpleClassName(clazz: Any?): String? = TODO()
}
