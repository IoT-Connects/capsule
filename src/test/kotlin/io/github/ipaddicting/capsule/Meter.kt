package io.github.ipaddicting.capsule

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Enumerated
import jakarta.persistence.Table

@Entity(name = "meter")
@Table(name = "meters")
class Meter : CapsuleEntity() {
    @Column
    var current: Float = 0.0f

    @Column
    var voltage: Float = 0.0f

    @Column
    var phase: Float = 0.0f

    @Tag
    @Enumerated
    lateinit var location: MeterLocation

    @Tag(length = 128)
    lateinit var remarks: String

    @Tag
    var groupId: Int = 0
}

enum class MeterLocation {
    HOME,
    CHARGER,
}
