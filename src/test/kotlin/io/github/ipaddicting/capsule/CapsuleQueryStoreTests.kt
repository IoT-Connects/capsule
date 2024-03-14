package io.github.ipaddicting.capsule

import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldNotBe

class CapsuleQueryStoreTests : FeatureSpec({
    val store = CapsuleQueryStore()
    store.dataTypes = TestProjectListener.dataTypes

    feature("Store managing") {
        scenario("Initialization") {
            store.prepare()
            store.queries.size shouldBeEqual 1
        }

        scenario("Retrieval") {
            val meter = store.get("Meter")

            meter!! shouldNotBe null
            meter.stableName shouldBeEqual "meters"
            meter.columns.size shouldBeEqual 3
            meter.tags.size shouldBeEqual 3
        }
    }
})
