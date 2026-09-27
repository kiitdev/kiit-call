package kiit.identity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class IdentityTest {
    @Test
    fun pathNameFullInstallIdFollowTheDocumentedConvention() {
        val identity = Identity.of("app1", "accounts.signup", Agent.Job, "qat", version = "1.0.2")

        assertEquals("app1:accounts.signup", identity.path)
        assertEquals("app1:accounts.signup:job", identity.name)
        assertEquals("app1:accounts.signup:job:qat", identity.full)
        assertEquals("app1:accounts.signup:job:qat:1.0.2", identity.install)
        assertTrue(identity.id.startsWith("app1:accounts.signup:job:qat:1.0.2:"))
    }

    @Test
    fun everySegmentExceptInstanceIsLowercased() {
        val identity =
            Identity(
                origin = "App1",
                scope = "Accounts.Signup",
                agent = Agent.API,
                env = "QAT",
                version = "1.0.RC1",
                instance = "Instance-Mixed-Case",
            )

        assertEquals("app1:accounts.signup:api:qat:1.0.rc1:Instance-Mixed-Case", identity.id)
    }

    @Test
    fun ofNormalizesOriginAndScopeIntoIdentifiers() {
        val identity = Identity.of("My Company", "Accounts Team.Sign Up!", Agent.API)

        assertEquals("my_company", identity.origin)
        assertEquals("accounts_team.sign_up", identity.scope)
    }

    @Test
    fun defaultsVersionToLatestAndEnvToDev() {
        val identity = Identity.of("app1", "accounts.signup", Agent.App)

        assertEquals("latest", identity.version)
        assertEquals("dev", identity.env)
        assertEquals("", identity.about)
        assertEquals(emptyList(), identity.tags)
        assertEquals(null, identity.uri)
    }

    @Test
    fun everyIdentityGetsAUniqueInstance() {
        val first = Identity.of("app1", "accounts.signup", Agent.App)
        val second = Identity.of("app1", "accounts.signup", Agent.App)

        assertNotEquals(first.instance, second.instance)
    }

    @Test
    fun newInstanceKeepsEverythingElseButChangesTheInstance() {
        val original = Identity.of("app1", "accounts.signup", Agent.App)
        val renewed = original.newInstance()

        assertNotEquals(original.instance, renewed.instance)
        assertEquals(original.origin, renewed.origin)
        assertEquals(original.install, renewed.install)
        assertNotEquals(original.id, renewed.id)
    }

    @Test
    fun withOverridesTheInstanceAndTags() {
        val original = Identity.of("app1", "accounts.signup", Agent.App)
        val updated = original.with("fixed-instance", listOf("a", "b"))

        assertEquals("fixed-instance", updated.instance)
        assertEquals(listOf("a", "b"), updated.tags)
        assertTrue(updated.id.endsWith("fixed-instance"))
    }

    @Test
    fun withGeneratesAnInstanceWhenGivenNull() {
        val original = Identity.of("app1", "accounts.signup", Agent.App, instance = "i-1")

        assertNotEquals("i-1", original.with(null, listOf()).instance)
    }

    @Test
    fun convenienceFactoriesUseTheMatchingAgent() {
        assertEquals(Agent.App, Identity.app("c", "s").agent)
        assertEquals(Agent.API, Identity.api("c", "s").agent)
        assertEquals(Agent.CLI, Identity.cli("c", "s").agent)
        assertEquals(Agent.Job, Identity.job("c", "s").agent)
        assertEquals(Agent.Test, Identity.test("c", "signup").agent)
    }

    @Test
    fun testFactoryPutsTheIdentityUnderATestsScope() {
        val identity = Identity.test("acme", "Login Flow")

        assertEquals("tests.login_flow", identity.scope)
        assertEquals("dev", identity.env)
    }

    @Test
    fun emptyIsThePlaceholderIdentity() {
        assertEquals(":empty:test:empty:latest", Identity.empty.install)
    }

    // Same cases as ports/kiit-identity-ts/test/fixtures/to-ident-cases.json. Keep the two in sync
    // by hand, they're what guards the TypeScript port's toIdent against drifting from this one.
    @Test
    fun toIdentMatchesTheTypeScriptPortFixture() {
        val cases =
            listOf(
                "My Company" to "my_company",
                "Sign Up!" to "sign_up",
                "  Trim Me  " to "trim_me",
                "a.b.c" to "a.b.c",
                "!!!" to "_",
                "   " to "_",
                "" to "_",
                "Über-Svc_1" to "über-svc_1",
                "café" to "café",
                "日本語 サービス" to "日本語_サービス",
            )
        for ((input, expected) in cases) {
            assertEquals(expected, Identity.of(input, "s", Agent.App).origin, "toIdent(\"$input\")")
        }
    }
}
