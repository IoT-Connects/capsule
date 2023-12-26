package io.iotconnects.capsule

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import java.sql.SQLException

class STableSchemaTests : FeatureSpec({
    val schema = STableSchema(Meter::class)

    feature("STable schema generating") {
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
            drop shouldBeEqual "DROP STABLE IF EXISTS meters;"
        }

        scenario("Broken sensor without @Entity") {
            val exception = shouldThrow<CapsuleExceptions> { STableSchema(BrokenSensor::class) }
            exception.message?.shouldBeEqual("No '@Entity' was declared in the entity class BrokenSensor!")
        }

        scenario("Wrong sensor without @Table") {
            val exception = shouldThrow<CapsuleExceptions> { STableSchema(NonTableSensor::class) }
            exception.message?.shouldBeEqual("No '@Table' was declared in the entity class NonTableSensor!")
        }

        scenario("Wrong configured sensor without the proper @Id") {
            val exception = shouldThrow<CapsuleExceptions> { STableSchema(WrongIdSensor::class) }
            exception.message?.shouldBeEqual(
                "The '@Id' must be of type 'Timestamp' or 'Instant' in the entity class WrongIdSensor!",
            )
        }
    }

    feature("STable creating & dropping") {
        scenario("Creates a super table") {
            TestProjectListener.connectionManager.get().use {
                it.createStatement().use { statement ->
                    statement.executeUpdate(schema.create())
                }
            }
        }

        scenario("Describes the super table") {
            TestProjectListener.connectionManager.get().use {
                it.createStatement().use { statement ->
                    statement.executeQuery(schema.describe()).use { resultSet ->
                        resultSet.next()
                        resultSet.getString("field") shouldBe "ts"
                        resultSet.getString("type") shouldBe "TIMESTAMP"
                        resultSet.getInt("length") shouldBe 8

                        resultSet.next()
                        resultSet.getString("field") shouldBe "current"
                        resultSet.getString("type") shouldBe "FLOAT"
                        resultSet.getInt("length") shouldBe 4

                        resultSet.next()
                        resultSet.getString("field") shouldBe "phase"
                        resultSet.getString("type") shouldBe "FLOAT"
                        resultSet.getInt("length") shouldBe 4

                        resultSet.next()
                        resultSet.getString("field") shouldBe "voltage"
                        resultSet.getString("type") shouldBe "FLOAT"
                        resultSet.getInt("length") shouldBe 4

                        resultSet.next()
                        resultSet.getString("field") shouldBe "group_id"
                        resultSet.getString("type") shouldBe "INT"
                        resultSet.getInt("length") shouldBe 4
                        resultSet.getString("note") shouldBe "TAG"

                        resultSet.next()
                        resultSet.getString("field") shouldBe "location"
                        resultSet.getString("type") shouldBe "TINYINT UNSIGNED"
                        resultSet.getString("note") shouldBe "TAG"

                        resultSet.next()
                        resultSet.getString("field") shouldBe "remarks"
                        resultSet.getString("type") shouldBe "NCHAR"
                        resultSet.getString("note") shouldBe "TAG"
                    }
                }
            }
        }

        scenario("Drops the super table") {
            TestProjectListener.connectionManager.get().use {
                it.createStatement().use { statement ->
                    statement.executeUpdate(schema.drop())
                }
            }
        }

        scenario("Error when describing non-existed the super table") {
            val exception =
                shouldThrow<SQLException> {
                    TestProjectListener.connectionManager.get().use {
                        it.createStatement().use { statement ->
                            statement.executeUpdate(schema.describe())
                        }
                    }
                }

            exception.message?.shouldBeEqual(
                "TDengine ERROR (0x2603): sql: DESCRIBE meters;, desc: Table does not exist",
            )
        }
    }
})
