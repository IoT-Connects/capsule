package io.iotconnects.capsule

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.equals.shouldBeEqual

class STableSchemaTests : FeatureSpec({
    feature("STable schema generating") {
        val schema = TestProjectListener.testingSchema
        val dataTypes = TestProjectListener.dataTypes

        scenario("DDL of super table creating & dropping") {
            val create = schema.create()
            create shouldBeEqual
                """
                |CREATE STABLE IF NOT EXISTS meters (
                |    ts TIMESTAMP,
                |    current FLOAT,
                |    phase FLOAT,
                |    voltage FLOAT
                |) TAGS (
                |    group_id INT,
                |    location TINYINT UNSIGNED,
                |    remarks NCHAR(128)
                |);
                """.trimMargin()

            val describe = schema.describe()
            describe shouldBeEqual
                """
                |DESCRIBE meters;
                """.trimMargin()

            val drop = schema.drop()
            drop shouldBeEqual
                """
                |DROP STABLE IF EXISTS meters;
                """.trimMargin()
        }

        scenario("Broken sensor without @Entity") {
            val exception = shouldThrow<CapsuleExceptions> { STableSchema(BrokenSensor::class, dataTypes) }
            exception.message?.shouldBeEqual("No '@Entity' was declared in the entity class BrokenSensor!")
        }

        scenario("Wrong sensor without @Table") {
            val exception = shouldThrow<CapsuleExceptions> { STableSchema(NonTableSensor::class, dataTypes) }
            exception.message?.shouldBeEqual("No '@Table' was declared in the entity class NonTableSensor!")
        }

        scenario("Wrong configured sensor without the proper @Id") {
            val exception = shouldThrow<CapsuleExceptions> { STableSchema(WrongIdSensor::class, dataTypes) }
            exception.message?.shouldBeEqual(
                "The '@Id' must be of type 'Timestamp' or 'Instant' in the entity class WrongIdSensor!",
            )
        }
    }
})
