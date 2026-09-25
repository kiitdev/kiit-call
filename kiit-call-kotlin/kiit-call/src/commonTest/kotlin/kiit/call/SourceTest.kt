package kiit.call

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SourceTest {
    @Test
    fun parsesKnownSources() {
        assertEquals(Source.API, Source.parse("api"))
        assertEquals(Source.CLI, Source.parse("cli"))
        assertEquals(Source.Web, Source.parse("web"))
        assertEquals(Source.Queue, Source.parse("queue"))
    }

    @Test
    fun parseIsCaseInsensitive() {
        assertEquals(Source.API, Source.parse("API"))
        assertEquals(Source.CLI, Source.parse("Cli"))
    }

    @Test
    fun unknownNameBecomesOther() {
        assertEquals(Source.Other("webhook"), Source.parse("webhook"))
    }

    @Test
    fun parentReferenceDetection() {
        assertTrue(Source.Parent.isParentReference())
        assertFalse(Source.API.isParentReference())
    }

    @Test
    fun orElseSubstitutesOnlyForParentReference() {
        assertEquals(Source.Web, Source.Parent.orElse(Source.Web))
        assertEquals(Source.API, Source.API.orElse(Source.Web))
    }
}
