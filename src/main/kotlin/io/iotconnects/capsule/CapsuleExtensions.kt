package io.iotconnects.capsule

import jakarta.persistence.Column
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import java.sql.Timestamp
import java.time.Instant
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaField

object CapsuleExtensions {
    fun KProperty<*>.columnName(): String {
        val column = this.column()
        return column.name.ifEmpty {
            camelRegex.replace(this.name) {
                "_" + it.value.replaceFirstChar { letter -> letter.lowercase() }
            }
        }
    }

    fun KProperty<*>.columnType(): String {
        val column = this.column()

        val enumerated = this.javaField!!.getAnnotation(Enumerated::class.java)
        if (enumerated != null) {
            return when (enumerated.value) {
                EnumType.ORDINAL -> "TINYINT UNSIGNED"
                EnumType.STRING -> "NCHAR(${column.length})"
            }
        }

        return when (this.returnType.classifier) {
            Timestamp::class, Instant::class -> "TIMESTAMP"
            String::class -> "NCHAR(${column.length})"
            Boolean::class -> "BOOL"
            Int::class -> "INT"
            UInt::class -> "INT UNSIGNED"
            Long::class -> "BIGINT"
            ULong::class -> "BIGINT UNSIGNED"
            Float::class -> "FLOAT"
            Double::class -> "DOUBLE"
            else -> throw CapsuleExceptions("Unsupported type of column - ${this.returnType}!")
        }
    }

    fun KProperty<*>.tagName(): String {
        val tag = this.tag()
        return tag.name.ifEmpty {
            camelRegex.replace(this.name) {
                "_" + it.value.replaceFirstChar { letter -> letter.lowercase() }
            }
        }
    }

    fun KProperty<*>.tagType(): String {
        val tag = this.tag()

        val enumerated = this.javaField!!.getAnnotation(Enumerated::class.java)
        if (enumerated != null) {
            return when (enumerated.value) {
                EnumType.ORDINAL -> "TINYINT UNSIGNED"
                EnumType.STRING -> "NCHAR(${tag.length})"
            }
        }

        return when (this.returnType.classifier) {
            Timestamp::class, Instant::class -> "TIMESTAMP"
            String::class -> "NCHAR(${tag.length})"
            Boolean::class -> "BOOL"
            Int::class -> "INT"
            UInt::class -> "INT UNSIGNED"
            Long::class -> "BIGINT"
            ULong::class -> "BIGINT UNSIGNED"
            Float::class -> "FLOAT"
            Double::class -> "DOUBLE"
            else -> throw CapsuleExceptions("Unsupported type of tag - ${this.returnType}!")
        }
    }

    private fun KProperty<*>.column() = this.javaField!!.getAnnotation(Column::class.java)

    private fun KProperty<*>.tag() = this.findAnnotation<Tag>()!!

    private val camelRegex = "(?<=[a-zA-Z0-9])[A-Z]".toRegex()
}
