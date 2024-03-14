package io.github.ipaddicting.capsule

import jakarta.persistence.PersistenceException

/**
 * The base type for exceptions thrown by Capsule.
 */
class CapsuleExceptions : PersistenceException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
