package fumbbl.net.auth

import com.jervisffb.fumbbl.net.api.auth.FumbblAuth
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
