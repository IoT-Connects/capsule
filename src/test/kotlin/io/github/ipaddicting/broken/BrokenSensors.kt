package io.github.ipaddicting.broken

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

class BrokenSensor

@Entity
class NonTableSensor {
    @Id
    var id: Int = 0
}

@Entity
@Table
class WrongIdSensor {
    @Id
    var id: Long = 0
}
