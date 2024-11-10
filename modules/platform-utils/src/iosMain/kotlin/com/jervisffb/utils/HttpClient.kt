package com.jervisffb.utils

import io.ktor.client.HttpClient

actual fun getHttpClient(): HttpClient {
    val client = HttpClient()
    //{
//        HttpClientConfig.install(Logging) {
//            LoggingConfig.logger = Logger.DEFAULT
//            LoggingConfig.level = LogLevel.ALL
//        }
//        // We should allow redirects for all types, not just GET and HEAD
//        // See https://github.com/ktorio/ktor/issues/1793
//        HttpClientConfig.install(HttpRedirect) {
//            HttpRedirectConfig.checkHttpMethod = false
//        }
//        HttpClientConfig.install(WebSockets) {
//            WebSockets.Config.contentConverter = KotlinxWebsocketSerializationConverter(Json)
//        }
//    }
    return client
}

