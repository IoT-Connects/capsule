package io.iotconnects.capsule

import jakarta.annotation.PostConstruct
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
class STableGenerator {
    private val strategy =
        Strategy.valueOf(System.getProperty("capsule.database.generation", Strategy.DROP_AND_CREATE.name))

    private val log = LoggerFactory.getLogger(STableGenerator::class.java)

    @Inject
    private lateinit var store: STableSchemasStore

    @Inject
    private lateinit var connectionManager: ConnectionManager

    @PostConstruct
    fun init() {
        when (strategy) {
            Strategy.DROP_AND_CREATE -> dropAndCreate()
        }
    }

    private fun dropAndCreate() {
        store.schemas.values.forEach { schema ->
            connectionManager.get().use { conn ->
                conn.createStatement().use { stmt ->
                    val drop = schema.drop()
                    log.info(drop)
                    stmt.executeUpdate(drop)

                    val create = schema.create()
                    log.info(create)
                    stmt.executeUpdate(create)
                }
            }
        }
    }

    enum class Strategy {
        DROP_AND_CREATE,
    }
}
