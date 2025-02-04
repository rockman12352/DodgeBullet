package com.csg.codeit.service

import com.csg.codeit.config.AppConfig
import com.csg.codeit.config.objectMapper
import com.csg.codeit.model.*
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.math.ceil

@Service
class CoordinatorService(
    val appConfig: AppConfig,
    val httpClient: OkHttpClient,
    val dodgeBulletService: DodgeBulletService
) {
    private val logger: Logger = LoggerFactory.getLogger(CoordinatorService::class.java)

    fun acceptRequest(evaluationRequest: EvaluationRequest) {
        val url = evaluationRequest.callbackUrl.toHttpUrl()
        try {
            dodgeBulletService.postChallenge(evaluationRequest.teamUrl, evaluationRequest.runId)
            val result = EvaluationResultRequest(evaluationRequest.runId, 100, "you pass!")
           postResult(url, result)
        } catch (exp : Exception){
            postResult(url, EvaluationResultRequest(evaluationRequest.runId, 0, exp.message ?: "error"))
        }
    }

    private fun postResult(url: HttpUrl, result: EvaluationResultRequest) {
        try {
            result.post(url) {
                it.addHeader("Authorization", appConfig.bearerToken)
            }?.also { logger.info("Notified coordinator with: $result") }
                ?: logger.warn("Error notifying coordinator with: $result")
        } catch (e: Exception) {
            logger.error("Error notifying coordinator with: $result\nException message: ${e.message}", e)
        }
    }

    private val AppConfig.bearerToken: String get() = "Bearer $coordinatorAuthToken"

    private val RequestPayload.toJson: String get() = objectMapper.writeValueAsString(this)

    private fun RequestPayload.post(url: HttpUrl, modifier: (Request.Builder) -> Request.Builder = { it }): Response? =
        Request.Builder().post(this.toJson.toRequestBody(MEDIA_TYPE_JSON))
            .let { httpClient.newCall(it.url(url).let(modifier).build()).execute() }
            .let { if (it.isSuccessful) it else it.close().run { null } }

    companion object {
        private val MEDIA_TYPE_JSON = "application/json".toMediaType()
    }
}
