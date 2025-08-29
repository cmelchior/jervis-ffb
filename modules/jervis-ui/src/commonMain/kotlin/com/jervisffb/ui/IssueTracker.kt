package com.jervisffb.ui

import com.jervisffb.BuildConfig
import com.jervisffb.utils.getBuildType
import com.jervisffb.utils.getPlatformDescription
import io.ktor.http.encodeURLParameter

/**
 * Wrapper around issue tracking. This class provides a convenient way to report issues to
 * report issues to GitHub.
 *
 * TODO Add a proxy in between the client and GitHub, most users will not have a GitHub account
 *  so they cannot create issues easily.
 */
object IssueTracker {
    const val NEW_ISSUE_BASE_URL: String = "https://github.com/cmelchior/jervis-ffb/issues/new"

    enum class Label(val description: String) {
        USER("user"),
        USER_CRASH("user+crash")
    }

    fun createIssueUrl(issueBody: String, label: Label): String {
        val body = buildString {
            append(issueBody)
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
        return "$NEW_ISSUE_BASE_URL?body=$body&label=${label.description}"
    }
}
