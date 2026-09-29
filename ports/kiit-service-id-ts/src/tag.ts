/** "=" — the delimiter Tag.Keyed's `raw` and `Tag.parse` split on. */
export const TAG_DELIMITER = "=";

/**
 * A label attached to a ServiceId or a request: either a bare value or a key/value pair.
 * `Tag.parse` splits a raw string on its first TAG_DELIMITER, e.g. "retry" -> Tag.Basic,
 * "region=us-east-1" -> Tag.Keyed. TAG_DELIMITER is its own constant, distinct from
 * SERVICE_ID_DELIMITER, on purpose: a tag's own key/value syntax is a different protocol from the
 * accessor chain SERVICE_ID_DELIMITER builds, and sharing one character between them would only
 * invite confusion if either ever needs to change alone.
 *
 * `variant` is a TypeScript-only addition, not present on Kotlin's `Tag`: Kotlin narrows with
 * `is Tag.Basic`/`is Tag.Keyed` against the sealed class hierarchy itself, which TypeScript has
 * no equivalent for over plain object literals, so `variant` is the discriminant a `switch`
 * narrows on instead. Named `variant`, not `kind`, so it doesn't collide with `ServiceId`'s own
 * `kind` field — a completely different classification — within the same package.
 */
export type Tag = Tag.Basic | Tag.Keyed;

export namespace Tag {
  export interface Basic {
    readonly variant: "Basic";
    readonly value: string;
    readonly raw: string;
  }

  /** `Tag.Basic("retry")`, mirroring Kotlin's `Tag.Basic("retry")` data class constructor call. */
  export function Basic(value: string): Basic {
    return { variant: "Basic", value, raw: value };
  }

  export interface Keyed {
    readonly variant: "Keyed";
    readonly key: string;
    readonly value: string;
    readonly raw: string;
  }

  /**
   * `Tag.Keyed("region", "us-east-1")`, mirroring Kotlin's `Tag.Keyed("region", "us-east-1")`
   * data class constructor call.
   */
  export function Keyed(key: string, value: string): Keyed {
    return { variant: "Keyed", key, value, raw: `${key}${TAG_DELIMITER}${value}` };
  }

  /** Splits `raw` into a Basic or Keyed tag, only on its first TAG_DELIMITER. */
  export function parse(raw: string): Tag {
    const idx = raw.indexOf(TAG_DELIMITER);
    return idx < 0 ? Basic(raw) : Keyed(raw.slice(0, idx), raw.slice(idx + TAG_DELIMITER.length));
  }
}
