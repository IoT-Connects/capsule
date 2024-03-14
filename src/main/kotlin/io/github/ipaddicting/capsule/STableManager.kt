package io.github.ipaddicting.capsule

import jakarta.annotation.PostConstruct
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
class STableManager {
    private val log = LoggerFactory.getLogger(STableManager::class.java)

    var strategy =
        Strategy.valueOf(System.getProperty("capsule.database.generation", Strategy.DROP_AND_CREATE.name))

    @Inject
    lateinit var store: CapsuleQueryStore

    @Inject
    lateinit var connectionManager: ConnectionManager

    @PostConstruct
    fun init() {
        when (strategy) {
            Strategy.DROP_AND_CREATE -> dropAndCreate()
            Strategy.VALID -> valid()
        }
    }

    fun dropAndCreate() {
        connectionManager.get().use { conn ->
            conn.createStatement().use { stmt ->
                store.queries.values.forEach { schema ->
                    val drop = schema.drop()
                    val create = schema.create()

                    log.info("\n$drop\n$create")

                    stmt.executeUpdate(drop)
                    stmt.executeUpdate(create)
                }
            }
        }
    }

    fun valid() {
        TODO()
    }

    enum class Strategy {
        DROP_AND_CREATE,
        VALID,
    }
}
