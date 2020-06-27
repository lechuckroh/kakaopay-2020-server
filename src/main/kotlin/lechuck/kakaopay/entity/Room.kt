package lechuck.kakaopay.entity

import javax.persistence.*


@Entity
@Table(name = "room")
data class Room(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        @Column(nullable = false, length = 40)
        var id: String = ""
)
