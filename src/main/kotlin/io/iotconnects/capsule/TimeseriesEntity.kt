package io.iotconnects.capsule

import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import java.time.Instant

/**
 * Represents an entity with a generated ID field [ts] of type [Instant].
 */
@MappedSuperclass
open class TimeseriesEntity {
    /**
     * The ID field. This field is set by creation time or actually recorded time.
     */
    @Id open var ts: Instant = Instant.now()

    /**
     * Default `toString()` implementation
     *
     * @return the class type and ID type
     */
    override fun toString() = "${javaClass.simpleName}<$ts>"
}
