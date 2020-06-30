//package lechuck.kakaopay.controller
//
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.extension.ExtendWith
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
//import org.springframework.http.MediaType
//import org.springframework.test.context.junit.jupiter.SpringExtension
//import org.springframework.test.context.web.WebAppConfiguration
//import org.springframework.test.web.servlet.MockMvc
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
//import org.springframework.test.web.servlet.setup.MockMvcBuilders
//import org.springframework.web.context.WebApplicationContext
//
//@ExtendWith(SpringExtension::class)
//@WebAppConfiguration
//@WebMvcTest
//class SprinkleControllerMockTest(private val wac: WebApplicationContext) {
//
//    private var mvc: MockMvc? = null
//
//    @BeforeEach
//    fun setup() {
//        this.mvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun getAllEmployeesAPI() {
//        mvc!!.perform(MockMvcRequestBuilders
//                .get("/sprinkle")
//                .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk)
//                .andExpect(MockMvcResultMatchers.jsonPath("$.data").exists())
//    }
//}