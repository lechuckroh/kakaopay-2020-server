package lechuck.kakaopay.entity

import javax.persistence.*


@Entity
@Table(name = "user", uniqueConstraints = [
    UniqueConstraint(name = "user_uq", columnNames = ["loginId"])
])
data class User(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        @Column(nullable = false)
        var id: Long = 0L,

        @Column(nullable = false, length = 40)
        var loginId: String = ""
)
