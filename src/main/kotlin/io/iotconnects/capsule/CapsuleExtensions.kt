package io.iotconnects.capsule

import jakarta.persistence.Column
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
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

    fun KProperty<*>.columnType(dataTypes: Map<KClass<out Any>, Type>): String {
        val column = this.column()
        val returnType = this.returnType.classifier!! as KClass<out Any>
        val dataType = returnType.dataType(dataTypes)

        return when (returnType) {
            String::class -> "${dataType.definition()}(${column.length})"
            else -> dataType.definition()
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

    fun KProperty<*>.tagType(dataTypes: Map<KClass<out Any>, Type>): String {
        val tag = this.tag()
        val returnType = this.returnType.classifier!! as KClass<out Any>
        val dataType = returnType.dataType(dataTypes)

        return when (returnType) {
            String::class -> "${dataType.definition()}(${tag.length})"
            else -> dataType.definition()
        }
    }

    private fun KClass<out Any>.dataType(dataTypes: Map<KClass<out Any>, Type>): Type {
        return if (this.isSubclassOf(Enum::class)) {
            dataTypes[Enum::class]!!
        } else {
            dataTypes.getOrElse(this) {
                throw CapsuleExceptions("Unsupported type of data - $this!")
            }
        }
    }

    private fun KProperty<*>.column() = this.javaField!!.getAnnotation(Column::class.java)

    private fun KProperty<*>.tag() = this.findAnnotation<Tag>()!!

    private val camelRegex = "(?<=[a-zA-Z0-9])[A-Z]".toRegex()
}
