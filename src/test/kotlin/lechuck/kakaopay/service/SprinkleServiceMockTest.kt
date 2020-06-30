package lechuck.kakaopay.service

import com.nhaarman.mockitokotlin2.whenever
import lechuck.kakaopay.entity.Sprinkle
import lechuck.kakaopay.repos.RoomUserRepository
import lechuck.kakaopay.repos.SprinkleItemRepository
import lechuck.kakaopay.repos.SprinkleRepository
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class SprinkleServiceMockTest {
    @Mock
    private lateinit var roomUserRepos: RoomUserRepository

    @Mock
    private lateinit var sprinkleRepos: SprinkleRepository

    @Mock
    private lateinit var sprinkleItemRepos: SprinkleItemRepository

    private lateinit var service: SprinkleService

    @BeforeEach
    fun setup() {
        this.service = SprinkleService(roomUserRepos, sprinkleRepos, sprinkleItemRepos)
    }

    @Test
    @DisplayName("토큰 중복 생성 테스트")
    fun testSprinkleDuplicatedToken() {
        whenever(sprinkleRepos.save(Mockito.any(Sprinkle::class.java)))
                .thenThrow(RuntimeException("duplicated record"))
                .thenThrow(RuntimeException("duplicated record"))
        whenever(sprinkleItemRepos.saveAll(Mockito.anyIterable()))

        val sum = 100
        val count = 3
        val userId = 1L
        val roomId = "ABC"
        val maxRetryCount = 3
        val token = service.sprinkle(sum, count, userId, roomId, maxRetryCount)
        assertNotNull(token)
    }
}
