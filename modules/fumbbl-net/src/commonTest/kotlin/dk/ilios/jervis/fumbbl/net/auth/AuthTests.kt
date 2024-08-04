package dk.ilios.jervis.fumbbl.net.auth

import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class AuthTests {
    @Test
    fun testLogin() =
        runBlocking {
            val auth = FumbblAuth()
            auth.login("<insertUser>", "<insertPass>")
            auth.close()
        }
}
