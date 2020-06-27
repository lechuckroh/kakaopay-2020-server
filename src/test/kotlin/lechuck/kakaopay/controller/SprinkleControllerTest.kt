package lechuck.kakaopay.controller

import lechuck.kakaopay.SpringMockMvcTestSupport
import lechuck.kakaopay.entity.RoomUser
import lechuck.kakaopay.entity.Sprinkle
import lechuck.kakaopay.entity.SprinkleItem
import lechuck.kakaopay.repos.RoomUserRepository
import lechuck.kakaopay.repos.SprinkleItemRepository
import lechuck.kakaopay.repos.SprinkleRepository
import lechuck.kakaopay.service.SprinkleStatusDTO
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import java.sql.Timestamp

class SprinkleControllerTest(
        val roomUserRepos: RoomUserRepository,
        val sprinkleRepos: SprinkleRepository,
        val sprinkleItemRepos: SprinkleItemRepository
) : SpringMockMvcTestSupport() {
    companion object {
        /**
         * userId, roomId를 포함하는 HttpHeaders 생성
         */
        private fun headers(userId: Long?, roomId: String? = null): HttpHeaders {
            val headers = HttpHeaders()
            if (userId != null) {
                headers[HEADER_USER_ID] = listOf(userId.toString())
            }
            if (roomId != null) {
                headers[HEADER_ROOM_ID] = listOf(roomId)
            }
            return headers
        }
    }

    @BeforeEach
    fun setup() {
        roomUserRepos.deleteAll()
        sprinkleRepos.deleteAll()
        sprinkleItemRepos.deleteAll()
    }

    /**
     * 뿌리기 테스트
     */
    @ParameterizedTest
    @CsvSource(value = [
        "true,100,12,3",
        "false,0,12,3",         // 뿌리기 금액이 0
        "false,100,0,3",        // 뿌리기 개수가 0
        "false,10,20,3"         // 뿌릴 금액 < 뿌릴 개수
    ])
    fun testSprinkle(success: Boolean, sum: Int, count: Int, userId: Long) {
        // 데이터 초기화
        val roomId = "room1"
        val userIdList = (1L..count).toList()
        val roomUserList = userIdList.map { RoomUser(it, roomId) }
        roomUserRepos.saveAll(roomUserList)

        // 뿌리기 요청
        val res = performPost(
                "/sprinkle",
                CreateBody(sum, count),
                headers(userId, roomId)
        ).andReturn().response

        if (success) {
            // 응답 확인
            assertEquals(HttpStatus.CREATED.value(), res.status)
            val token = getJsonResponseData(res, String::class.java)!!
            assertEquals(3, token.length)

            // 뿌리기 분배건 확인
            val sprinkleItems = sprinkleItemRepos.findByToken(token)
            assertEquals(count, sprinkleItems.size)
            assertEquals(sum, sprinkleItems.sumBy { it.amount })
        } else {
            // 응답 확인
            assertTrue(res.status >= 400)
            val errorResponse = toJsonErrorResponse(res)
            val error = errorResponse.errors?.first()
            assertEquals(res.status.toString(), error?.status)
        }
    }

    /**
     * 받기 성공 테스트
     */
    @Test
    fun testReceive() {
        // 데이터 초기화
        val roomId = "room1"
        val userIdList = (1L..5L).toList()
        val roomUserList = userIdList.map { RoomUser(it, roomId) }
        roomUserRepos.saveAll(roomUserList)

        val token = "123"
        val userId = 3L
        val sum = 100
        sprinkleRepos.save(Sprinkle(token, userId, roomId, sum))

        val item1 = SprinkleItem(0L, token, 0, 20)
        val item2 = SprinkleItem(0L, token, 1, 30)
        val item3 = SprinkleItem(0L, token, 2, 50)
        sprinkleItemRepos.saveAll(listOf(item1, item2, item3))

        // 받기 요청
        val receiveUserId = 4L
        val res = performPost(
                "/sprinkle/receive",
                ReceiveBody(token),
                headers(receiveUserId)
        ).andReturn().response
        val receivedAmount = getJsonResponseData(res, Int::class.java)!!

        // 응답 확인
        assertEquals(HttpStatus.ACCEPTED.value(), res.status)
        assertTrue(receivedAmount > 0)

        // 뿌리기 분배건의 사용자 업데이트 확인
        val dbSprinkleItems = sprinkleItemRepos.findByToken(token)
        val receivedItem = dbSprinkleItems.find { it.amount == receivedAmount }
        assertEquals(receiveUserId, receivedItem?.receivedUserId)
    }

    /**
     * 받기 실패 (잘못된 토큰)
     */
    @Test
    fun testReceiveInvalidToken() {
        // 데이터 초기화
        val roomId = "room1"
        val userIdList = (1L..5L).toList()
        val roomUserList = userIdList.map { RoomUser(it, roomId) }
        roomUserRepos.saveAll(roomUserList)

        val token = "123"
        val userId = 3L
        val sum = 100
        sprinkleRepos.save(Sprinkle(token, userId, roomId, sum))

        val item1 = SprinkleItem(0L, token, 0, 20)
        val item2 = SprinkleItem(0L, token, 1, 30)
        val item3 = SprinkleItem(0L, token, 2, 50)
        sprinkleItemRepos.saveAll(listOf(item1, item2, item3))

        // 받기 요청
        val receiveUserId = 4L
        val invalidToken = "XXX"        // 존재하지 않는 토큰
        val res = performPost(
                "/sprinkle/receive",
                ReceiveBody(invalidToken),
                headers(receiveUserId)
        ).andReturn().response

        // 응답 확인
        assertEquals(HttpStatus.BAD_REQUEST.value(), res.status)
    }

    /**
     * 받기 실패 (자신이 뿌린 토큰)
     */
    @Test
    fun testReceiveSelf() {
        // 데이터 초기화
        val roomId = "room1"
        val userIdList = (1L..5L).toList()
        val roomUserList = userIdList.map { RoomUser(it, roomId) }
        roomUserRepos.saveAll(roomUserList)

        val token = "123"
        val userId = 3L
        val sum = 100
        sprinkleRepos.save(Sprinkle(token, userId, roomId, sum))

        val item1 = SprinkleItem(0L, token, 0, 20)
        val item2 = SprinkleItem(0L, token, 1, 30)
        val item3 = SprinkleItem(0L, token, 2, 50)
        sprinkleItemRepos.saveAll(listOf(item1, item2, item3))

        // 받기 요청
        val res = performPost(
                "/sprinkle/receive",
                ReceiveBody(token),
                headers(userId)
        ).andReturn().response

        // 응답 확인
        assertEquals(HttpStatus.BAD_REQUEST.value(), res.status)
    }

    /**
     * 받기 실패 (받기 유효시간 초과)
     */
    @Test
    fun testReceiveTimeout() {
        // 데이터 초기화
        val roomId = "room1"
        val userIdList = (1L..5L).toList()
        val roomUserList = userIdList.map { RoomUser(it, roomId) }
        roomUserRepos.saveAll(roomUserList)

        val token = "123"
        val userId = 3L
        val sum = 100
        val sprinkle = sprinkleRepos.save(Sprinkle(token, userId, roomId, sum))
        sprinkle.createdAt = Timestamp.valueOf("2000-01-01 00:00:00")       // 뿌리기 생성 시각을 2000-01-01로 변경
        sprinkleRepos.save(sprinkle)

        val item1 = SprinkleItem(0L, token, 0, 20)
        val item2 = SprinkleItem(0L, token, 1, 30)
        val item3 = SprinkleItem(0L, token, 2, 50)
        sprinkleItemRepos.saveAll(listOf(item1, item2, item3))

        // 받기 요청
        val receiveUserId = 4L
        val res = performPost(
                "/sprinkle/receive",
                ReceiveBody(token),
                headers(receiveUserId)
        ).andReturn().response

        // 응답 확인
        assertEquals(HttpStatus.BAD_REQUEST.value(), res.status)
    }

    /**
     * 받기 실패 (대화방에 속하지 않은 사용자)
     */
    @Test
    fun testReceiveNotInRoom() {
        // 데이터 초기화
        val roomId = "room1"
        val userIdList = (1L..5L).toList()
        val roomUserList = userIdList.map { RoomUser(it, roomId) }
        roomUserRepos.saveAll(roomUserList)

        val token = "123"
        val userId = 3L
        val sum = 100
        sprinkleRepos.save(Sprinkle(token, userId, roomId, sum))

        val item1 = SprinkleItem(0L, token, 0, 20)
        val item2 = SprinkleItem(0L, token, 1, 30)
        val item3 = SprinkleItem(0L, token, 2, 50)
        sprinkleItemRepos.saveAll(listOf(item1, item2, item3))

        // 받기 요청
        val receiveUserId = 9999L       // 대화방에 속하지 않은 사용자 ID
        val res = performPost(
                "/sprinkle/receive",
                ReceiveBody(token),
                headers(receiveUserId)
        ).andReturn().response

        // 응답 확인
        assertEquals(HttpStatus.BAD_REQUEST.value(), res.status)
    }

    /**
     * 받기 실패 (더 이상 받을 수 있는 항목이 없음)
     */
    @Test
    fun testReceiveNothingLeft() {
        // 데이터 초기화
        val roomId = "room1"
        val userIdList = (1L..5L).toList()
        val roomUserList = userIdList.map { RoomUser(it, roomId) }
        roomUserRepos.saveAll(roomUserList)

        val token = "123"
        val userId = 3L
        val sum = 100
        sprinkleRepos.save(Sprinkle(token, userId, roomId, sum))

        val item1 = SprinkleItem(0L, token, 0, 20, 1L)
        val item2 = SprinkleItem(0L, token, 1, 30, 2L)
        val item3 = SprinkleItem(0L, token, 2, 50, 4L)
        sprinkleItemRepos.saveAll(listOf(item1, item2, item3))

        // 받기 요청
        val receiveUserId = 5L
        val res = performPost(
                "/sprinkle/receive",
                ReceiveBody(token),
                headers(receiveUserId)
        ).andReturn().response

        // 응답 확인
        assertEquals(HttpStatus.BAD_REQUEST.value(), res.status)
    }

    /**
     * 받기 실패 (이미 받은 경우)
     */
    @Test
    fun testReceiveAlready() {
        // 데이터 초기화
        val roomId = "room1"
        val userIdList = (1L..5L).toList()
        val roomUserList = userIdList.map { RoomUser(it, roomId) }
        roomUserRepos.saveAll(roomUserList)

        val token = "123"
        val userId = 3L
        val sum = 100
        sprinkleRepos.save(Sprinkle(token, userId, roomId, sum))

        val receiveUserId = 1L
        val item1 = SprinkleItem(0L, token, 0, 20)
        val item2 = SprinkleItem(0L, token, 1, 30, receiveUserId)       // 받은 항목
        val item3 = SprinkleItem(0L, token, 2, 50)
        sprinkleItemRepos.saveAll(listOf(item1, item2, item3))

        // 받기 요청
        val res = performPost(
                "/sprinkle/receive",
                ReceiveBody(token),
                headers(receiveUserId)
        ).andReturn().response

        // 응답 확인
        assertEquals(HttpStatus.BAD_REQUEST.value(), res.status)
    }

    /**
     * 뿌리기 상태 조회 성공 테스트
     */
    @Test
    fun testGetStatus() {
        // 데이터 초기화
        val roomId = "room1"
        val userIdList = (1L..5L).toList()
        val roomUserList = userIdList.map { RoomUser(it, roomId) }
        roomUserRepos.saveAll(roomUserList)

        val token = "123"
        val userId = 3L
        val sum = 100
        sprinkleRepos.save(Sprinkle(token, userId, roomId, sum))

        val item1 = SprinkleItem(0L, token, 0, 20)
        val item2 = SprinkleItem(0L, token, 1, 30, 1L)
        val item3 = SprinkleItem(0L, token, 2, 50, 2L)
        sprinkleItemRepos.saveAll(listOf(item1, item2, item3))
        val receivedItems = listOf(item2, item3)

        // 상태 조회 요청
        val res = performGet(
                "/sprinkle",
                mapOf("token" to token),
                headers(userId)
        ).andReturn().response
        val statusDTO = getJsonResponseData(res, SprinkleStatusDTO::class.java)!!

        // 응답 확인
        assertEquals(HttpStatus.OK.value(), res.status)
        assertNotNull(statusDTO.createdAt)
        assertEquals(sum, statusDTO.totalAmount)
        assertEquals(receivedItems.sumBy { it.amount }, statusDTO.receivedAmount)
        assertListEquals(receivedItems.map { it.receivedUserId!! }, statusDTO.receivedList.map { it.userId })
    }

    /**
     * 뿌리기 상태 조회 실패 (뿌린 사용자가 아닌 경우)
     */
    @Test
    fun testGetStatusNotOwner() {
        // 데이터 초기화
        val roomId = "room1"
        val userIdList = (1L..5L).toList()
        val roomUserList = userIdList.map { RoomUser(it, roomId) }
        roomUserRepos.saveAll(roomUserList)

        val token = "123"
        val userId = 3L
        val sum = 100
        sprinkleRepos.save(Sprinkle(token, userId, roomId, sum))

        val item1 = SprinkleItem(0L, token, 0, 20)
        val item2 = SprinkleItem(0L, token, 1, 30, 1L)
        val item3 = SprinkleItem(0L, token, 2, 50, 2L)
        sprinkleItemRepos.saveAll(listOf(item1, item2, item3))

        // 상태 조회 요청
        val requestUserId = 9999L       // 뿌린 사용자와 다른 사용자 ID
        val res = performGet(
                "/sprinkle",
                mapOf("token" to token),
                headers(requestUserId)
        ).andReturn().response

        // 응답 확인
        assertEquals(HttpStatus.BAD_REQUEST.value(), res.status)
    }

    /**
     * 뿌리기 상태 조회 실패 (조회 유효시간 초과)
     */
    @Test
    fun testGetStatusTimeout() {
        // 데이터 초기화
        val roomId = "room1"
        val userIdList = (1L..5L).toList()
        val roomUserList = userIdList.map { RoomUser(it, roomId) }
        roomUserRepos.saveAll(roomUserList)

        val token = "123"
        val userId = 3L
        val sum = 100
        val sprinkle = sprinkleRepos.save(Sprinkle(token, userId, roomId, sum))
        sprinkle.createdAt = Timestamp.valueOf("2000-01-01 00:00:00")       // 뿌리기 생성 시각을 2000-01-01로 변경
        sprinkleRepos.save(sprinkle)

        val item1 = SprinkleItem(0L, token, 0, 20)
        val item2 = SprinkleItem(0L, token, 1, 30, 1L)
        val item3 = SprinkleItem(0L, token, 2, 50, 2L)
        sprinkleItemRepos.saveAll(listOf(item1, item2, item3))

        // 상태 조회 요청
        val res = performGet(
                "/sprinkle",
                mapOf("token" to token),
                headers(userId)
        ).andReturn().response

        // 응답 확인
        assertEquals(HttpStatus.BAD_REQUEST.value(), res.status)
    }
}