package lechuck.kakaopay.repos

import lechuck.kakaopay.entity.RoomUser
import lechuck.kakaopay.entity.RoomUserPK
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RoomUserRepository : JpaRepository<RoomUser, RoomUserPK> {

    @Query("SELECT ru FROM RoomUser ru WHERE ru.roomId=:roomId")
    fun findByRoomId(@Param("roomId") roomId: String): List<RoomUser>
}
