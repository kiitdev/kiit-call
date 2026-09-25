package kiit.call

import kotlin.test.Test
import kotlin.test.assertEquals

class VerbTest {
    @Test
    fun coversEveryProtocolNeutralVerb() {
        assertEquals(
            listOf("Create", "Get", "Query", "Update", "Patch", "Delete", "Execute"),
            Verb.entries.map { it.name },
        )
    }
}
