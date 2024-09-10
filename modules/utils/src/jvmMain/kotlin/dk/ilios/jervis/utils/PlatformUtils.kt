package dk.ilios.jervis.utils

public actual fun threadId(): ULong {
    return Thread.currentThread().id.toULong()
}
