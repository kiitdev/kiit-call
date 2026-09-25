package kiit.call

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TraceTest {
    @Test
    fun parentSpanIdDefaultsToNullAndSampledDefaultsToTrue() {
        val trace = Trace(traceId = "abc")
        assertNull(trace.parentSpanId)
        assertTrue(trace.sampled)
    }

    @Test
    fun fieldsCanBeSuppliedExplicitly() {
        val trace = Trace(traceId = "abc", parentSpanId = "span-1", sampled = false)
        assertEquals("span-1", trace.parentSpanId)
        assertEquals(false, trace.sampled)
    }

    @Test
    fun equalityIsStructural() {
        assertEquals(Trace(traceId = "abc"), Trace(traceId = "abc"))
    }
}
