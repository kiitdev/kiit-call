# @kiitdev/identity

Who is making a call, and what kind of runnable thing they are. A native TypeScript port of the Kotlin [`kiit-identity`](../../README.md), checked against it. It isn't a wrapper. Runs in Node and in the browser, no dependencies.

```ts
import { Identity } from "@kiitdev/identity";

const identity = Identity.api("acme", "accounts.signup", "qat");

identity.name;     // "acme:accounts.signup:api"
identity.install;  // "acme:accounts.signup:api:qat:latest"
```

Pre-1.0: the API may still shift before a stable release.

## Install

```bash
npm install @kiitdev/identity
```

## Identity

`Identity` builds up five accessors, one field at a time, each one lowercased except `instance`:

| Accessor | Adds | Example |
|---|---|---|
| `path` | `origin`, `scope` | `acme:accounts.signup` |
| `name` | `agent` | `acme:accounts.signup:api` |
| `full` | `env` | `acme:accounts.signup:api:qat` |
| `install` | `version` | `acme:accounts.signup:api:qat:1.0.2` |
| `id` | `instance` | `acme:accounts.signup:api:qat:1.0.2:4a3b300b-...` |

`name` reads the same regardless of environment ("this component"), `full` pins it to one environment, `install` pins it to one version deployed there, and `id` is unique per running instance.

Shortcuts cover the common agents: `Identity.app`, `api`, `cli`, `job` take `(origin, scope, env = "dev")`, and `Identity.test(origin, name)` puts the identity under a `tests.<name>` scope. For everything else, use `Identity.of` with an options object:

```ts
import { Agent, Identity } from "@kiitdev/identity";

const identity = Identity.of({
  origin: "Code Helix",              // normalized: "code_helix"
  scope: "Accounts Team.Sign Up!",   // "accounts_team.sign_up"
  agent: Agent.Worker,
  env: "PRO",                        // lowercased: "pro"
  version: "1.0.2",
  about: "Sends the welcome email after signup",
  uri: "worker-7.codehelix.internal",
});
```

`of` normalizes `origin` and `scope` (lowercased, trimmed, only letters, digits, `-`, `_`, `.` and spaces kept, spaces turned into `_`, `_` if nothing is left) and lowercases `env`. Letters and digits are Unicode-aware, so `café` and `日本語` survive. Dots in `scope` are kept on purpose, so hierarchy like `"accounts.signup"` survives intact. There's no public constructor — `Identity.of` (or the shortcuts above it) is the only way to build one.

`env` is a plain string, not a typed enum, so pass whatever env label you already use.

## Fields

| Field | What it is |
|---|---|
| `origin` | Domain-like label for who owns this, e.g. `"acme.com"` or `"acme"`. |
| `scope` | Free-form, consumer-defined label for where in `origin` this lives, e.g. `"accounts.signup"`. Dots express hierarchy; kiit-identity never parses it, and it shouldn't contain `:` (reserved for the delimiter). |
| `agent` | What kind of runnable thing this is. See Agent below. |
| `env` | dev \| qat \| pro, environment. |
| `version` | Defaults to `"latest"`. |
| `instance` | Id of this specific running instance. Random by default, never lowercased. |
| `about` | Short description. Not part of any derived accessor. |
| `tags` | Labels attached to this identity. See [Tag](#tag) below. Not part of any derived accessor, and not normalized. |
| `uri` | Optional reference to this instance (a hostname, a service-discovery address). Not part of any derived accessor. |

`IIdentity` is the plain data contract above (all nine fields, no behavior), exported for anyone who wants a custom shape. `Identity` is the concrete implementation, and the only place `path`/`name`/`full`/`install`/`id` live. Implementing `IIdentity` yourself doesn't get you those for free, on purpose: if you need them, build a real `Identity`.

## Immutable

`newInstance()` and `with(inst, tags)` return a new `Identity`. The original is never changed.

```ts
const original = Identity.job("acme", "accounts.signup");
const retried = original.with(null, [Tag.Basic("retry")]); // null: generate a new instance id

original.tags;  // []
retried.tags;   // [{ kind: "Basic", value: "retry", raw: "retry" }]
```

## Agent

`Agent` is `App`, `CLI`, `Web`, `API`, `Bot`, `Job`, `Worker`, `Service` or `Test`. It's an `as const` object with a matching union type, so `Agent.Job` and a `switch` over an `Agent` both type-check exhaustively.

## Tag

A label attached to an identity: either a bare value or a key/value pair.

```ts
import { Tag } from "@kiitdev/identity";

Tag.Basic("retry");                    // { kind: "Basic", value: "retry", raw: "retry" }
Tag.Keyed("region", "us-east-1");      // { kind: "Keyed", key: "region", value: "us-east-1", raw: "region=us-east-1" }
Tag.parse("region=us-east-1");         // same as Tag.Keyed("region", "us-east-1")
```

`Tag.parse` splits on the first `TAG_DELIMITER` (`"="`), so a value containing `=` (e.g. `"config=key=value"`) parses to `Tag.Keyed("config", "key=value")`. `TAG_DELIMITER` is its own constant, separate from `IDENTITY_DELIMITER` — a tag's key/value syntax is a different protocol from the `path`/`name`/`full`/`install`/`id` chain, and giving each its own delimiter means changing one never risks the other. `kind` is a TypeScript-only addition: Kotlin narrows with `is Tag.Basic`/`is Tag.Keyed` against the sealed class itself, which has no equivalent over a plain object here, so `kind` is what a `switch` narrows on instead.

`Tag.Basic`/`Tag.Keyed` are exported factory functions, not classes — `raw` is computed from `value`/`key` at construction, not enforced afterward. Building the object shape by hand instead of going through the factories (or `parse`) can produce a `Tag` where `raw` doesn't actually match `value`/`key`; that's not supported.

## Equality and string form

Two identities are equal when their `id` is equal. Tags, `about` and `uri` don't count — this matches how identity actually travels on the wire: a caller sends its `id` as a header (e.g. `x-client-id`), and a server treats two requests as the same caller exactly when that value matches, nothing more.

```ts
const a = Identity.of({ origin: "acme", scope: "x", agent: Agent.App, instance: "i-1" });

a.equals(a.with("i-1", [Tag.Basic("tagged")]));  // true, same id
a.equals(a.newInstance());                       // false, new instance
`${a}`;                                          // the id
```

`toString()` returns the same `id`, so it works as a `Map` key.

## Differences from Kotlin

1. There's no public constructor. `Identity.of(...)` (or the named shortcuts) is the only way to build one; Kotlin keeps an `internal` (module-visible) raw constructor alongside `of`.
2. `Agent` has no int value. Nothing in the port used it.
3. `Tag`'s `kind` field doesn't exist on Kotlin's `Tag` — see [Tag](#tag) above.

## Browser

The instance id comes from `crypto.randomUUID()`, which browsers only expose on HTTPS pages and on localhost. On a plain-HTTP page, the port builds the UUID from `crypto.getRandomValues` instead, so `Identity.of` doesn't throw there.

## License

[Apache License 2.0](../../LICENSE)
