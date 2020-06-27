package lechuck.kakaopay.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class SprinkleServiceTest {

    /**
     * 뿌리기 분배 성공 테스트
     */
    @ParameterizedTest
    @CsvSource(value = [
        "100,3,1",
        "10,10,1",
        "20,10,2",
        "1,1,1"
    ])
    fun testSplitSprinkle(sum: Int, count: Int, minValue: Int) {
        val list = SprinkleService.splitSprinkle(sum, count, minValue)
        assertEquals(sum, list.sum())
        assertEquals(count, list.size)
        assertTrue(list.all { it >= minValue })
    }

    /**
     * 뿌리기 분배 실패 테스트
     */
    @ParameterizedTest
    @CsvSource(value = [
        "10,11,1",  // 10 < 11 * 1
        "10,50,3",  // 10 < 50 * 3
        "0,2,1",    // sum = 0
        "10,0,1",   // count = 0
        "10,5,0"    // minValue = 0
    ])
    fun testSplitSprinkleFailure(sum: Int, count: Int, minValue: Int) {
        assertThrows(IllegalArgumentException::class.java) {
            SprinkleService.splitSprinkle(sum, count, minValue)
        }
    }
}