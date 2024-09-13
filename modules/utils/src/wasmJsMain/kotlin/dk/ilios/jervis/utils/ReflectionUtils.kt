package dk.ilios.jervis.utils

import kotlin.reflect.KClass

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object ReflectionUtils {
    actual fun <E: Any> getEnumConstants(kClass: KClass<E>): Array<E> {
        TODO("Not supported on WasmJS") // Figure out a way to do this (or make sure it isn't used on JS)
    }
}
