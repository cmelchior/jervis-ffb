package com.jervisffb.ui

import com.jervisffb.BuildConfig
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.serialize.JervisSerialization
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.utils.PROP_UNCAUGHT_ERROR_MESSAGE
import com.jervisffb.utils.PROP_UNCAUGHT_ERROR_STACKTRACE
import com.jervisffb.utils.PROP_UNCAUGHT_ERROR_TITLE
import com.jervisffb.utils.getBuildType
import com.jervisffb.utils.getHttpClient
import com.jervisffb.utils.getPlatformDescription
import com.jervisffb.utils.jervisLogger
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
 */
object IssueTracker {
    const val NEW_ISSUE_BASE_URL: String = "https://jervis.ilios.dk/create_issue.php"

    private val LOG = jervisLogger()
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

    /**
     * Save an uncaught exception so it can be reported later.
     * This method must never throw.
     */
    fun saveUncaughtException(throwable: Throwable) {
        try {
            val title = "Uncaught application error: ${throwable::class.simpleName}"
            val body = throwable.message ?: "No message provided"
            val stackTrace = throwable.stackTraceToString()
            with(SETTINGS_MANAGER) {
                put(PROP_UNCAUGHT_ERROR_TITLE, title)
                put(PROP_UNCAUGHT_ERROR_MESSAGE, body)
                put(PROP_UNCAUGHT_ERROR_STACKTRACE, stackTrace)
            }
            LOG.e { throwable.stackTraceToString() }
        } catch (ex: Throwable) {
            // Ignore. This method is only called in case of catastrophic
            // failures and should not add to it.
            LOG.e { "Failed to save uncaught exception: $ex" }
            LOG.e { "Failed to handle application error: $throwable" }
        }
    }

    /**
     * Clear any saved uncaught exceptions.
     */
    fun clearUncaughtException() {
        try {
            with(SETTINGS_MANAGER) {
                put(PROP_UNCAUGHT_ERROR_TITLE, null)
                put(PROP_UNCAUGHT_ERROR_MESSAGE, null)
                put(PROP_UNCAUGHT_ERROR_STACKTRACE, null)
            }
        } catch (_: Throwable) {
            // Ignore. This method is only called in case of catastrophic
            // failures and should not add to it.
        }
    }
}
