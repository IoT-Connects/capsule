package io.iotconnects.capsule

import jakarta.annotation.PostConstruct
import jakarta.inject.Inject
import jakarta.inject.Singleton
import jakarta.persistence.Entity
import org.reflections.Reflections
import kotlin.reflect.KClass

/**
 * The store of [STableSchema]s.
 */
@Singleton
class STableSchemasStore {
    private val prefix = System.getProperty("capsule.scanning-package.prefix", "io.iotconnects.capsule")

    val schemas = mutableMapOf<String, STableSchema>()

    @Inject
    lateinit var dataTypes: Map<KClass<out Any>, Type>

    @PostConstruct
    fun prepare() {
        val entities = Reflections(prefix).getTypesAnnotatedWith(Entity::class.java)
        // transform classes to schemas
        entities.forEach {
            val schema = STableSchema(it::class, dataTypes)
            schemas[schema.entityName] = schema
        }
    }

    fun get(entityName: String): STableSchema? = schemas[entityName]
}
