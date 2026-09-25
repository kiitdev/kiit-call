package kiit.call

import kotlin.test.Test
import kotlin.test.assertEquals

class AboutTest {
    @Test
    fun idJoinsAreaAndName() {
        val about = About.simple("kiit", "accounts", "signup", "handles signup")
        assertEquals("accounts.signup", about.id)
    }

    @Test
    fun simpleLeavesEverythingElseBlank() {
        val about = About.simple("kiit", "accounts", "signup", "handles signup")
        assertEquals("", about.region)
        assertEquals("", about.url)
        assertEquals("", about.contact)
        assertEquals("", about.tags)
    }

    @Test
    fun dirPrefersCompanyOverName() {
        val withCompany = About.simple("My Company", "a", "signup", "d")
        assertEquals("My-Company", withCompany.dir())

        val withoutCompany = About.simple("", "a", "Sign Up", "d")
        assertEquals("Sign-Up", withoutCompany.dir())
    }

    @Test
    fun toIdBuildsAnAppIdentityInDev() {
        val about = About.simple("kiit", "accounts", "signup", "handles signup")
        val identity = about.toId()

        assertEquals(Agent.App, identity.agent)
        assertEquals("dev", identity.env)
        assertEquals("kiit", identity.company)
    }

    @Test
    fun logVisitsEveryFieldOnce() {
        val about = About.simple("kiit", "accounts", "signup", "handles signup")
        val seen = mutableMapOf<String, String>()
        about.log { key, value -> seen[key] = value }

        assertEquals("kiit", seen["company"])
        assertEquals("signup", seen["name"])
        assertEquals("handles signup", seen["desc"])
    }
}
