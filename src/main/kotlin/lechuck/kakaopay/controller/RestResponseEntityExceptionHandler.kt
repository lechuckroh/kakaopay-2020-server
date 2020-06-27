package lechuck.kakaopay.controller

import org.apache.commons.lang3.exception.ExceptionUtils
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

/**
 * Controller에서 발생한 Exception 핸들링을 위한 클래스.
 *
 * Exception 종류에 따라 알맞은 HttpStatus 코드와 함께 JSON:API 형식으로 응답을 반환한다.
 */
@ControllerAdvice
class RestResponseEntityExceptionHandler : ResponseEntityExceptionHandler() {

    companion object {
        fun extractExceptionTitle(message: String): String? {
            val tokens = message.split("; nested exception is ".toRegex(), 2)
            return if (tokens.size == 2) tokens[0] else null
        }
    }

    @ExceptionHandler(value = [ResponseStatusException::class])
    fun handleResponseStatusException(e: ResponseStatusException): ResponseEntity<JsonErrorResponse> {
        if (logger.isInfoEnabled) {
            logger.info("", e)
        }
        val title = extractExceptionTitle(e.message)
        val error = Error(e.status.value(), e.status.name, title, e.message)
        return ResponseEntity.status(e.status).body(JsonErrorResponse(error))
    }

    @ExceptionHandler(value = [Exception::class])
    fun handleException(e: RuntimeException, request: WebRequest): ResponseEntity<JsonErrorResponse> {
        if (logger.isErrorEnabled) {
            logger.error("", e)
        }
        val rootCause = ExceptionUtils.getRootCause(e)
        val status = HttpStatus.INTERNAL_SERVER_ERROR
        val error = Error(
                status.value(),
                rootCause.javaClass.simpleName,
                rootCause.javaClass.simpleName,
                rootCause.message)
        return ResponseEntity.status(status).body(JsonErrorResponse(error))
    }
}
