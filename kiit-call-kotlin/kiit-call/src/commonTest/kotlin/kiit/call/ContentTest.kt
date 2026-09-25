package kiit.call

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ContentTest {
    @Test
    fun buildersProduceTheRightContentType() {
        assertEquals(ContentTypes.Csv, Contents.csv("a,b").tpe)
        assertEquals(ContentTypes.Html, Contents.html("<p></p>").tpe)
        assertEquals(ContentTypes.Json, Contents.json("{}").tpe)
        assertEquals(ContentTypes.Plain, Contents.text("hi").tpe)
        assertEquals(ContentTypes.Xml, Contents.xml("<a/>").tpe)
    }

    @Test
    fun textContentPreservesRawAndEncodesData() {
        val content = Contents.json("""{"a":1}""")
        assertEquals("""{"a":1}""", (content as ContentText).raw)
        assertEquals("""{"a":1}""".encodeToByteArray().size, content.data.size)
    }

    @Test
    fun emptyAndDefinedReflectDataSize() {
        val empty = Contents.text("")
        val nonEmpty = Contents.text("hi")

        assertTrue(empty.isEmpty)
        assertFalse(empty.isDefined)
        assertFalse(nonEmpty.isEmpty)
        assertTrue(nonEmpty.isDefined)
    }

    @Test
    fun toTextPrefersRawOverDecodingBytes() {
        assertEquals("hello", Contents.toText(Contents.text("hello")))
        assertEquals("", Contents.toText(null))
    }

    @Test
    fun toTextDecodesBytesForContentDataAndContentFileWhenRawIsMissing() {
        val data = ContentData("hello".encodeToByteArray(), null, ContentTypes.Plain)
        val file = ContentFile("note.txt", "hello".encodeToByteArray(), null, ContentTypes.Plain)
        assertEquals("hello", Contents.toText(data))
        assertEquals("hello", Contents.toText(file))
    }

    @Test
    fun contentTypeParseFallsBackToJson() {
        assertEquals(ContentTypes.Csv, ContentType.parse("csv"))
        assertEquals(ContentTypes.Json, ContentType.parse("unknown-extension"))
    }

    @Test
    fun contentTextEqualityComparesByteArrayContentsNotReferences() {
        val a = ContentText("hi".encodeToByteArray(), "hi", ContentTypes.Plain)
        val b = ContentText("hi".encodeToByteArray(), "hi", ContentTypes.Plain)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun contentDataEqualityComparesByteArrayContentsNotReferences() {
        val a = ContentData("hi".encodeToByteArray(), "hi", ContentTypes.Plain)
        val b = ContentData("hi".encodeToByteArray(), "hi", ContentTypes.Plain)
        val c = ContentData("bye".encodeToByteArray(), "bye", ContentTypes.Plain)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, c)
    }

    @Test
    fun contentFileEqualityComparesByteArrayContentsNotReferences() {
        val a = ContentFile("note.txt", "hi".encodeToByteArray(), "hi", ContentTypes.Plain)
        val b = ContentFile("note.txt", "hi".encodeToByteArray(), "hi", ContentTypes.Plain)
        val c = ContentFile("other.txt", "hi".encodeToByteArray(), "hi", ContentTypes.Plain)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, c)
    }
}
