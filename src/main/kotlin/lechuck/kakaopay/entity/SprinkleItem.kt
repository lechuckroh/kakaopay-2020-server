package lechuck.kakaopay.entity

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.sql.Timestamp
import javax.persistence.*


@Entity
@Table(name = "sprinkle_item", uniqueConstraints = [
    UniqueConstraint(name = "sprinkle_item_uq", columnNames = ["token", "itemSeq"])
])
data class SprinkleItem(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        @Column(nullable = false)
        var id: Long = 0L,

        @Column(nullable = false, length = 3)
        var token: String = "",

        @Column(nullable = false)
        var itemSeq: Int = 0,

        @Column(nullable = false)
        var amount: Int = 0,

        var receivedUserId: Long? = null,

        @Column(nullable = false)
        @CreationTimestamp
        var createdAt: Timestamp = Timestamp(System.currentTimeMillis()),

        @UpdateTimestamp
        var updatedAt: Timestamp? = null
)
