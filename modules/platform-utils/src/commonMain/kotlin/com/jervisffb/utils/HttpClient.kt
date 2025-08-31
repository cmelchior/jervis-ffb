package com.jervisffb.utils

import io.ktor.client.HttpClient

/**
 * Returns a HTTP client for the current platform.
 *
 * Note, this http client is created once and shared between all resources,
 * so it should never be closed.
 */
expect fun getHttpClient(): HttpClient

