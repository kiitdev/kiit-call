package kiit.call

/**
 * Distributed tracing context (W3C Trace Context shape), for correlating a request across
 * multiple hops, e.g. an HTTP call that triggers a queue message that triggers another action,
 * all sharing one trace. kiit only carries this data through faithfully; it doesn't create,
 * manage, or propagate spans itself. That's owned by whatever tracing SDK is integrated at the
 * host boundary.
 *
 * @param traceId Shared across every hop.
 * @param parentSpanId The span that triggered this request, if any. Not this request's own span,
 * since handling it creates a new span downstream, outside kiit's responsibility.
 */
data class Trace(
    val traceId: String,
    val parentSpanId: String? = null,
    val sampled: Boolean = true,
)
