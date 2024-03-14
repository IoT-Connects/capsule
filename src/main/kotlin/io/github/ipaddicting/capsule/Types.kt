package io.github.ipaddicting.capsule

import jakarta.inject.Singleton
import java.sql.Timestamp
import java.time.Instant
import kotlin.reflect.KClass

interface Type {
    fun name(): String

    fun returnedClass(): KClass<*>

    fun definition(): String
}

@Singleton
class StringType : Type {
    override fun name(): String = "String"

    override fun returnedClass(): KClass<*> = String::class

    override fun definition(): String = "NCHAR"
}

@Singleton
class BooleanType : Type {
    override fun name(): String = "Boolean"

    override fun returnedClass(): KClass<*> = Boolean::class

    override fun definition(): String = "BOOL"
}

@Singleton
class IntType : Type {
    override fun name(): String = "Int"

    override fun returnedClass(): KClass<*> = Int::class

    override fun definition(): String = "INT"
}

@Singleton
class UIntType : Type {
    override fun name(): String = "UInt"

    override fun returnedClass(): KClass<*> = UInt::class

    override fun definition(): String = "INT UNSIGNED"
}

@Singleton
class LongType : Type {
    override fun name(): String = "Long"

    override fun returnedClass(): KClass<*> = Long::class

    override fun definition(): String = "BIGINT"
}

@Singleton
class ULongType : Type {
    override fun name(): String = "ULong"

    override fun returnedClass(): KClass<*> = ULong::class

    override fun definition(): String = "BIGINT UNSIGNED"
}

@Singleton
class FloatType : Type {
    override fun name(): String = "Float"

    override fun returnedClass(): KClass<*> = Float::class

    override fun definition(): String = "FLOAT"
}

@Singleton
class DoubleType : Type {
    override fun name(): String = "Double"

    override fun returnedClass(): KClass<*> = Double::class

    override fun definition(): String = "DOUBLE"
}

@Singleton
class EnumType : Type {
    override fun name(): String = "Enum"

    override fun returnedClass(): KClass<*> = Enum::class

    override fun definition(): String = "TINYINT UNSIGNED"
}

@Singleton
class TimestampType : Type {
    override fun name(): String = "TIMESTAMP"

    override fun returnedClass(): KClass<*> = Timestamp::class

    override fun definition(): String = "TIMESTAMP"
}

@Singleton
class InstantType : Type {
    override fun name(): String = "INSTANT"

    override fun returnedClass(): KClass<*> = Instant::class

    override fun definition(): String = "TIMESTAMP"
}
