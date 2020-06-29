package lechuck.kakaopay.repos

import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

@Repository
@Transactional
class SprinkleItemCustomRepositoryImpl(
        private val em: EntityManager
) : SprinkleItemCustomRepository {

    override fun updateReceivedUserId(id: Long, userId: Long): Boolean {
        val jpql = """
            UPDATE SprinkleItem si 
            SET si.receivedUserId=:userId 
            WHERE si.id=:id AND si.receivedUserId is null
        """.trimIndent()
        val query = em.createQuery(jpql)
        query.setParameter("id", id)
        query.setParameter("userId", userId)
        val updateCount = query.executeUpdate()
        return updateCount > 0
    }
}
