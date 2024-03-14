package io.github.ipaddicting.capsule

import jakarta.persistence.Column
import jakarta.persistence.Id
import java.sql.Timestamp
import java.time.Instant
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

@Suppress("TooManyFunctions")
object CapsuleExtensions {
    fun KProperty<*>.columnName(): String {
        val column = this.column()
        return column.name.ifEmpty { this.name.underscore() }
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
        return tag.name.ifEmpty { this.name.underscore() }
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

    fun Array<*>.asCommaSeparatedString(): String {
        return this.filterNotNull().joinToString { it.asValueString() }
    }

    fun CapsuleEntity.asTupleString(): String {
        var id = ""
        val columns = mutableListOf<Any>()

        this::class.memberProperties.forEach {
            if (it.javaField!!.isAnnotationPresent(Id::class.java)) {
                id = readInstancePropertyAsValueString(this, it.name)
            } else if (it.javaField!!.isAnnotationPresent(Column::class.java)) {
                columns.add(readInstancePropertyAsValueString(this, it.name))
            }
        }

        return "($id, ${columns.joinToString()})"
    }

    @Suppress("UNCHECKED_CAST")
    private fun readInstancePropertyAsValueString(
        instance: Any,
        propertyName: String,
    ): String {
        val property =
            instance::class.members.first { it.name == propertyName } as KProperty1<Any, *>
        return property.get(instance)?.asValueString() ?: ""
    }

    private fun Any.asValueString(): String {
        return when (this) {
            is Instant -> "${this.toEpochMilli()}"
            is Timestamp -> "${this.time}"
            is Enum<*> -> "${this.ordinal}"
            is String -> "'$this'"
            is Boolean -> if (this) "1" else "0"
            else -> "$this"
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

    private fun String.underscore(): String {
        return camelRegex.replace(this) { "_" + it.value.replaceFirstChar { letter -> letter.lowercase() } }
    }

    private val camelRegex = "(?<=[a-zA-Z0-9])[A-Z]".toRegex()
}
