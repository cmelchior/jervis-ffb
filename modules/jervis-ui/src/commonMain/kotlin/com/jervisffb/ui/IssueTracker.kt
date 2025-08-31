package com.jervisffb.ui

import com.jervisffb.BuildConfig
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.serialize.JervisSerialization
import com.jervisffb.utils.getBuildType
import com.jervisffb.utils.getHttpClient
import com.jervisffb.utils.getPlatformDescription
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.encodeURLParameter
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

    fun createIssueUrlFromException(title: String, body: String, error: Throwable): String {
        val body = buildString {
            if (body.isNotBlank()) {
                append(body)
                if (!body.endsWith("\n")) {
                    appendLine()
                }
                appendLine()
            }
            appendLine("-----")
            appendLine()
            appendLine("```")
            append(error.stackTraceToString().substring(0, 1500))
            if (!this.endsWith("\n")) {
                appendLine()
            }
            appendLine("```")
        }
        return createIssueUrl(title, body, Label.USER_CRASH)
    }

    fun createIssueUrl(title: String?, body: String, label: Label): String {
        val urlBody = buildString {
            append(body)
            appendLine()
            append("""
                
                -----
                **Client Information (${getBuildType()})**
                Jervis Client Version: ${BuildConfig.releaseVersion}
                Git Commit: ${BuildConfig.gitHash}
            """.trimIndent())
            appendLine()
            append(getPlatformDescription())
        }.encodeURLParameter()

        return buildString{
            append(NEW_ISSUE_BASE_URL)
            if (title != null) {
                append("?title=${title.encodeURLParameter()}&")
            } else {
                append("?")
            }
            append("body=$urlBody")
            append("&label=${label.description}")
        }
    }

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
            return when (val result = json.decodeFromString<CreateIssueResponse>(httpResponse.body())) {
                is CreateIssueError -> Result.failure(RuntimeException(result.message))
                is CreateIssueSuccess -> Result.success(result.message)
            }
        } catch (ex: Exception) {
            return Result.failure(ex)
        }
    }
}
