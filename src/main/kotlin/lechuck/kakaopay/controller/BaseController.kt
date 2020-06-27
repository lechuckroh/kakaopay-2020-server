package lechuck.kakaopay.controller

import lechuck.kakaopay.Log
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.*
import javax.servlet.http.HttpServletRequest

abstract class BaseController {
    companion object : Log() {
        /** BadRequest 응답을 반환하기 위한 ResponseStatusException을 생성한다. */
        fun badRequestException(message: String?): ResponseStatusException {
            return ResponseStatusException(HttpStatus.BAD_REQUEST, message)
        }

        /** Unauthorized 응답을 반환하기 위한 ResponseStatusException을 생성한다. */
        fun unauthorizedException(message: String?): ResponseStatusException {
            return ResponseStatusException(HttpStatus.UNAUTHORIZED, message)
        }

        fun getHeader(req: HttpServletRequest, name: String): Optional<String> {
            val e = req.getHeaders(name)
            return if (e.hasMoreElements()) {
                Optional.of(e.nextElement())
            } else {
                Optional.empty()
            }
        }

        /** 헤더에 설정된 사용자 ID를 반환한다. */
        fun getUserId(req: HttpServletRequest): Optional<Long> {
            return getHeader(req, HEADER_USER_ID).map { it.toLong() }
        }

        /** 헤더에 설정된 사용자 ID를 반환한다. 미지정시 BadRequest 상태 코드 반환 */
        fun getUserIdOrError(req: HttpServletRequest): Long {
            val maybeUserId = getUserId(req)
            if (maybeUserId.isEmpty) {
                throw unauthorizedException("userId is not set")
            }
            return maybeUserId.get()
        }

        /** 헤더에 설정된 대화방 식별값을 반환한다 */
        fun getRoomId(req: HttpServletRequest): Optional<String> {
            return getHeader(req, HEADER_ROOM_ID)
        }

        /** 대화방 식별값을 반환한다. 미지정시 BadRequest 상태 코드 반환 */
        fun getRoomIdOrError(req: HttpServletRequest): String {
            val maybeRoomId = getRoomId(req)
            if (maybeRoomId.isEmpty) {
                throw badRequestException("roomId is not set")
            }
            return maybeRoomId.get()
        }
    }
}
