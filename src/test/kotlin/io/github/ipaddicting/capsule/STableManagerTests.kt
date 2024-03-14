package io.github.ipaddicting.capsule

import io.kotest.core.spec.style.FeatureSpec

class STableManagerTests : FeatureSpec({
    feature("STable managing") {
        val store = CapsuleQueryStore()
        store.dataTypes = TestProjectListener.dataTypes
        store.prepare()

        val manager = STableManager()
        manager.store = store
        manager.connectionManager = TestProjectListener.connectionManager

        scenario("Drop & Create") {
            manager.dropAndCreate()
        }
    }
})
