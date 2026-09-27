import { describe, expect, it } from "vitest";
import { Tag } from "../src/index.js";

// Ported from TagTest.kt.

describe("Tag.parse", () => {
  it("with no equals becomes Basic", () => {
    expect(Tag.parse("retry")).toEqual(Tag.Basic("retry"));
  });

  it("with equals becomes Keyed", () => {
    expect(Tag.parse("region=us-east-1")).toEqual(Tag.Keyed("region", "us-east-1"));
  });

  it("splits only on the first equals", () => {
    expect(Tag.parse("config=key=value")).toEqual(Tag.Keyed("config", "key=value"));
  });
});

describe("Tag.raw", () => {
  it("reconstructs the original string", () => {
    expect(Tag.Basic("retry").raw).toBe("retry");
    expect(Tag.Keyed("region", "us-east-1").raw).toBe("region=us-east-1");
  });
});
