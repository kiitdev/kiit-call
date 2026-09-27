package kiit.identity

import kotlin.test.Test
import kotlin.test.assertEquals

class TagTest {
    @Test
    fun parseWithNoEqualsBecomesBasic() {
        assertEquals(Tag.Basic("retry"), Tag.parse("retry"))
    }

    @Test
    fun parseWithEqualsBecomesKeyed() {
        assertEquals(Tag.Keyed("region", "us-east-1"), Tag.parse("region=us-east-1"))
    }

    @Test
    fun parseSplitsOnlyOnFirstEquals() {
        assertEquals(Tag.Keyed("config", "key=value"), Tag.parse("config=key=value"))
    }

    @Test
    fun rawReconstructsTheOriginalString() {
        assertEquals("retry", Tag.Basic("retry").raw)
        assertEquals("region=us-east-1", Tag.Keyed("region", "us-east-1").raw)
    }
}
