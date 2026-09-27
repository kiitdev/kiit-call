/**
 * A label attached to an Identity or a request: either a bare value or a key/value pair. `parse`
 * splits a raw string on its first "=", e.g. "retry" -> Basic, "region=us-east-1" -> Keyed. "="
 * is used rather than IDENTITY_DELIMITER on purpose: a tag's own key/value syntax is a different
 * protocol from the accessor chain IDENTITY_DELIMITER builds, and sharing one character between
 * them would only invite confusion if either ever needs to change alone.
 */
export type Tag = Tag.Basic | Tag.Keyed;

// eslint-disable-next-line @typescript-eslint/no-namespace
export namespace Tag {
  export interface Basic {
    readonly kind: "basic";
    readonly value: string;
    readonly raw: string;
  }

  export interface Keyed {
    readonly kind: "keyed";
    readonly key: string;
    readonly value: string;
    readonly raw: string;
  }

  export function basic(value: string): Basic {
    return { kind: "basic", value, raw: value };
  }

  export function keyed(key: string, value: string): Keyed {
    return { kind: "keyed", key, value, raw: `${key}=${value}` };
  }

  /** Splits `raw` into a Basic or Keyed tag, only on its first "=". */
  export function parse(raw: string): Tag {
    const idx = raw.indexOf("=");
    return idx < 0 ? basic(raw) : keyed(raw.slice(0, idx), raw.slice(idx + 1));
  }
}
