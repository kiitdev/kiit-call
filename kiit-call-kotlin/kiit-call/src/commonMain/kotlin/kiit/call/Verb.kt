package kiit.call

/**
 * Protocol-neutral CRUD-ish verb for a request, deliberately not HTTP-shaped. Per-protocol
 * rendering (e.g. `Verb.Create` -> HTTP `POST`) is a host/adapter concern, not this type's.
 */
enum class Verb {
    Create,
    Get,

    /** Safe, read-only, but body-bearing. Maps to HTTP's QUERY method. */
    Query,
    Update,
    Patch,
    Delete,

    /** Mixed/general-processing actions with no single clean CRUD verb, e.g. an `inc()`. */
    Execute,
}
