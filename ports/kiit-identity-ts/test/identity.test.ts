import { describe, expect, it } from "vitest";
import { Agent, Identity, Tag } from "../src/index.js";
import toIdentCases from "./fixtures/to-ident-cases.json" with { type: "json" };

// Ported from IdentityTest.kt. Kotlin's named/default arguments become an options object.

describe("Identity naming", () => {
  it("path, name, full, install and id follow the documented convention", () => {
    const identity = Identity.of({ origin: "app1", scope: "accounts.signup", agent: Agent.Job, env: "qat", version: "1.0.2" });

    expect(identity.path).toBe("app1:accounts.signup");
    expect(identity.name).toBe("app1:accounts.signup:job");
    expect(identity.full).toBe("app1:accounts.signup:job:qat");
    expect(identity.install).toBe("app1:accounts.signup:job:qat:1.0.2");
    expect(identity.id.startsWith("app1:accounts.signup:job:qat:1.0.2:")).toBe(true);
  });

  // origin/scope/env are already lowercased by `of`, so there's no public way to construct an
  // Identity with those un-lowercased (the constructor is private). What's left to prove the
  // accessors lowercase regardless: Agent's own casing ("API", not "api") and version, which `of`
  // passes through unchanged.
  it("lowercases agent and version in the derived accessors", () => {
    const identity = Identity.of({ origin: "app1", scope: "s", agent: Agent.API, version: "1.0.RC1" });

    expect(identity.name).toBe("app1:s:api");
    expect(identity.install).toBe("app1:s:api:dev:1.0.rc1");
  });
});

describe("Identity.of", () => {
  it("normalizes origin and scope into identifiers", () => {
    const identity = Identity.of({ origin: "My Company", scope: "Accounts Team.Sign Up!", agent: Agent.API });

    expect(identity.origin).toBe("my_company");
    expect(identity.scope).toBe("accounts_team.sign_up");
  });

  it("defaults version to latest and env to dev", () => {
    const identity = Identity.of({ origin: "app1", scope: "accounts.signup", agent: Agent.App });

    expect(identity.version).toBe("latest");
    expect(identity.env).toBe("dev");
    expect(identity.about).toBe("");
    expect(identity.tags).toEqual([]);
    expect(identity.uri).toBeNull();
  });

  it("treats null optional fields like missing ones", () => {
    const identity = Identity.of({
      origin: "c", scope: "s", agent: Agent.App, version: null, about: null, instance: null, uri: null,
    });

    expect(identity.version).toBe("latest");
    expect(identity.about).toBe("");
    expect(identity.uri).toBeNull();
    expect(identity.instance).not.toBe("");
  });

  it("keeps an explicit instance, version, about, tags and uri", () => {
    const identity = Identity.of({
      origin: "c", scope: "s", agent: Agent.App, instance: "i-1", version: "2.0", about: "hello",
      tags: [Tag.Basic("retry"), Tag.Keyed("region", "us-east-1")], uri: "svc-7.internal",
    });

    expect(identity.tags).toEqual([Tag.Basic("retry"), Tag.Keyed("region", "us-east-1")]);
    expect(identity.instance).toBe("i-1");
    expect(identity.version).toBe("2.0");
    expect(identity.about).toBe("hello");
    expect(identity.uri).toBe("svc-7.internal");
  });

  it("gives every identity a unique instance", () => {
    const first = Identity.of({ origin: "app1", scope: "accounts.signup", agent: Agent.App });
    const second = Identity.of({ origin: "app1", scope: "accounts.signup", agent: Agent.App });

    expect(first.instance).not.toBe(second.instance);
  });
});

describe("toIdent (through Identity.of)", () => {
  it.each(toIdentCases)("normalizes $input", ({ input, expected }) => {
    expect(Identity.of({ origin: input, scope: "s", agent: Agent.App }).origin).toBe(expected);
  });
});

describe("Identity copies", () => {
  it("newInstance keeps everything else but changes the instance", () => {
    const original = Identity.of({ origin: "app1", scope: "accounts.signup", agent: Agent.App });
    const renewed = original.newInstance();

    expect(renewed.instance).not.toBe(original.instance);
    expect(renewed.origin).toBe(original.origin);
    expect(renewed.install).toBe(original.install);
    expect(renewed.id).not.toBe(original.id);
  });

  it("with overrides the instance and tags", () => {
    const original = Identity.of({ origin: "app1", scope: "accounts.signup", agent: Agent.App });
    const updated = original.with("fixed-instance", [Tag.Basic("a"), Tag.Basic("b")]);

    expect(updated.instance).toBe("fixed-instance");
    expect(updated.tags).toEqual([Tag.Basic("a"), Tag.Basic("b")]);
    expect(updated.id.endsWith("fixed-instance")).toBe(true);
  });

  it("with generates an instance when given null", () => {
    const original = Identity.of({ origin: "c", scope: "s", agent: Agent.App, instance: "i-1" });

    expect(original.with(null, []).instance).not.toBe("i-1");
  });

  it("does not mutate the original", () => {
    const original = Identity.of({ origin: "c", scope: "s", agent: Agent.App, instance: "i-1" });
    original.with("i-2", [Tag.Basic("x")]);
    original.newInstance();

    expect(original.instance).toBe("i-1");
    expect(original.tags).toEqual([]);
  });

  it("copies the tags array, so later changes to the input don't leak in", () => {
    const tags = [Tag.Basic("a")];
    const identity = Identity.of({ origin: "c", scope: "s", agent: Agent.App }).with("i", tags);
    tags.push(Tag.Basic("b"));

    expect(identity.tags).toEqual([Tag.Basic("a")]);
    expect(Object.isFrozen(identity.tags)).toBe(true);
  });
});

describe("Identity factories", () => {
  it("use the matching agent", () => {
    expect(Identity.app("c", "s").agent).toBe(Agent.App);
    expect(Identity.api("c", "s").agent).toBe(Agent.API);
    expect(Identity.cli("c", "s").agent).toBe(Agent.CLI);
    expect(Identity.job("c", "s").agent).toBe(Agent.Job);
    expect(Identity.test("c", "signup").agent).toBe(Agent.Test);
  });

  it("default env to dev and accept an override", () => {
    expect(Identity.api("c", "s").env).toBe("dev");
    expect(Identity.api("c", "s", "PRO").env).toBe("pro");
  });

  it("test puts the identity under a tests scope, on dev", () => {
    const identity = Identity.test("acme", "Login Flow");

    expect(identity.scope).toBe("tests.login_flow");
    expect(identity.env).toBe("dev");
  });

  it("empty is the placeholder identity", () => {
    expect(Identity.empty.install).toBe(":empty:test:empty:latest");
  });
});

describe("Identity equality and string form", () => {
  const base = () => Identity.of({ origin: "c", scope: "s", agent: Agent.App, instance: "i-1" });

  it("toString is the id", () => {
    expect(String(base())).toBe(base().id);
    expect(`${base()}`).toBe("c:s:app:dev:latest:i-1");
  });

  it("is equal when the id is equal, ignoring tags, about and uri", () => {
    const other = Identity.of({ origin: "c", scope: "s", agent: Agent.App, instance: "i-1", about: "different", uri: "x" });

    expect(base().equals(other)).toBe(true);
    expect(base().equals(base().with("i-1", ["tagged"]))).toBe(true);
  });

  it("is not equal across instances, env, version or agent", () => {
    expect(base().equals(base().newInstance())).toBe(false);
    expect(base().equals(Identity.of({ origin: "c", scope: "s", agent: Agent.App, instance: "i-1", env: "qat" }))).toBe(false);
    expect(base().equals(Identity.of({ origin: "c", scope: "s", agent: Agent.App, instance: "i-1", version: "2" }))).toBe(false);
    expect(base().equals(Identity.of({ origin: "c", scope: "s", agent: Agent.Job, instance: "i-1" }))).toBe(false);
  });
});
