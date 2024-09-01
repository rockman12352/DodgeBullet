package com.csg.codeit.service

import com.csg.codeit.config.AppConfig
import com.csg.codeit.config.objectMapper
import com.csg.codeit.model.*
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import kotlin.test.assertNotEquals

internal class CoordinatorServiceTest {

    private val appConfig = AppConfig(endpointSuffix = "test", coordinatorAuthToken = "COORDINATOR_AUTH_TOKEN")

    private lateinit var mockWebServer: MockWebServer

    private lateinit var coordinatorService: CoordinatorService

    private lateinit var dodgeBulletService: DodgeBulletService

    private lateinit var resultMap: Map<String, String>

    @BeforeEach
    fun setUp() {
        mockWebServer = MockWebServer().also { it.start() }
        coordinatorService = CoordinatorService(appConfig, appConfig.httpClient(), DodgeBulletService(appConfig.httpClient()))
        dodgeBulletService = DodgeBulletService(mock(OkHttpClient::class.java))
        val result = listOf(
                "{\"instructions\": [\"d\", \"l\"]}",
                "{\"instructions\":null}",
                "{\"instructions\":[\"l\", \"r\",\"l\", \"r\",\"l\", \"r\",\"l\", \"r\"]}",
                "{\"instructions\":[]}",
                "{\"instructions\":[]}",
                "{\"instructions\":[\"u\",\"u\",\"u\",\"r\",\"r\",\"r\",\"r\",\"r\",\"r\",\"r\",\"r\",\"d\",\"d\",\"d\",\"d\",\"l\",\"l\",\"l\",\"l\",\"l\",\"l\",\"l\",\"l\"]}",
                "{\"instructions\":null}",
                "{\"instructions\":[\"r\",\"l\", \"r\",\"l\", \"r\",\"l\", \"r\", \"r\", \"r\", \"r\", \"r\"]}"
        )

        resultMap = dodgeBulletService.getLevels().zip(result)
                .associate { p -> p.first.map.joinToString("\n") { it.joinToString("") } to p.second }
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `can success`() {
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = when (request.path) {
                "/${appConfig.endpointSuffix}" -> MockResponse()
                        .setResponseCode(200)
                        .setBody(objectMapper.writeValueAsString(TestResponse(50)))

                "/evaluate" -> MockResponse().setResponseCode(204).also {
                    assertThat(
                            objectMapper.readValue(request.body.readUtf8(), EvaluationResultRequest::class.java)
                    ).isEqualTo(EvaluationResultRequest("runId", 100, "you pass!"))
                }

                "/dodge" -> MockResponse()
                        .setResponseCode(200)
                        .setBody(resultMap[String(request.body.readByteArray())]!!)

                else -> MockResponse().setResponseCode(500)
            }
        }
        coordinatorService.acceptRequest(
                EvaluationRequest(
                        runId = "runId",
                        teamUrl = mockWebServer.url("").toString(),
                        callbackUrl = mockWebServer.url("evaluate").toString()
                )
        )
    }

    @Test
    fun `can failed`() {
        var count = 0
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = when (request.path) {
                "/${appConfig.endpointSuffix}" -> MockResponse()
                        .setResponseCode(200)
                        .setBody(objectMapper.writeValueAsString(TestResponse(50)))

                "/evaluate" -> MockResponse().setResponseCode(204).also {
                    assertThat(
                            objectMapper.readValue(request.body.readUtf8(), EvaluationResultRequest::class.java)
                    ).isEqualTo(EvaluationResultRequest("runId", 0, "failed to dodge the bullet"))
                }

                "/dodge" -> {
                    count++
                    println(String(request.body.readByteArray()))
                    println("gapping")
                    MockResponse()
                            .setResponseCode(200)
                            .setBody("{\"instructions\": [\"d\", \"l\"]}")
                }

                else -> MockResponse().setResponseCode(500)
            }
        }
        coordinatorService.acceptRequest(
                EvaluationRequest(
                        runId = "runId",
                        teamUrl = mockWebServer.url("").toString(),
                        callbackUrl = mockWebServer.url("evaluate").toString()
                )
        )
        assert(count == 3)
    }

    @Test
    fun `result is random`() {
        val mutableList1 = mutableListOf<String>()
        val mutableList2 = mutableListOf<String>()
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = when (request.path) {
                "/${appConfig.endpointSuffix}" -> MockResponse()
                        .setResponseCode(200)
                        .setBody(objectMapper.writeValueAsString(TestResponse(50)))

                "/evaluate" -> MockResponse().setResponseCode(204)

                "/dodge" -> {
                    val level = String(request.body.readByteArray())
                    if(request.headers["runId"]=="runId"){
                        mutableList1.add(level)
                    }else{
                        mutableList2.add(level)
                    }
                    MockResponse()
                            .setResponseCode(200)
                            .setBody(resultMap[level]!!)
                }

                else -> MockResponse().setResponseCode(500)
            }
        }
        coordinatorService.acceptRequest(
                EvaluationRequest(
                        runId = "runId",
                        teamUrl = mockWebServer.url("").toString(),
                        callbackUrl = mockWebServer.url("evaluate").toString()
                )
        )
        coordinatorService.acceptRequest(
                EvaluationRequest(
                        runId = "runId2",
                        teamUrl = mockWebServer.url("").toString(),
                        callbackUrl = mockWebServer.url("evaluate").toString()
                )
        )
        assertNotEquals(mutableList1, mutableList2)
    }


}