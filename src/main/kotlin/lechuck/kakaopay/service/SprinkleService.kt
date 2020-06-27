package lechuck.kakaopay.service

import lechuck.kakaopay.Log
import lechuck.kakaopay.entity.Sprinkle
import lechuck.kakaopay.entity.SprinkleItem
import lechuck.kakaopay.repos.RoomUserRepository
import lechuck.kakaopay.repos.SprinkleItemRepository
import lechuck.kakaopay.repos.SprinkleRepository
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.RandomUtils
import org.apache.commons.lang3.time.DateUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class SprinkleService(
        private val roomUserRepos: RoomUserRepository,
        private val sprinkleRepos: SprinkleRepository,
        private val sprinkleItemRepos: SprinkleItemRepository
) {
    companion object : Log() {
        const val receiveTimeout = DateUtils.MILLIS_PER_MINUTE * 10
        const val statusTimeout = DateUtils.MILLIS_PER_DAY * 7
        const val maxSprinkleRetryCount = 5

        /**
         * 뿌리기 토큰 생성.
         * @return 3자리 랜덤 문자열로 구성된 토큰
         */
        private fun createToken(): String {
            return RandomStringUtils.random(3, true, true)
        }

        /**
         * 뿌리기 분배
         * @param sum 뿌릴 금액
         * @param count 뿌릴 인원
         * @param minValue 건 당 최소 분배 금액
         * @throws IllegalArgumentException 주어진 조건으로 분배할 수 없는 경우
         */
        fun splitSprinkle(sum: Int, count: Int, minValue: Int): List<Int> {
            if (sum <= 0) {
                throw IllegalArgumentException("sum should be positive")
            }
            if (count <= 0) {
                throw IllegalArgumentException("count should be positive")
            }
            if (sum < count) {
                throw IllegalArgumentException("sum is less than count")
            }
            if (minValue <= 0) {
                throw IllegalArgumentException("minValue should be positive")
            }
            val maxValue = sum - count * minValue
            if (maxValue < 0) {
                throw IllegalArgumentException("cannot split. minValue is too high")
            }

            // count-1 만큼 랜덤 숫자를 가지는 리스트 생성
            val list = (1 until count).map {
                RandomUtils.nextInt(0, maxValue)
            }.toMutableList()

            // 리스트의 마지막에는 최대값을 추가한 다음, 리스트 정렬
            list.add(maxValue)
            list.sort()

            // 리스트의 n-1 ~ 1번째 인덱스까지 루프를 돌면서, A[n] = A[n] - A[n-1]
            (count - 1 downTo 1).forEach {
                list[it] -= list[it - 1]
            }

            // 리스트의 각 항목의 값을 최소값만큼 더한다.
            return list.map { it + minValue }
        }
    }

    /**
     * 뿌리기 요청
     * @param sum 뿌릴 금액
     * @param count 뿌릴 인원
     * @throws IllegalArgumentException 주어진 조건으로 분배할 수 없는 경우 발생
     */
    @Throws(java.lang.IllegalArgumentException::class)
    fun sprinkle(sum: Int, count: Int, userId: Long, roomId: String): String {
        // 뿌리기 생성
        // 토큰 중복 에러 발생시 최대 횟수까지 재시도
        var retryCount = 0
        var success = false
        var token: String = ""
        while (!success && retryCount < maxSprinkleRetryCount) {
            try {
                token = createToken()
                sprinkleRepos.save(Sprinkle(token, userId, roomId, sum))
                success = true
            } catch (e: Exception) {
                logger().warn("duplicated token: $token", e)
            }
            retryCount++
        }

        // 최대 시도 횟수 초과
        if (!success) {
            throw SprinkleFailedException("failed to create token. max retry limit exceeded.")
        }

        // 뿌리기 분배건 생성
        val minValue = 1
        val splitList = splitSprinkle(sum, count, minValue)
        val sprinkleItems = splitList.mapIndexed { idx, value ->
            SprinkleItem(0L, token, idx, value)
        }
        sprinkleItemRepos.saveAll(sprinkleItems)

        return token
    }

    /**
     * 할당되지 않은 분배건 받기
     * @param token 뿌리기 토큰
     * @param userId 받을 사용자 ID
     * @return 할당된 분배건의 금액
     * @throws IllegalArgumentException 잘못된 토큰 또는 받을 수 없는 사용자인 경우
     * @throws ReceiveFailedException 받을 수 없는 경우
     */
    @Throws(java.lang.IllegalArgumentException::class, ReceiveFailedException::class)
    fun receive(token: String, userId: Long): Int {
        // 토큰에 해당하는 뿌리기 조회
        val maybeSprinkle = sprinkleRepos.findById(token)
        if (maybeSprinkle.isEmpty) {
            throw IllegalArgumentException("invalid token: $token")
        }
        val sprinkle = maybeSprinkle.get()

        // 자신이 뿌린 경우
        if (sprinkle.userId == userId) {
            throw IllegalArgumentException("self receive is not allowed")
        }

        // 받기 유효시간 초과
        val elapsed = System.currentTimeMillis() - sprinkle.createdAt.time
        if (elapsed > receiveTimeout) {
            throw ReceiveFailedException("too late to receive")
        }

        // 대화방에 속하지 않은 사용자인 경우
        val roomId = sprinkle.roomId
        val roomUserList = roomUserRepos.findByRoomId(roomId)
        if (roomUserList.all { it.userId != userId }) {
            throw IllegalArgumentException("user is not in the room")
        }

        // 분배 가능 항목 조회 (자신이 받은 건도 같이 포함되어 있음)
        val availables = sprinkleItemRepos.findAvailableItemsWithReceived(token, userId).toMutableList()

        // 이미 받은 경우
        if (availables.any { it.receivedUserId == userId }) {
            throw ReceiveFailedException("already received")
        }

        // 분배 가능 항목 중 하나를 선택해서 받은 상태로 변경.
        // 다른 사용자에게 이미 분배된 경우 계속 재시도
        var count = availables.count()
        while (count > 0) {
            // 분배건이 이미 랜덤하게 저장되어 있지만, 분배할 항목의 인덱스를 다시 랜덤 생성
            // 가능 항목을 순서대로 할당하는 경우, 다른 서버에서 동시에 할당을 하면서 충돌이 발생할 수 있는 가능성이 높아짐
            val idx = RandomUtils.nextInt(0, count)
            val receiveItem = availables[idx]
            val success = sprinkleItemRepos.updateReceivedUserId(receiveItem.id, userId)
            if (success) {
                return receiveItem.amount
            }

            // 이미 다른 사람이 받은 경우, 해당 항목을 리스트에서 삭제
            availables.removeAt(idx)
            count = availables.count()
        }

        // 더 이상 받을 항목이 없는 경우
        throw ReceiveFailedException("nothing left to receive")
    }

    /**
     * 뿌리기 상태 조회
     * @param token 뿌리기 토큰
     * @param userId 뿌린 사용자 ID
     */
    @Throws(java.lang.IllegalArgumentException::class)
    fun getStatus(token: String, userId: Long): SprinkleStatusDTO {
        // 토큰에 해당하는 뿌리기 조회
        val maybeSprinkle = sprinkleRepos.findById(token)
        if (maybeSprinkle.isEmpty) {
            throw IllegalArgumentException("invalid token: $token")
        }
        val sprinkle = maybeSprinkle.get()

        // 뿌린 사용자가 아닌 경우
        if (sprinkle.userId != userId) {
            throw IllegalArgumentException("token is not created by given userId")
        }

        // 조회 유효시간 초과
        val elapsed = System.currentTimeMillis() - sprinkle.createdAt.time
        if (elapsed > statusTimeout) {
            throw IllegalArgumentException("too old to get status")
        }

        // 분배 완료된 항목 조회
        val itemList = sprinkleItemRepos.findReceivedItems(token)
        val receivedAmount = itemList.sumBy { it.amount }
        val receivedItems = itemList.map {
            ReceivedItem(it.receivedUserId!!, it.amount)
        }

        return SprinkleStatusDTO(
                sprinkle.createdAt,
                sprinkle.totalAmount,
                receivedAmount,
                receivedItems)
    }
}

/** 뿌리기 상태 조회 결과 */
data class SprinkleStatusDTO(
        val createdAt: Date,
        val totalAmount: Int,
        val receivedAmount: Int,
        val receivedList: List<ReceivedItem>
)

/** 뿌리기 받기 완료된 받은 사용자별 정보 */
data class ReceivedItem(val userId: Long, val amount: Int)
