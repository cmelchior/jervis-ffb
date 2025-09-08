package com.jervisffb.ui

import com.jervisffb.BuildConfig
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.serialize.JervisSerialization
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.utils.getBuildType
import com.jervisffb.utils.getHttpClient
import com.jervisffb.utils.getPlatformDescription
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.core.toByteArray
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
private sealed interface CreateIssueResponse { val message: String }

@Serializable
@SerialName("success")
private data class CreateIssueSuccess(override val message: String) : CreateIssueResponse

@Serializable
@SerialName("error")
private data class CreateIssueError(override val message: String) : CreateIssueResponse


/**
 * Wrapper around issue tracking. This class provides a convenient way to report issues to
 * report issues to GitHub.
 *
 * TODO Add a proxy in between the client and GitHub, most users will not have a GitHub account
 *  so they cannot create issues easily.
 */
object IssueTracker {
    const val NEW_ISSUE_BASE_URL: String = "https://jervis.ilios.dk/create_issue.php"

    enum class Label(val description: String) {
        USER("user"),
        USER_CRASH("user+crash")
    }

    private val json = Json

    /**
     * Create a new issue on GitHub using the Jervis Support Bot proxy.
     * If successful, returns the URL to the newly created issue.
     */
    suspend fun createNewIssue(
        title: String,
        body: String,
        throwable: Throwable?,
        gameState: GameEngineController?
    ): Result<String> {
        try {
            val bodyWithStackTrace = buildString {
                append(body)
                if (throwable != null) {
                    if (!body.endsWith("\n")) {
                        appendLine()
                    }
                    appendLine()
                    appendLine("**Stacktrace**")
                    appendLine("```")
                    appendLine(throwable.stackTraceToString())
                    appendLine("```")
                }
                appendLine()
                appendLine()
                append("""
                    -----
                    **Client Information (${getBuildType()})**
                    Jervis Client Version: ${BuildConfig.releaseVersion}
                    Git Commit: ${BuildConfig.gitHash}
                """.trimIndent())
                appendLine()
                append(getPlatformDescription())
                appendLine("Screen Density: ${JervisTheme.screenDensity.density}")
                append("Window size: ${JervisTheme.windowSizePx.width.toInt()}x${JervisTheme.windowSizePx.height.toInt()}")
            }
            val client = getHttpClient()
            val httpResponse = client.submitFormWithBinaryData(
                url = NEW_ISSUE_BASE_URL,
                formData = formData {
                    append("title", title)
                    append("body", bodyWithStackTrace)
                    if (gameState != null) {
                        val serializedState = JervisSerialization.serializeGameStateToJson(gameState, true)
                        append("attachments[]", serializedState.toByteArray(), Headers.build {
                            append(HttpHeaders.ContentType, "application/json")
                            append(HttpHeaders.ContentDisposition, "filename=game_state.json")
                        })
                    }
                }
            )
            try {
                val result = json.decodeFromString<CreateIssueResponse>(httpResponse.body())
                return when (result) {
                    is CreateIssueError -> Result.failure(RuntimeException(result.message))
                    is CreateIssueSuccess -> Result.success(result.message)
                }
            } catch (_: Exception) {
                return Result.failure(
                    RuntimeException("Failed to create issue [${httpResponse.status}]: ${httpResponse.bodyAsText()}")
                )
            }
        } catch (ex: Throwable) {
            return Result.failure(ex)
        }
    }
}
