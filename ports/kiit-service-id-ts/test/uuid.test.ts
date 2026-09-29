import { afterEach, describe, expect, it, vi } from "vitest";
import { randomUuid } from "../src/uuid.js";

const V4 = /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/;

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("randomUuid", () => {
  it("returns a v4 uuid", () => {
    expect(randomUuid()).toMatch(V4);
  });

  it("falls back to getRandomValues when randomUUID is missing (non-secure context)", () => {
    const real = globalThis.crypto;
    vi.stubGlobal("crypto", { getRandomValues: real.getRandomValues.bind(real) });
    const first = randomUuid();
    const second = randomUuid();
    expect(first).toMatch(V4);
    expect(second).toMatch(V4);
    expect(first).not.toBe(second);
  });

  it("sets the version and variant bits on the fallback", () => {
    vi.stubGlobal("crypto", { getRandomValues: (a: Uint8Array) => a.fill(0xff) });
    expect(randomUuid()).toBe("ffffffff-ffff-4fff-bfff-ffffffffffff");
  });

  it("throws a clear error when there is no Web Crypto at all", () => {
    vi.stubGlobal("crypto", undefined);
    expect(() => randomUuid()).toThrow(/Web Crypto/);
  });
});
