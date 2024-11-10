package com.jervisffb.utils

import kotlin.reflect.KClass

expect object ReflectionUtils {
    fun <E: Any> getEnumConstants(kClass: KClass<E>): Array<E>
    fun <T: Any> getTypesInPackage(packageName: String, type: KClass<T>): List<KClass<out T>>
    fun isSubclassOf(type: KClass<*>, parentType: KClass<*>): Boolean
    fun <T: Any> objectInstance(type: KClass<T>): T
    fun simpleClassName(clazz: Any?): String?
}
