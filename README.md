<div align="center">

# kiit-identity

**Shared identity vocabulary for a service: who it is, and what kind of thing it is. Kotlin Multiplatform.**

[![Build](https://img.shields.io/github/actions/workflow/status/kiitdev/kiit-identity/ci.yml?branch=main)](https://github.com/kiitdev/kiit-identity/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/kiitdev/kiit-identity)](./LICENSE)
[![Kotlin](https://img.shields.io/badge/kotlin-multiplatform-purple.svg)](https://kotlinlang.org)

Part of [Kiit](https://www.kiit.dev)

</div>

## Table of Contents

- [Why](#why)
- [Start](#start)
- [Concepts](#concepts)
- [Usage](#usage)
- [Requirements](#requirements)
- [License](#license)

## Why

A cache entry needs to know which service owns it. A telemetry counter needs to know which service emitted it. A background job needs its own identity for logging. All of these are the same question, asked from different places: who is this? Most codebases answer it separately per subsystem, a string constant here, a config key there, nothing connecting them. kiit-identity is that one shared identity vocabulary, reused everywhere something needs to be attributed to a service, instead of reinvented per subsystem.

```kotlin
import kiit.identity.Identity

val identity = Identity.api(origin = "acme", scope = "accounts.signup", env = "qat")

println(identity.name)     // acme:accounts.signup:api
println(identity.install)  // acme:accounts.signup:api:qat:latest
```

None of these types are tied to requests, RPC, caching, telemetry, or jobs specifically. Each of those depends on kiit-identity, it doesn't depend on any of them.

## Start

kiit-identity hasn't been published to Maven Central yet. Once it is:

```kotlin
dependencies {
    implementation("dev.kiit:kiit-identity:<version>")
}
```

**Identity is immutable.** `newInstance()`/`with()` return a new value rather than mutating:

```kotlin
import kiit.identity.Identity
import kiit.identity.Tag

val original = Identity.job("acme", "accounts.signup")
val retried = original.with(inst = null, tags = listOf(Tag.Basic("retry")))

println(original.tags)  // []
println(retried.tags)   // [Basic(value=retry)]
```

See [`samples/sample-kotlin`](./samples/sample-kotlin) for a runnable end-to-end example.

**TypeScript.** A native port lives in [`ports/kiit-identity-ts`](./ports/kiit-identity-ts) (`@kiitdev/identity`), with a sample in [`samples/sample-ts`](./samples/sample-ts).

## Concepts

`Identity` builds up five accessors, one field at a time, each one lowercased except `instance`:

| Accessor | Adds | Example |
|---|---|---|
| `path` | `origin`, `scope` | `acme:accounts.signup` |
| `name` | `agent` | `acme:accounts.signup:api` |
| `full` | `env` | `acme:accounts.signup:api:qat` |
| `install` | `version` | `acme:accounts.signup:api:qat:1.0.2` |
| `id` | `instance` | `acme:accounts.signup:api:qat:1.0.2:4a3b300b-...` |

`name` is the same regardless of environment ("this component"), `full` pins it to one environment, `install` pins it to one version deployed to that environment, and `id` is unique per running instance.

| Term | What it is |
|---|---|
| **`origin`** | Domain-like label for who owns this, e.g. `"acme.com"` or `"acme"`. Same convention as `Status.origin` in [kiit-codes](../kiit-codes). |
| **`scope`** | Free-form, consumer-defined label for where in `origin` this lives, e.g. `"accounts.signup"`. Dots express hierarchy, same convention as `Status.scope`. |
| **`Agent`** | What kind of runnable app or service has this identity: `App`, `CLI`, `Web`, `API`, `Bot`, `Job`, `Worker`, `Service`, `Test`. A closed set, a real enum, no runtime-extensible case. |
| **`about`** | Short, human-readable description of what this is or does. Not part of any derived accessor. |
| **`tags`** | Labels attached to this identity: `Tag.Basic("retry")` or `Tag.Keyed("region", "us-east-1")`. `Tag.parse("region=us-east-1")` splits on the first `=`. Not part of any derived accessor, and not normalized — a tag's value often needs preserving exactly as given (a trace id, a hash), not canonicalized the way `origin`/`scope` are. |
| **`uri`** | Optional reference to this instance itself (a hostname, a service-discovery address). Unique per environment, not part of any derived accessor. |

`IIdentity` is the plain data contract (all nine fields, no behavior) for anyone who wants a custom shape. `Identity` is the concrete, constructible implementation, and the only place `path`/`name`/`full`/`install`/`id` live — implementing `IIdentity` yourself doesn't get you those for free, on purpose. If you need them, build a real `Identity`.

**Equality.** Two identities are equal when their `id` is equal, so `about`/`tags`/`uri` don't count — `equals`/`hashCode`/`toString` are all overridden to match, rather than relying on `data class`'s default (which would otherwise compare/print all nine fields). This mirrors how identity actually travels on the wire: a caller sends its `id` as a header (e.g. `x-client-id`), and a server treats two requests as the same caller exactly when that value matches, nothing more.

`Identity.env` is a plain `String`, not a typed enum. kiit-identity has no dependency on the environment-selection module (`kiit-conf-envs`), since `Identity` is needed well beyond env-aware bootstrap code, so callers pass whatever env label they're already using.

## Usage

**Good fit if:**
1. You want one consistent "who/what is this" identifier reused across caching, telemetry, jobs, requests, and anywhere else that needs to attribute something to a service.
2. You're building several things (a server, a client, a job runner) that each need to identify themselves the same way.

**Probably not necessary if:**
1. You only have one service and don't log, cache, or trace anything by identity, a hardcoded string is simpler in that case.
2. A free-text string is enough, you don't need a structured, machine-parseable identity.

## Requirements

- Kotlin Multiplatform
- JVM, Android, iOS (arm64, simulator arm64, x64)
- TypeScript port: Node 24+ and modern browsers
- No dependencies

## License

[Apache License 2.0](./LICENSE)

---

<div align="center">

**kiit-identity** is one module of [Kiit](https://www.kiit.dev), a lightweight, modular
Kotlin toolkit for building server applications, APIs, CLIs, and jobs.

**Adopt one module at a time.**

</div>
