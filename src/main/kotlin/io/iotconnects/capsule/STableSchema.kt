package io.iotconnects.capsule

import io.iotconnects.capsule.CapsuleExtensions.columnName
import io.iotconnects.capsule.CapsuleExtensions.columnType
import io.iotconnects.capsule.CapsuleExtensions.tagName
import io.iotconnects.capsule.CapsuleExtensions.tagType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.sql.Timestamp
import java.time.Instant
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

class STableSchema(entityClass: KClass<out Any>) {
    private var entityName: String = entityClass.simpleName!!
    private var stableName: String

    private val idClassifiers = setOf(Timestamp::class, Instant::class)
    private lateinit var id: KProperty<*>

    private var columns = mutableListOf<KProperty<*>>()
    private var tags = mutableListOf<KProperty<*>>()

    init {
        if (!entityClass.hasAnnotation<Entity>()) {
            throw CapsuleExceptions("No '@Entity' was declared in the entity class $entityName!")
        }

        if (!entityClass.hasAnnotation<Table>()) {
            throw CapsuleExceptions("No '@Table' was declared in the entity class $entityName!")
        }

        stableName = entityClass.findAnnotation<Table>()!!.name

        entityClass.memberProperties.forEach {
            if (it.javaField!!.isAnnotationPresent(Id::class.java)) {
                id = it
            } else if (it.javaField!!.isAnnotationPresent(Column::class.java)) {
                columns.add(it)
            } else if (it.hasAnnotation<Tag>()) {
                tags.add(it)
            }
        }

        if (!::id.isInitialized) {
            throw CapsuleExceptions("No '@Id' was declared in the entity class $entityName!")
        } else {
            if (!idClassifiers.contains(id.returnType.classifier)) {
                throw CapsuleExceptions(
                    "The '@Id' must be of type 'Timestamp' or 'Instant' in the entity class $entityName!",
                )
            }
        }
    }

    /**
     * Generates the SQL statement for creating the super table.
     */
    fun create(): String {
        return """
            |CREATE STABLE IF NOT EXISTS $stableName (
            |    ${id.name} TIMESTAMP,
            |${columns.joinToString(",\n") { "    ${it.columnName()} ${it.columnType()}" }}
            |) TAGS (
            |${tags.joinToString(",\n") { "    ${it.tagName()} ${it.tagType()}" }}
            |);
            """.trimMargin()
    }

    /**
     * Generates the SQL statement for describing the super table.
     */
    fun describe(): String {
        return """
            |DESCRIBE $stableName;
            """.trimMargin()
    }

    /**
     * Generates the SQL statement for dropping the super table.
     */
    fun drop(): String {
        return """
            |DROP STABLE IF EXISTS $stableName;
            """.trimMargin()
    }
}
