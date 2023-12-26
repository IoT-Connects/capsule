package io.iotconnects.capsule

import io.kotest.core.annotation.AutoScan
import io.kotest.core.listeners.AfterProjectListener
import io.kotest.core.listeners.BeforeProjectListener

@AutoScan
object TestProjectListener : BeforeProjectListener, AfterProjectListener {
    val connectionManager = ConnectionManager()

    override suspend fun beforeProject() {
        connectionManager.init()
    }

    override suspend fun afterProject() {
        connectionManager.del()
    }
}
