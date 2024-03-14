package io.github.ipaddicting.capsule

import io.github.ipaddicting.broken.BrokenSensor
import io.github.ipaddicting.broken.NonTableSensor
import io.github.ipaddicting.broken.WrongIdSensor
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.equals.shouldBeEqual
import java.time.Instant

class CapsuleQueryTests : FeatureSpec({
    val query = TestProjectListener.testingQuery
    val dataTypes = TestProjectListener.dataTypes

    feature("Exceptions checking") {
        scenario("Broken sensor without @Entity") {
            val exception = shouldThrow<CapsuleExceptions> { CapsuleQuery(BrokenSensor::class, dataTypes) }
            exception.message?.shouldBeEqual("No '@Entity' was declared in the entity class BrokenSensor!")
        }

        scenario("Wrong sensor without @Table") {
            val exception = shouldThrow<CapsuleExceptions> { CapsuleQuery(NonTableSensor::class, dataTypes) }
            exception.message?.shouldBeEqual("No '@Table' was declared in the entity class NonTableSensor!")
        }

        scenario("Wrong configured sensor without the proper @Id") {
            val exception = shouldThrow<CapsuleExceptions> { CapsuleQuery(WrongIdSensor::class, dataTypes) }
            exception.message?.shouldBeEqual(
                "The '@Id' must be of type 'Timestamp' or 'Instant' in the entity class WrongIdSensor!",
            )
        }
    }

    feature("DDL generating") {
        scenario("DDL for stable") {
            val create = query.create()
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

            val describe = query.describe()
            describe shouldBeEqual
                """
                |DESCRIBE meters;
                """.trimMargin()

            val drop = query.drop()
            drop shouldBeEqual
                """
                |DROP STABLE IF EXISTS meters;
                """.trimMargin()
        }

        scenario("DDL for table") {
            val create = query.createTable("d1001", arrayOf(1, MeterLocation.CHARGER, "bla bla bla"))
            create shouldBeEqual
                """
                |CREATE TABLE meters_d1001 
                |USING meters 
                |TAGS (1, 1, 'bla bla bla')
                """.trimMargin()

            val drop = query.dropTable("d1001")
            drop shouldBeEqual
                """
                |DROP TABLE IF EXISTS meters_d1001;
                """.trimMargin()
        }
    }

    feature("DML generating") {
        scenario("Insert") {
            val now = Instant.now()
            val meter1 = "d1001"
            val meter1Records =
                listOf(
                    Meter().apply {
                        ts = now.minusMillis(1)
                        current = 1.0f
                        voltage = 220.0f
                        phase = 10f
                    },
                    Meter().apply {
                        ts = now
                        current = 2.0f
                        voltage = 240.0f
                        phase = 15f
                    },
                )

            val meter2 = "d1002"
            val meter2Records =
                listOf(
                    Meter().apply {
                        ts = now
                        current = 3.0f
                        voltage = 220.0f
                        phase = 10f
                    },
                )

            val insert = query.insert(mapOf(meter1 to meter1Records, meter2 to meter2Records))
            insert shouldBeEqual
                """
                |INSERT INTO 
                |meters_d1001 VALUES (${now.minusMillis(1).toEpochMilli()}, 1.0, 10.0, 220.0)(${now.toEpochMilli()}, 2.0, 15.0, 240.0) 
                |meters_d1002 VALUES (${now.toEpochMilli()}, 3.0, 10.0, 220.0);
                """.trimMargin()
        }

        scenario("Count") {
            val count = query.count()
            count shouldBeEqual
                """
                |SELECT COUNT(*) FROM meters;
                """.trimMargin()

            val countTable = query.count("d1001")
            countTable shouldBeEqual
                """
                |SELECT COUNT(*) FROM meters_d1001;
                """.trimMargin()
        }

        scenario("Last Row") {
            val lastRow = query.lastRow()
            lastRow shouldBeEqual
                """
                |SELECT LAST_ROW(*) FROM meters;
                """.trimMargin()

            val lastRowTable = query.lastRow("d1001")
            lastRowTable shouldBeEqual
                """
                |SELECT LAST_ROW(*) FROM meters_d1001;
                """.trimMargin()
        }
    }
})
