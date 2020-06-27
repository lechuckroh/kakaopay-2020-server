package lechuck.kakaopay

import com.fasterxml.jackson.databind.ObjectMapper
import lechuck.kakaopay.controller.JsonErrorResponse
import lechuck.kakaopay.controller.JsonResponse
import org.apache.commons.collections4.ListUtils
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

/**
 * MockMvc 를 사용하기 위한 기본 클래스
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [App::class],
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
abstract class SpringMockMvcTestSupport {
    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    /** 객체를 JSON 문자열로 변환 */
    protected fun json(value: Any): String {
        return objectMapper.writeValueAsString(value)
    }

    /** JSON 문자열을 cls 클래스로 변환 */
    protected fun <T> toObject(json: String, cls: Class<T>): T {
        return objectMapper.readValue(json, cls)
    }

    /** MockHttpServletResponse를 cls 클래스로 변환 */
    protected fun <T> toObject(response: MockHttpServletResponse, cls: Class<T>): T {
        return toObject(response.contentAsString, cls)
    }

    /** HTTP 요청 */
    private fun perform(method: (String, Array<Any>) -> MockHttpServletRequestBuilder,
                        url: String,
                        params: Map<String, String>?,
                        body: Any?,
                        headers: HttpHeaders?,
                        vararg uriVars: Any): ResultActions {
        var builder = method(url, arrayOf(*uriVars)).contentType(MediaType.APPLICATION_JSON)
        params?.forEach { (key, value) ->
            builder = builder.param(key, value)
        }
        if (body != null) {
            builder = builder.content(json(body))
        }
        if (headers != null) {
            builder = builder.headers(headers)
        }

        return mockMvc.perform(builder)
    }

    /** GET 요청 */
    protected fun performGet(url: String,
                             params: Map<String, String>? = null,
                             headers: HttpHeaders?): ResultActions {
        return perform(MockMvcRequestBuilders::get, url, params, null, headers)
    }

    /** POST 요청 */
    protected fun performPost(url: String,
                              body: Any?,
                              headers: HttpHeaders?): ResultActions {
        return perform(MockMvcRequestBuilders::post, url, null, body, headers)
    }

    /** MockHttpServletResponse의 data 필드 dataType으로 변환 */
    protected fun <T> getJsonResponseData(response: MockHttpServletResponse, dataType: Class<T>): T? {
        return toJsonResponse(response, dataType).data
    }

    /** JSON 문자열을 JsonResponse<T> 타입으로 변환 */
    protected fun <T> toJsonResponse(json: String, dataType: Class<T>): JsonResponse<T> {
        return objectMapper.readValue(json,
                objectMapper.typeFactory.constructParametricType(JsonResponse::class.java, dataType))
    }

    /** MockHttpServletResponse를 JsonResponse<T> 타입으로 변환 */
    protected fun <T> toJsonResponse(response: MockHttpServletResponse, dataType: Class<T>): JsonResponse<T> {
        return toJsonResponse(response.contentAsString, dataType)
    }

    /** MockHttpServletResponse를 JsonErrorResponse 타입으로 변환 */
    protected fun toJsonErrorResponse(response: MockHttpServletResponse): JsonErrorResponse {
        return toObject(response, JsonErrorResponse::class.java)
    }

    /** 순서에 상관없이 리스트 내용이 같은지 assert를 실행한다. */
    protected fun <T : Comparable<T>> assertListEquals(list1: List<T>?, list2: List<T>?) {
        assertTrue(ListUtils.isEqualList(list1?.sorted(), list2?.sorted()))
    }
}
