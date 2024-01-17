package io.iotconnects.capsule

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import java.sql.SQLException

class STableGeneratorTests : FeatureSpec({
    val schema = TestProjectListener.testingSchema

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
