package lechuck.kakaopay

import lechuck.kakaopay.repos.RoomUserRepository
import lechuck.kakaopay.repos.SprinkleItemRepository
import lechuck.kakaopay.repos.SprinkleRepository
import org.mockito.Mockito
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Profile("test")
@Configuration
class TestConfiguration {
    @Bean
    @Primary
    fun roomUserRepository(): RoomUserRepository {
        return Mockito.mock(RoomUserRepository::class.java)
    }

    @Bean
    @Primary
    fun sprinkleRepository(): SprinkleRepository {
        return Mockito.mock(SprinkleRepository::class.java)
    }

    @Bean
    @Primary
    fun sprinkleItemRepository(): SprinkleItemRepository {
        return Mockito.mock(SprinkleItemRepository::class.java)
    }
}