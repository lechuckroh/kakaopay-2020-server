package lechuck.kakaopay.entity

import org.hibernate.annotations.CreationTimestamp
import java.io.Serializable
import java.sql.Timestamp
import javax.persistence.*


@Entity
@Table(name = "room_user")
@IdClass(RoomUserPK::class)
data class RoomUser(
        @Id
        @Column(nullable = false)
        var userId: Long = 0L,

        @Id
        @Column(nullable = false, length = 40)
        var roomId: String = "",

        @Column(nullable = false)
        @CreationTimestamp
        var createdAt: Timestamp = Timestamp(System.currentTimeMillis())
)

data class RoomUserPK(
        var userId: Long = 0L,
        var roomId: String = ""
) : Serializable
