package lechuck.kakaopay.repos

import lechuck.kakaopay.entity.Sprinkle
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SprinkleRepository : JpaRepository<Sprinkle, String>
