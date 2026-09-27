import { describe, expect, it } from "vitest";
import { Agent } from "../src/index.js";

describe("Agent", () => {
  it("has the same members as the Kotlin enum", () => {
    expect(Object.keys(Agent)).toEqual(["App", "CLI", "Web", "API", "Bot", "Job", "Worker", "Service", "Test"]);
  });

  it("uses the member name as its value", () => {
    for (const [key, value] of Object.entries(Agent)) {
      expect(value).toBe(key);
    }
  });
});
