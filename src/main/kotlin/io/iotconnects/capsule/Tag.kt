package io.iotconnects.capsule

/**
 * Specifies the mapped tag for a persistent property or field.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Tag(val name: String = "", val length: Int = 255)
