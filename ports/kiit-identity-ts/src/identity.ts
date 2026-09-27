import { Agent } from "./agent.js";
import type { Tag } from "./tag.js";
import { randomUuid } from "./uuid.js";

/** ":" — the delimiter Identity's `path`/`name`/`fullName`/`install`/`id` are built from. */
export const IDENTITY_DELIMITER = ":";

/**
 * Contract for anything that identifies a caller or service. Deliberately a plain data shape,
 * nothing more: no `path`/`id`-style derived properties here, and nothing to inherit for free.
 * `Identity` is the canonical, constructible way to get that behavior. Implement this directly
 * only when you need a custom storage shape (wrapping an existing domain object, say); you still
 * won't get `path`/`name`/`fullName`/`install`/`id` for it, since those belong to `Identity`, not to
 * every possible shape that happens to carry these nine fields.
 *
 * The fields build up from broad to specific:
 * 1. origin and scope say who this is.
 * 2. agent says what kind of thing it is.
 * 3. env and version say where it's running and which build.
 * 4. instance tells apart multiple running copies of the same version.
 *
 * about and uri are side information, not part of any derived identifier.
 */
export interface IIdentity {
  /**
   * Domain-like label for who owns this identity, e.g. "acme.com" or "acme". Same convention as
   * kiit-codes' `Status.origin`: a real domain or any other stable id, not validated, and not
   * guaranteed unique unless it's an actual domain.
   */
  readonly origin: string;

  /**
   * Free-form, consumer-defined label for where in origin this lives, e.g. "accounts.signup".
   * Same convention as `Status.scope`: dots are fine for expressing hierarchy, kiit-identity
   * never parses or enforces the internal shape, and it shouldn't contain ":" (reserved, see
   * IDENTITY_DELIMITER).
   */
  readonly scope: string;

  /** The kind of runnable app or service this is, e.g. Agent.API for an HTTP service. */
  readonly agent: Agent;

  /** dev | qat | pro, environment. A plain string, so callers pass whatever env label they already use. */
  readonly env: string;

  /**
   * Short, human-readable description of what this is or does, e.g.
   * "Sends the welcome email after signup". Empty string means unset.
   */
  readonly about: string;

  /** The version running here, e.g. "1.4.2". Defaults to "latest". */
  readonly version: string;

  /**
   * Id of this specific running instance, for telling apart multiple instances of the same
   * version (a redeploy, a pod, a worker in a pool). Random by default, e.g.
   * "4a3b300b-d0ac-4776-8a9c-31aa75e412b3", or a caller-supplied id like "pod-7f9c9d4b8-x2kq1".
   * Left as-is in `id`, unlike every other field here: its whole job is uniqueness, and
   * lowercasing it could make two different instances collide.
   */
  readonly instance: string;

  /**
   * Labels attached to this identity, e.g. [Tag.Basic("retry"), Tag.Keyed("region", "us-east-1")].
   * Not part of any derived identifier.
   */
  readonly tags: readonly Tag[];

  /**
   * Optional reference to this instance itself, e.g. "accounts-signup.acme.internal". Unique per
   * environment. Not part of any derived identifier.
   */
  readonly uri: string | null;
}

/**
 * Options for `Identity.of(...)`. Only origin, scope and agent are required. `null` is accepted
 * alongside `undefined` for every optional field, matching Kotlin's nullable parameters.
 */
export interface IdentityOptions {
  readonly origin: string;
  readonly scope: string;
  readonly agent: Agent;
  readonly env?: string | null;
  readonly about?: string | null;
  readonly version?: string | null;
  readonly instance?: string | null;
  readonly tags?: readonly Tag[] | null;
  readonly uri?: string | null;
}

/**
 * Identity used to identify services/components.
 *
 * ```
 * path     = origin:scope
 * name     = path:agent                = origin:scope:agent
 * fullName = name:env                  = origin:scope:agent:env
 * install  = fullName:version          = origin:scope:agent:env:version
 * id       = install:instance          = origin:scope:agent:env:version:instance
 * ```
 *
 * For `Identity.api("acme", "accounts.signup", "qat")` with `version: "1.4.2"`:
 * ```
 * path     = acme:accounts.signup
 * name     = acme:accounts.signup:api
 * fullName = acme:accounts.signup:api:qat
 * install  = acme:accounts.signup:api:qat:1.4.2
 * id       = acme:accounts.signup:api:qat:1.4.2:4a3b300b-d0ac-4776-8a9c-31aa75e412b3
 * ```
 *
 * Every segment except `instance` is lowercased, so two identities that only differ by casing in
 * origin/scope/agent/env/version still produce the same path/name/fullName/install/id. `instance`
 * is left exactly as given, since folding its case could make two genuinely different instances
 * collide.
 *
 * Immutable. `newInstance`/`with` return a new `Identity` rather than mutating this one.
 *
 * Two identities are equal when their `id` is equal, so tags/about/uri don't count. `toString()`
 * returns the same `id`.
 *
 * There's no public constructor. `Identity.of(...)` (or the named shortcuts below it) is the only
 * way to build one, and it's the only place origin/scope get normalized, see `normalize`.
 */
export class Identity implements IIdentity {
  readonly origin: string;
  readonly scope: string;
  readonly agent: Agent;
  readonly env: string;
  readonly about: string;
  readonly version: string;
  readonly instance: string;
  readonly tags: readonly Tag[];
  readonly uri: string | null;

  private constructor(options: IdentityOptions) {
    this.origin = options.origin;
    this.scope = options.scope;
    this.agent = options.agent;
    this.env = options.env ?? "dev";
    this.about = options.about ?? "";
    this.version = options.version ?? "latest";
    this.instance = options.instance ?? randomUuid();
    this.tags = Object.freeze([...(options.tags ?? [])]);
    this.uri = options.uri ?? null;
  }

  /** `origin:scope`, lowercased, e.g. "acme:accounts.signup". */
  get path(): string {
    return `${this.origin.toLowerCase()}${IDENTITY_DELIMITER}${this.scope.toLowerCase()}`;
  }

  /** `path` plus `agent`, lowercased, e.g. "acme:accounts.signup:api". The same component, any environment. */
  get name(): string {
    return `${this.path}${IDENTITY_DELIMITER}${this.agent.toLowerCase()}`;
  }

  /** `name` plus `env`, lowercased, e.g. "acme:accounts.signup:api:qat". */
  get fullName(): string {
    return `${this.name}${IDENTITY_DELIMITER}${this.env.toLowerCase()}`;
  }

  /**
   * `fullName` plus `version`, lowercased, e.g. "acme:accounts.signup:api:qat:1.4.2". Named for
   * what it is: a specific version installed into a specific environment.
   */
  get install(): string {
    return `${this.fullName}${IDENTITY_DELIMITER}${this.version.toLowerCase()}`;
  }

  /**
   * `install` plus `instance`, not lowercased, e.g.
   * "acme:accounts.signup:api:qat:1.4.2:4a3b300b-d0ac-4776-8a9c-31aa75e412b3". Unique per running
   * instance.
   */
  get id(): string {
    return `${this.install}${IDENTITY_DELIMITER}${this.instance}`;
  }

  /** Same identity with a new random instance id. */
  newInstance(): Identity {
    return new Identity({ ...this, instance: randomUuid() });
  }

  /** Same identity with the given instance id (random if null/undefined) and tags. */
  with(inst: string | null | undefined, tags: readonly Tag[]): Identity {
    return new Identity({ ...this, instance: inst ?? randomUuid(), tags });
  }

  equals(other: IIdentity): boolean {
    return this.id === Identity.of(other).id;
  }

  toString(): string {
    return this.id;
  }

  /** A placeholder identity, for tests and defaults. */
  static readonly empty: Identity = new Identity({ origin: "", scope: "empty", agent: Agent.Test, env: "empty" });

  static app(origin: string, scope: string, env = "dev"): Identity {
    return Identity.of({ origin, scope, agent: Agent.App, env });
  }

  static api(origin: string, scope: string, env = "dev"): Identity {
    return Identity.of({ origin, scope, agent: Agent.API, env });
  }

  static cli(origin: string, scope: string, env = "dev"): Identity {
    return Identity.of({ origin, scope, agent: Agent.CLI, env });
  }

  static job(origin: string, scope: string, env = "dev"): Identity {
    return Identity.of({ origin, scope, agent: Agent.Job, env });
  }

  /** A test identity: scope is "tests.<name>", agent is Test, env is "dev". */
  static test(origin: string, name: string): Identity {
    return Identity.of({ origin, scope: `tests.${name}`, agent: Agent.Test, env: "dev" });
  }

  /**
   * Builds an Identity from names, normalizing origin/scope (see `normalize`) and lowercasing
   * env. This is the only public way to build one, since the constructor itself is private.
   */
  static of(options: IdentityOptions): Identity {
    return new Identity({
      ...options,
      origin: normalize(options.origin),
      scope: normalize(options.scope),
      env: (options.env ?? "dev").toLowerCase(),
    });
  }
}

/**
 * Normalizes a name into an identifier: lowercased, trimmed, keeping only letters, digits, "-",
 * "_", ".", and space, with spaces turned into "_". Falls back to "_" if nothing survives, e.g.
 * "My Company" -> "my_company", "Accounts Team.Sign Up!" -> "accounts_team.sign_up". Dots are
 * kept so a scope like "accounts.signup" survives normalization intact. Letters and digits are
 * Unicode-aware, same as Kotlin's `isLetter`/`isDigit`.
 */
function normalize(value: string): string {
  const filtered = value.trim().toLowerCase().replace(/[^\p{L}\p{Nd}\-_. ]/gu, "");
  const cleaned = filtered.trim() === "" ? "_" : filtered;
  return cleaned.replaceAll(" ", "_");
}
