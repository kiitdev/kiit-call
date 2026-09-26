<div align="center">

# kiit-call

**Shared identity vocabulary for a service: who it is, what kind of thing it is, and how to describe it. Kotlin Multiplatform.**

[![Build](https://img.shields.io/github/actions/workflow/status/kiitdev/kiit-call/ci.yml?branch=main)](https://github.com/kiitdev/kiit-call/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/kiitdev/kiit-call)](./LICENSE)
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

A cache entry needs to know which service owns it. A telemetry counter needs to know which service emitted it. A background job needs its own identity for logging. All of these are the same question, asked from different places: who is this? Most codebases answer it separately per subsystem, a string constant here, a config key there, nothing connecting them. kiit-call is that one shared identity vocabulary, reused everywhere something needs to be attributed to a service, instead of reinvented per subsystem.

```kotlin
import kiit.call.Identity

val identity = Identity.api(company = "acme", area = "accounts", service = "signup", env = "qat")

println(identity.name)  // acme.accounts.signup.api
println(identity.full)  // acme.accounts.signup.api.qat.latest
```

None of these types are tied to requests, RPC, caching, telemetry, or jobs specifically. Each of those depends on kiit-call, it doesn't depend on any of them.

## Start

kiit-call hasn't been published to Maven Central yet. Once it is:

```kotlin
dependencies {
    implementation("dev.kiit:kiit-call:<version>")
}
```

**Identity is immutable.** `newInstance()`/`with()` return a new value rather than mutating:

```kotlin
import kiit.call.Identity

val original = Identity.job("acme", "accounts", "signup")
val retried = original.with(inst = null, tags = listOf("retry"))

println(original.tags)  // []
println(retried.tags)   // [retry]
```

**`About` is the prose complement to `Identity`**, and converts directly into one:

```kotlin
import kiit.call.About

val about = About.simple(company = "acme", area = "accounts", name = "signup", desc = "Handles new user signup.")
val identity = about.toId()
```

See [`samples/sample-kotlin`](./samples/sample-kotlin) for a runnable end-to-end example.

## Concepts

| Term | What it is |
|---|---|
| **`Identity`** | `company.area.service.agent.env.instance` structural identifier for a service/component. Every field lands in `name`/`full`/`id`, dot-joined, so it's stable and log-friendly. |
| **`Agent`** | What kind of runnable app or service has this identity: `App`, `CLI`, `Web`, `API`, `Bot`, `Job`, `Svc`, `Test`. A closed set, a real enum, no runtime-extensible case. |
| **`About`** | Human-readable app description (name, desc, url, contact, tags), with `toId()` converting it into the equivalent `Identity`. |

`Identity.env` is a plain `String`, not a typed enum. kiit-call has no dependency on the environment-selection module (`kiit-conf-envs`), since `Identity` is needed well beyond env-aware bootstrap code, so callers pass whatever env label they're already using.

## Usage

**Good fit if:**
1. You want one consistent "who/what is this" identifier reused across caching, telemetry, jobs, requests, and anywhere else that needs to attribute something to a service.
2. You want a machine identifier (`Identity`) and a human-readable description (`About`) that convert cleanly between each other, rather than maintaining both separately.
3. You're building several things (a server, a client, a job runner) that each need to describe themselves the same way.

**Probably not necessary if:**
1. You only have one service and don't log, cache, or trace anything by identity, a hardcoded string is simpler in that case.
2. A free-text string is enough, you don't need a structured, machine-parseable identity.

## Requirements

- Kotlin Multiplatform
- JVM, Android, iOS (arm64, simulator arm64, x64)
- No dependencies

## License

[Apache License 2.0](./LICENSE)

---

<div align="center">

**kiit-call** is one module of [Kiit](https://www.kiit.dev), a lightweight, modular
Kotlin toolkit for building server applications, APIs, CLIs, and jobs.

**Adopt one module at a time.**

</div>
