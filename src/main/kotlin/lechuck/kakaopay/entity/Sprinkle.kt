package lechuck.kakaopay.entity

import org.hibernate.annotations.CreationTimestamp
import java.sql.Timestamp
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table


@Entity
@Table(name = "sprinkle")
data class Sprinkle(
        @Id
        @Column(nullable = false, length = 3)
        var token: String = "",

        @Column(nullable = false)
        var userId: Long = 0L,

        @Column(nullable = false, length = 40)
        var roomId: String = "",

        @Column(nullable = false)
        var totalAmount: Int = 0,

        @Column(nullable = false)
        @CreationTimestamp
        var createdAt: Timestamp = Timestamp(System.currentTimeMillis())
)
