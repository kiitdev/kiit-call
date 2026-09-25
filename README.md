<div align="center">

# kiit-call

**Shared vocabulary for a call: who's making it, what channel it arrived on, what verb/version/trace/content it carries. Kotlin Multiplatform.**

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

A cache entry needs to know which service owns it. A telemetry counter needs to know which service emitted it. A background job needs its own identity for logging. An inbound request and an outbound RPC call both need to know which protocol/channel they're on, what verb they're carrying, and how to trace them across hops. All of these are the same small set of questions, asked from different places: who is this, what kind of action is it, how do I follow it across a system? Most codebases answer each one separately per subsystem, and answer it differently again for the client side vs. the server side. kiit-call is that one shared vocabulary, used identically whether a call is arriving (kiit-requests) or being made (kiit-rpc), instead of reinvented and re-translated at every boundary.

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

**`Source` classifies which protocol/channel a call arrived on**, with a named `Other` case for anything not built in:

```kotlin
import kiit.call.Source

Source.parse("api")      // Source.API
Source.parse("webhook")  // Source.Other("webhook")
```

**`Verb`/`Version`/`Trace` describe the call itself**, shared vocabulary between an inbound request (kiit-requests' `Request`) and an outbound one (kiit-rpc's `RpcRequest`):

```kotlin
import kiit.call.Trace
import kiit.call.Verb
import kiit.call.Version

val verb = Verb.Create                          // protocol-neutral, not HTTP-shaped
val version = Version(api = "1", action = null)  // API-level version, optional action override
val trace = Trace(traceId = "abc-123")           // W3C Trace Context shape, carried through as-is
```

**`Content` is typed byte content**, for a file/binary request or response value that needs to carry its format along with it:

```kotlin
import kiit.call.Contents

val json = Contents.json("""{"id":1}""")
println(json.tpe.http)  // application/json
```

See [`samples/sample-kotlin`](./samples/sample-kotlin) for a runnable end-to-end example.

## Concepts

| Term | What it is |
|---|---|
| **`Identity`** | `company.area.service.agent.env.instance` structural identifier for a service/component. Every field lands in `name`/`full`/`id`, dot-joined, so it's stable and log-friendly. |
| **`Agent`** | What kind of thing has this identity: `App`, `CLI`, `Web`, `API`, `Bot`, `Job`, `Cmd`, `Svc`, `Test`. A closed set, a real enum, no runtime-extensible case. |
| **`Source`** | The protocol/channel a call arrived on, or targets for an outbound call: `API`, `CLI`, `Web`, `Queue`, `Bot`, and others, or `Other(name)` for anything not built in. |
| **`About`** | Human-readable app description (name, desc, url, contact, tags), with `toId()` converting it into the equivalent `Identity`. |
| **`Verb`** | A protocol-neutral CRUD-ish verb (`Create`, `Get`, `Query`, `Update`, `Patch`, `Delete`, `Execute`), deliberately not HTTP-shaped. Per-protocol rendering (`Verb.Create` -> HTTP `POST`) is a host/adapter concern. |
| **`Version`** | A call's API-level version, plus an optional action-level override. |
| **`Trace`** | Distributed tracing context (W3C Trace Context shape: `traceId`/`parentSpanId`/`sampled`), carried through faithfully but never created or managed by kiit-call itself. |
| **`Content`** (`ContentText`/`ContentData`/`ContentFile`) | Typed byte content with a `ContentType` attached. `ContentText` for text with a known string form, `ContentData` for bytes without a name, `ContentFile` for a named file. All three carry correct `ByteArray` equality (structural, not reference). |
| **`ContentType`**/**`ContentTypes`** | A MIME type + file extension pair (`ContentType`), and a table of common ones (`ContentTypes.Json`, `.Png`, `.Pdf`, ...). `ContentType.parse(ext)` looks one up by extension. |
| **`ContentMulti`** | Bundles several named `ContentText`/`ContentFile` values together, e.g. a full multipart form submission. |
| **`Contents`** | Factory functions (`.json()`, `.csv()`, `.text()`, ...) and `toText(content)` for reading any `Content` back as a string. |

`Identity.env` is a plain `String`, not a typed enum. kiit-call has no dependency on the environment-selection module (`kiit-conf-envs`), since `Identity` is needed well beyond env-aware bootstrap code, so callers pass whatever env label they're already using.

## Usage

**Good fit if:**
1. You want one consistent "who/what is this" identifier reused across caching, telemetry, jobs, requests, and anywhere else that needs to attribute something to a service.
2. You're building a protocol adapter and need a `Source` classification that isn't tied to any one transport's own vocabulary.
3. You want a machine identifier (`Identity`) and a human-readable description (`About`) that convert cleanly between each other, rather than maintaining both separately.
4. You have both an inbound request shape and an outbound call shape (kiit-requests and kiit-rpc, or your own equivalents) and want them to share `Verb`/`Version`/`Trace`/`Identity`/`Content` directly, with no translation layer between the two.

**Probably not necessary if:**
1. You only have one service and don't log, cache, or trace anything by identity, a hardcoded string is simpler in that case.
2. You only have one side (just a server, or just a client) and don't need the shared vocabulary a two-sided design benefits from.

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
