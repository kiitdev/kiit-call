package kiit.call

/**
 * String/byte content with type/format information attached. Lets a handler return this instead
 * of a plain string when the caller (an HTTP server, an RPC client, etc.) needs to know the
 * content type to send back, without every handler having to know about content types itself.
 */
sealed class Content {
    abstract val data: ByteArray
    abstract val tpe: ContentType

    val isEmpty: Boolean get() = data.isEmpty()

    val isDefined: Boolean get() = !isEmpty

    val size: Int get() = data.size
}

class ContentText(override val data: ByteArray, val raw: String, override val tpe: ContentType) : Content() {
    override fun equals(other: Any?): Boolean =
        this === other ||
            (
                other is ContentText &&
                    data.contentEquals(other.data) &&
                    raw == other.raw &&
                    tpe == other.tpe
            )

    override fun hashCode(): Int = data.contentHashCode() * 31 + raw.hashCode() * 31 + tpe.hashCode()
}

class ContentData(override val data: ByteArray, val raw: String?, override val tpe: ContentType) : Content() {
    override fun equals(other: Any?): Boolean =
        this === other ||
            (
                other is ContentData &&
                    data.contentEquals(other.data) &&
                    raw == other.raw &&
                    tpe == other.tpe
            )

    override fun hashCode(): Int = data.contentHashCode() * 31 + raw.hashCode() * 31 + tpe.hashCode()
}

/**
 * A named file's content, e.g. a multipart upload on the server side or an attachment staged
 * for an outbound call on the client side.
 */
class ContentFile(val name: String, override val data: ByteArray, val raw: String?, override val tpe: ContentType) :
    Content() {
    override fun equals(other: Any?): Boolean =
        this === other ||
            (
                other is ContentFile &&
                    name == other.name &&
                    data.contentEquals(other.data) &&
                    raw == other.raw &&
                    tpe == other.tpe
            )

    override fun hashCode(): Int {
        return name.hashCode() * 31 + data.contentHashCode() * 31 + raw.hashCode() * 31 + tpe.hashCode()
    }
}

/** Bundles several named text fields and files together, e.g. a full multipart form submission. */
data class ContentMulti(val fields: Map<String, ContentText>, val files: Map<String, ContentFile>)

object Contents {
    fun csv(text: String): Content = ContentText(text.encodeToByteArray(), text, ContentTypes.Csv)

    fun html(text: String): Content = ContentText(text.encodeToByteArray(), text, ContentTypes.Html)

    fun json(text: String): Content = ContentText(text.encodeToByteArray(), text, ContentTypes.Json)

    fun text(text: String): Content = ContentText(text.encodeToByteArray(), text, ContentTypes.Plain)

    fun xml(text: String): Content = ContentText(text.encodeToByteArray(), text, ContentTypes.Xml)

    fun other(
        text: String,
        tpe: ContentType,
    ): Content = ContentText(text.encodeToByteArray(), text, tpe)

    fun toText(content: Content?): String? {
        return when (content) {
            null -> ""
            is ContentText -> content.raw
            is ContentData -> content.raw ?: content.data.decodeToString()
            is ContentFile -> content.raw ?: content.data.decodeToString()
        }
    }
}
