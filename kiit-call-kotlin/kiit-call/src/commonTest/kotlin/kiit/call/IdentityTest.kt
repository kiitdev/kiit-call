package kiit.call

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class IdentityTest {
    @Test
    fun nameFullIdFollowTheDocumentedConvention() {
        val identity = Identity.of("app1", "accounts", "signup", Agent.Job, "qat", version = "1.0.2.3")

        assertEquals("app1.accounts.signup.job", identity.name)
        assertEquals("app1.accounts.signup.job.qat.1_0_2_3", identity.full)
        assertTrue(identity.id.startsWith("app1.accounts.signup.job.qat.1_0_2_3."))
    }

    @Test
    fun ofNormalizesCompanyAreaAndServiceIntoIdentifiers() {
        val identity = Identity.of("My Company", "Accounts Team", "Sign Up!", Agent.API)

        assertEquals("my_company", identity.company)
        assertEquals("accounts_team", identity.area)
        assertEquals("sign_up", identity.service)
    }

    @Test
    fun defaultsVersionToLatestAndEnvToDev() {
        val identity = Identity.of("app1", "accounts", "signup", Agent.App)

        assertEquals("latest", identity.version)
        assertEquals("dev", identity.env)
    }

    @Test
    fun everyIdentityGetsAUniqueInstance() {
        val first = Identity.of("app1", "accounts", "signup", Agent.App)
        val second = Identity.of("app1", "accounts", "signup", Agent.App)

        assertNotEquals(first.instance, second.instance)
    }

    @Test
    fun newInstanceKeepsEverythingElseButChangesInstance() {
        val original = Identity.of("app1", "accounts", "signup", Agent.App)
        val renewed = original.newInstance()

        assertNotEquals(original.instance, renewed.instance)
        assertEquals(original.company, renewed.company)
        assertEquals(original.full, renewed.full)
    }

    @Test
    fun withOverridesInstanceAndTags() {
        val original = Identity.of("app1", "accounts", "signup", Agent.App)
        val updated = original.with("fixed-instance", listOf("a", "b"))

        assertEquals("fixed-instance", updated.instance)
        assertEquals(listOf("a", "b"), updated.tags)
        assertTrue(updated.idWithTags.endsWith("fixed-instance.a, b"))
    }

    @Test
    fun convenienceFactoriesUseTheMatchingAgent() {
        assertEquals(Agent.App, Identity.app("c", "a", "s").agent)
        assertEquals(Agent.API, Identity.api("c", "a", "s").agent)
        assertEquals(Agent.CLI, Identity.cli("c", "a", "s").agent)
        assertEquals(Agent.Job, Identity.job("c", "a", "s").agent)
        assertEquals(Agent.Test, Identity.test("c", "s").agent)
    }
}
