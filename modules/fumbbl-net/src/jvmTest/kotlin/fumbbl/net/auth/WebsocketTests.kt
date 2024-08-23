package fumbbl.net.auth

import dk.ilios.jervis.fumbbl.FumbblWebsocketConnection
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class WebsocketTests {
    private lateinit var adapter: FumbblWebsocketConnection

    @BeforeTest
    fun setUp() {
        adapter = FumbblWebsocketConnection()
    }

    @AfterTest
    fun tearDown() {
        adapter.close()
    }

    @Test
    fun traffic() =
        runBlocking<Unit> {
            adapter.start()
            launch {
                while (!adapter.isClosed) {
                    delay(1000)
//                adapter.send(S)
                }
            }
        }
}
