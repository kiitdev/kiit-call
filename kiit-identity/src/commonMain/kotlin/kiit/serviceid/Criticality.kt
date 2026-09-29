package kiit.serviceid

/**
 * How much it matters if the thing this [ServiceId] describes fails or becomes unavailable.
 * Mirrors OpenTelemetry's `service.criticality` resource attribute (Alpha stability as of this
 * writing), extended here to describe a caller's identity rather than only a
 * telemetry-emitting service's own.
 *
 * Same shape and reasoning as [Kind]: a small, closed set of values, not open for extension.
 * Not part of any derived identifier ([ServiceId.path]/[ServiceId.name]/[ServiceId.fullName]/
 * [ServiceId.install]/[ServiceId.privateId]) — two identities that differ only in [Criticality]
 * are still the same identity for equality purposes. A reorg or tier change shouldn't mint a new
 * identity.
 */
enum class Criticality {
    /** No criticality declared. The default — most callers won't set this. */
    Unspecified,
    Low,
    Medium,
    High,
    Critical,
}
