/**
 * How much it matters if the thing this ServiceId describes fails or becomes unavailable.
 * Mirrors OpenTelemetry's `service.criticality` resource attribute (Alpha stability as of this
 * writing), extended here to describe a caller's identity rather than only a telemetry-emitting
 * service's own.
 *
 * Same shape and reasoning as Kind: a small, closed set of values, not open for extension. Not
 * part of any derived identifier (path/name/fullName/install/privateId) — two identities that
 * differ only in criticality are still the same identity for equality purposes. A reorg or tier
 * change shouldn't mint a new identity.
 */
export const Criticality = {
  /** No criticality declared. The default — most callers won't set this. */
  Unspecified: "Unspecified",
  Low: "Low",
  Medium: "Medium",
  High: "High",
  Critical: "Critical",
} as const;

export type Criticality = (typeof Criticality)[keyof typeof Criticality];
