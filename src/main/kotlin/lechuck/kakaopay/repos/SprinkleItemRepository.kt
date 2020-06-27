package lechuck.kakaopay.repos

import lechuck.kakaopay.entity.SprinkleItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface SprinkleItemRepository :
        JpaRepository<SprinkleItem, Long>,
        SprinkleItemCustomRepository {
    /**
     *  뿌리기 분배건을 조회한다.
     * @param token 뿌리기 토큰
     */
    @Query("SELECT si FROM SprinkleItem si WHERE si.token=:token")
    fun findByToken(@Param("token") token: String): List<SprinkleItem>

    /**
     * 받기 완료된 뿌리기 분배건을 조회한다.
     * @param token 뿌리기 토큰
     */
    @Query("""
            SELECT si FROM SprinkleItem si 
            WHERE si.token=:token AND si.receivedUserId is not null
        """)
    fun findReceivedItems(@Param("token") token: String): List<SprinkleItem>

    /**
     * 미할당 뿌리기 분배건을 조회한다. 해당 사용자가 이미 받은 분배건이 있는 경우 결과에 포함.
     * @param token 뿌리기 토큰
     * @param userId 사용자 ID
     */
    @Query("""
            SELECT si FROM SprinkleItem si 
            WHERE si.token=:token AND 
                  (si.receivedUserId is null OR si.receivedUserId=:userId)
    """)
    fun findAvailableItemsWithReceived(
            @Param("token") token: String,
            @Param("userId") userId: Long
    ): List<SprinkleItem>
}
