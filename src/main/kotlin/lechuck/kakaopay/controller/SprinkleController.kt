package lechuck.kakaopay.controller

import lechuck.kakaopay.service.ReceiveFailedException
import lechuck.kakaopay.service.SprinkleService
import lechuck.kakaopay.service.SprinkleStatusDTO
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("sprinkle")
class SprinkleController(private val service: SprinkleService) : BaseController() {

    /**
     * 뿌리기 요청
     */
    @PostMapping
    fun sprinkle(req: HttpServletRequest,
                 @RequestBody body: CreateBody): ResponseEntity<JsonResponse<String>> {
        val userId = getUserIdOrError(req)
        val roomId = getRoomIdOrError(req)
        if (body.sum <= 0) {
            throw badRequestException("sum should be positive")
        }
        if (body.count <= 0) {
            throw badRequestException("count should be positive")
        }
        if (body.sum < body.count) {
            throw badRequestException("sum should be equal or greater than count")
        }

        try {
            val token = service.sprinkle(body.sum, body.count, userId, roomId)
            return JsonResponse.entity(HttpStatus.CREATED, token)
        } catch (e: IllegalArgumentException) {
            throw badRequestException(e.message)
        }
    }

    /**
     * 받기
     */
    @PostMapping("receive")
    fun receive(req: HttpServletRequest,
                @RequestBody body: ReceiveBody
    ): ResponseEntity<JsonResponse<Int>> {
        val userId = getUserIdOrError(req)

        try {
            val receivedAmount = service.receive(body.token, userId)
            return JsonResponse.entity(HttpStatus.ACCEPTED, receivedAmount)
        } catch (e: IllegalArgumentException) {
            throw badRequestException(e.message)
        } catch (e: ReceiveFailedException) {
            throw badRequestException(e.message)
        }
    }

    /**
     * 뿌리기 상태 조회
     */
    @GetMapping
    fun getStatus(req: HttpServletRequest,
                  @RequestParam(value = "token") token: String
    ): ResponseEntity<JsonResponse<SprinkleStatusDTO>> {
        val userId = getUserIdOrError(req)

        try {
            val status = service.getStatus(token, userId)
            return JsonResponse.entity(status)
        } catch (e: IllegalArgumentException) {
            throw badRequestException(e.message)
        }
    }
}

/** 뿌리기 요청에 사용되는 Body */
data class CreateBody(val sum: Int, val count: Int)

/** 받기 요청에 사용되는 Body */
data class ReceiveBody(val token: String)
