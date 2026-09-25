package kiit.call

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class VersionTest {
    @Test
    fun actionOverrideDefaultsToNull() {
        val version = Version(api = "1")
        assertNull(version.action)
    }

    @Test
    fun actionOverrideCanBeSuppliedExplicitly() {
        val version = Version(api = "1", action = "2")
        assertEquals("2", version.action)
    }

    @Test
    fun equalityIsStructural() {
        assertEquals(Version(api = "1"), Version(api = "1"))
    }
}
