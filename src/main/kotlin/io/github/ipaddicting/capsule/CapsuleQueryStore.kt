package io.github.ipaddicting.capsule

import jakarta.annotation.PostConstruct
import jakarta.inject.Inject
import jakarta.inject.Singleton
import jakarta.persistence.Entity
import org.reflections.Reflections
import kotlin.reflect.KClass

/**
 * The store of [CapsuleQuery]s.
 */
@Singleton
class CapsuleQueryStore {
    private val prefix = System.getProperty("capsule.scanning-package.prefix", "io.github.ipaddicting.capsule")

    val queries = mutableMapOf<String, CapsuleQuery>()

    @Inject
    lateinit var dataTypes: Map<KClass<out Any>, Type>

    @PostConstruct
    fun prepare() {
        val entities = Reflections(prefix).getTypesAnnotatedWith(Entity::class.java)
        // transform classes to schemas
        entities
            .forEach {
                val query = CapsuleQuery(it.kotlin, dataTypes)
                queries[query.entityName] = query
            }
    }

    fun get(entityName: String): CapsuleQuery? = queries[entityName]
}
