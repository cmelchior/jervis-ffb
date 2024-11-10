package com.jervisffb.fumbbl.net.api.auth

import com.jervisffb.utils.getHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import io.ktor.http.parameters

data class TeamData(val id: Int, val name: String)

/**
 * Class controlling login into FUMBBL in order to create auth tokens for the websocket connection.
 *
 * This is done by faking a manual login flow as like a website user, and is not the recommended way
 * of doing it: https://fumbbl.com/index.php?name=PNphpBB2&file=viewtopic&t=25698&postdays=0&postorder=asc&start=0
 *
 *
 */
class FumbblAuth {
    private val client: HttpClient = getHttpClient()
    private var authSessionId: String? = null

    private suspend fun getAnonymousSessionId(): String {
        val response: HttpResponse =
            client.request("https://fumbbl.com/p/login") {
                method = HttpMethod.Get
            }
        if (!response.status.isSuccess()) {
            throw IllegalStateException("Could not access FUMBBL: ${response.status}")
        }
        return readSessionCookie(response)
    }

    private fun readSessionCookie(response: HttpResponse): String {
        return response.headers["Set-Cookie"]?.let { cookies ->
            cookies.split("; ")
                .firstOrNull { cookie -> cookie.startsWith("POSTNUKESID") }
                ?.split("=")
                ?.get(1) ?: throw IllegalStateException("Could not find session id in headers: ${response.headers}")
        } ?: throw IllegalStateException("Could not find session id in headers: ${response.headers}")
    }

    suspend fun login(
        username: String,
        password: String,
    ) {
        val anonymousSessionid = getAnonymousSessionId()
        println("Anonymous session id: $anonymousSessionid")
        val response =
            client.request("https://fumbbl.com/user.php") {
                method = HttpMethod.Post
                parameters {
                    append("rememberme", "true")
                    append("module", "NS-User")
                    append("op", "login")
                    append("url", "%2Fuser.php")
                    append("user", username)
                    append("pass", password)
                    append("hash", anonymousSessionid)
                }
            }
        if (!response.status.isSuccess()) {
            throw IllegalStateException("Login failed:: ${response.status}")
        }
        authSessionId = readSessionCookie(response)
        println("Logged in session id: $authSessionId")
    }

    suspend fun getAvailableTeams(): List<TeamData> {
        TODO()
    }

    fun close() {
        client.close()
    }
}
