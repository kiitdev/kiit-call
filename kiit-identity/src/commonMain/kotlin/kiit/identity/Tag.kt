package kiit.identity

/**
 * A label attached to an [Identity] or a request: either a bare value or a key/value pair.
 * [parse] splits a raw string on its first `=`, e.g. `"retry"` -> [Basic], `"region=us-east-1"`
 * -> [Keyed]. `=` is used rather than [IDENTITY_DELIMITER] on purpose: a tag's own key/value
 * syntax is a different protocol from the accessor chain [IDENTITY_DELIMITER] builds, and sharing
 * one character between them would only invite confusion if either ever needs to change alone.
 */
sealed class Tag {
    abstract val raw: String

    data class Keyed(val key: String, val value: String) : Tag() {
        override val raw: String = "$key=$value"
    }

    data class Basic(val value: String) : Tag() {
        override val raw: String get() = value
    }

    companion object {
        fun parse(raw: String): Tag {
            val idx = raw.indexOf('=')
            return if (idx < 0) Basic(raw) else Keyed(raw.substring(0, idx), raw.substring(idx + 1))
        }
    }
}
