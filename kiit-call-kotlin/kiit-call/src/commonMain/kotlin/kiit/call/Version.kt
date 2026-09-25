package kiit.call

/**
 * A request's API-level version, with an optional action-level override.
 *
 * @param api API-level version, always parsed from the route, always present.
 * @param action Optional caller-supplied override (e.g. via header). Null unless explicitly
 * supplied. Resolving the actual default when no override is present (single-version -> that
 * version; multiple versions -> the unversioned variant, or the second-to-latest) is a
 * dispatcher concern, not something this type does.
 */
data class Version(
    val api: String,
    val action: String? = null,
)
