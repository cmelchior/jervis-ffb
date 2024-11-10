package com.jervisffb.utils

import org.reflections.Reflections
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object ReflectionUtils {
    actual fun <E: Any> getEnumConstants(kClass: KClass<E>): Array<E> {
        return kClass.java.enumConstants
    }
    actual fun <T : Any> getTypesInPackage(packageName: String, type: KClass<T>): List<KClass<out T>> {
        val reflections = Reflections(packageName)
        return reflections.getSubTypesOf(type.java).map { it.kotlin }.toList()
    }
    actual fun isSubclassOf(type: KClass<*>, parentType: KClass<*>): Boolean {
        return type.isSubclassOf(parentType)
    }
    actual fun <T: Any> objectInstance(type: KClass<T>): T = type.objectInstance!!
    actual fun simpleClassName(clazz: Any?): String? {
        return clazz?.let { it::class.simpleName }
    }
}
