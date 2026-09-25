<div align="center">

# kiit-call

**Identity, source, and app-description types for who/what/how a call or running instance is associated with. Kotlin Multiplatform.**

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

A cache entry needs to know which service owns it. A telemetry counter needs to know which service emitted it. A background job needs its own identity for logging. A request needs to know both which protocol it arrived on and which caller sent it. All of these are the same underlying question, asked from different places: who or what is this associated with? Most codebases answer it once per subsystem, a string here, a tag there, none of them consistent with each other. kiit-call is that one answer, reused everywhere instead of reinvented per subsystem.

```kotlin
import kiit.call.Identity

val identity = Identity.api(company = "acme", area = "accounts", service = "signup", env = "qat")

println(identity.name)  // acme.accounts.signup.api
println(identity.full)  // acme.accounts.signup.api.qat.latest
```

`Identity` is deliberately not tied to requests, caching, telemetry, or jobs specifically. Each of those depends on it, it doesn't depend on any of them.

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

**`Source` classifies which protocol/channel a call arrived on**, with a named `Other` case for anything not built in:

```kotlin
import kiit.call.Source

Source.parse("api")      // Source.API
Source.parse("webhook")  // Source.Other("webhook")
```

See [`samples/sample-kotlin`](./samples/sample-kotlin) for a runnable end-to-end example.

## Concepts

| Term | What it is |
|---|---|
| **`Identity`** | `company.area.service.agent.env.instance` structural identifier for a service/component. Every field lands in `name`/`full`/`id`, dot-joined, so it's stable and log-friendly. |
| **`Agent`** | What kind of thing has this identity: `App`, `CLI`, `Web`, `API`, `Bot`, `Job`, `Cmd`, `Svc`, `Test`. A closed set, a real enum, no runtime-extensible case. |
| **`Source`** | The protocol/channel a call arrived on, or targets for an outbound call: `API`, `CLI`, `Web`, `Queue`, `Bot`, and others, or `Other(name)` for anything not built in. |
| **`About`** | Human-readable app description (name, desc, url, contact, tags), with `toId()` converting it into the equivalent `Identity`. |

`Identity.env` is a plain `String`, not a typed enum. kiit-call has no dependency on the environment-selection module (`kiit-conf-envs`), since `Identity` is needed well beyond env-aware bootstrap code, so callers pass whatever env label they're already using.

## Usage

**Good fit if:**
1. You want one consistent "who/what is this" identifier reused across caching, telemetry, jobs, requests, and anywhere else that needs to attribute something to a service.
2. You're building a protocol adapter and need a `Source` classification that isn't tied to any one transport's own vocabulary.
3. You want a machine identifier (`Identity`) and a human-readable description (`About`) that convert cleanly between each other, rather than maintaining both separately.

**Probably not necessary if:**
1. You only have one service and don't log, cache, or trace anything by identity, a hardcoded string is simpler in that case.

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
