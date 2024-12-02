package fumbbl.net.auth

import com.jervisffb.fumbbl.net.FumbblWebsocketConnection
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
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
    @Ignore // This is not testing anything right now
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
