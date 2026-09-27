/**
 * Random v4 UUID for `Identity.instance`. `crypto.randomUUID` only exists in secure contexts
 * (HTTPS or localhost), so on a plain-HTTP page it's undefined. In that case the UUID is built
 * from `crypto.getRandomValues`, which has no such restriction and is available everywhere.
 */
export function randomUuid(): string {
  const c = globalThis.crypto;

  // The fast path: does the actual UUID generation for us. Present in every secure context
  // (Node, Deno, and any browser page served over HTTPS or from localhost).
  if (typeof c?.randomUUID === "function") {
    return c.randomUUID();
  }

  // No Web Crypto at all: an environment old enough, or stripped-down enough, to lack even
  // getRandomValues. Fail loudly rather than fall back to a non-cryptographic random source,
  // since a predictable instance id defeats the point of it being random.
  if (typeof c?.getRandomValues !== "function") {
    throw new Error("kiit-identity needs the Web Crypto API (globalThis.crypto) to create an instance id");
  }

  // randomUUID is missing but getRandomValues isn't: a browser page on plain HTTP. Build the v4
  // UUID by hand from 16 random bytes, per RFC 4122: force the version nibble to 4 and the
  // variant bits to 10xx, then format as the standard 8-4-4-4-12 hex groups.
  const bytes = c.getRandomValues(new Uint8Array(16));
  bytes[6] = (bytes[6]! & 0x0f) | 0x40; // version 4
  bytes[8] = (bytes[8]! & 0x3f) | 0x80; // RFC 4122 variant
  const hex = Array.from(bytes, (b) => b.toString(16).padStart(2, "0")).join("");
  return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`;
}
