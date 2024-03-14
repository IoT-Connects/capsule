package io.github.ipaddicting.capsule

import io.kotest.core.annotation.AutoScan
import io.kotest.core.listeners.AfterProjectListener
import io.kotest.core.listeners.BeforeProjectListener
import java.sql.Timestamp
import java.time.Instant
import kotlin.reflect.KClass

@AutoScan
object TestProjectListener : BeforeProjectListener, AfterProjectListener {
    val dataTypes: Map<KClass<out Any>, Type> =
        mapOf(
            String::class to StringType(),
            Int::class to IntType(),
            UInt::class to UIntType(),
            Long::class to LongType(),
            ULong::class to ULongType(),
            Float::class to FloatType(),
            Double::class to DoubleType(),
            Boolean::class to BooleanType(),
            Enum::class to EnumType(),
            Timestamp::class to TimestampType(),
            Instant::class to InstantType(),
        )
    val testingQuery = CapsuleQuery(Meter::class, dataTypes)
    val connectionManager = ConnectionManager()

    override suspend fun beforeProject() {
        connectionManager.init()
    }

    override suspend fun afterProject() {
        connectionManager.del()
    }
}
