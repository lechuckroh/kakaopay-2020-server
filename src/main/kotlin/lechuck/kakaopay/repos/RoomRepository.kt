package lechuck.kakaopay.repos

import lechuck.kakaopay.entity.Room
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoomRepository : JpaRepository<Room, String>
