package io.github.ipaddicting.capsule

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import java.sql.SQLException
import java.time.Instant

class CapsuleEntityTests : FeatureSpec({
    val query = TestProjectListener.testingQuery

    feature("STable & table creating") {
        scenario("Creates a super table") {
            TestProjectListener.connectionManager.get().use {
                it.createStatement().use { statement ->
                    statement.executeUpdate(query.create())
                }
            }
        }

        scenario("Describes the super table") {
            TestProjectListener.connectionManager.get().use {
                it.createStatement().use { statement ->
                    statement.executeQuery(query.describe()).use { resultSet ->
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

        scenario("Creates a table") {
            TestProjectListener.connectionManager.get().use {
                it.createStatement().use { statement ->
                    statement.executeUpdate(
                        query.createTable("d1001", arrayOf(1, MeterLocation.HOME, "bla bla bla")),
                    )

                    statement.executeUpdate(
                        query.createTable("d1002", arrayOf(1, MeterLocation.CHARGER, "bla bla bla")),
                    )
                }
            }
        }
    }

    feature("Data insertion") {
        scenario("Inserts data into the table") {
            val now = Instant.now()
            val records =
                mapOf(
                    "d1001" to
                        listOf(
                            Meter().apply {
                                ts = now.minusMillis(1)
                                current = 1.0f
                                voltage = 1.0f
                                phase = 1.0f
                            },
                            Meter().apply {
                                ts = now
                                current = 1.5f
                                voltage = 1.5f
                                phase = 1.5f
                            },
                        ),
                    "d1002" to
                        listOf(
                            Meter().apply {
                                ts = now
                                current = 2.0f
                                voltage = 2.0f
                                phase = 2.0f
                            },
                        ),
                )

            TestProjectListener.connectionManager.get().use {
                it.createStatement().use { statement ->
                    statement.executeUpdate(query.insert(records))
                }
            }
        }

        scenario("Counting records") {
            TestProjectListener.connectionManager.get().use {
                it.createStatement().use { statement ->
                    statement.executeQuery(query.count()).use { resultSet ->
                        resultSet.next()
                        resultSet.getInt(1) shouldBe 3
                    }

                    statement.executeQuery(query.count("d1001")).use { resultSet ->
                        resultSet.next()
                        resultSet.getInt(1) shouldBe 2
                    }
                }
            }
        }
    }

    feature("Table & STable dropping") {
        scenario("Drops the table") {
            TestProjectListener.connectionManager.get().use {
                it.createStatement().use { statement ->
                    statement.executeUpdate(query.dropTable("d1001"))
                }
            }
        }

        scenario("Drops the super table") {
            TestProjectListener.connectionManager.get().use {
                it.createStatement().use { statement ->
                    statement.executeUpdate(query.drop())
                }
            }
        }

        scenario("Error when describing non-existed the super table") {
            val exception =
                shouldThrow<SQLException> {
                    TestProjectListener.connectionManager.get().use {
                        it.createStatement().use { statement ->
                            statement.executeUpdate(query.describe())
                        }
                    }
                }

            exception.message?.shouldBeEqual(
                "TDengine ERROR (0x2603): sql: DESCRIBE meters;, desc: Table does not exist",
            )
        }
    }
})
