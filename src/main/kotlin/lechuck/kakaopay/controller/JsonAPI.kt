package lechuck.kakaopay.controller

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity


class ErrorLink(val about: String,
                val type: String)

class ErrorSource(val pointer: String,
                  val parameter: String)

@JsonInclude(JsonInclude.Include.NON_NULL)
class Error(
        val id: String? = null,
        val links: ErrorLink? = null,
        val status: String? = null,
        val code: String? = null,
        val title: String? = null,
        val detail: String? = null,
        val source: ErrorSource? = null) {
    constructor(status: String?,
                code: String?,
                title: String?,
                detail: String?) :
            this(null, null, status, code, title, detail, null)

    constructor(status: Int?,
                code: String?,
                title: String?,
                detail: String?) :
            this(null, null, status?.toString(), code, title, detail, null)
}

@JsonInclude(JsonInclude.Include.NON_NULL)
class JsonErrorResponse(val errors: List<Error>?,
                        val meta: Map<String, Any>?) {
    constructor(error: Error) : this(listOf(error), null)

    constructor(errors: List<Error>) : this(errors, null)
}

/**
 * JSON:API 스펙에 따르는 응답을 생성하기 위한 클래스
 * @see <a href="https://jsonapi.org/">JSON:API</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class JsonResponse<T>(val data: T?) {
    companion object {
        /** ResponseEntity 인스턴스 생성 */
        fun <T> entity(status: HttpStatus, body: T?): ResponseEntity<JsonResponse<T>> {
            return ResponseEntity.status(status).body(JsonResponse(body))
        }

        /** ResponseEntity 인스턴스 생성 */
        fun <T> entity(body: T?): ResponseEntity<JsonResponse<T>> {
            return entity(HttpStatus.OK, body)
        }
    }
}
