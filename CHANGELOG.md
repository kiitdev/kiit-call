# Changelog

All notable changes to kiit-identity are documented here. Format follows
[Keep a Changelog](https://keepachangelog.com/), versions follow
[Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added
- Extracted from the Kiit monorepo as its own standalone module.
- `@kiitdev/identity`, a native TypeScript port of `Identity` and `Agent` in `ports/kiit-identity-ts`,
  with a sample in `samples/sample-ts`. Equality in the port compares `id`, and `Agent` has no int
  `value`, see the port's README.

### Changed
- Renamed from `kiit-call` to `kiit-identity`. Once `Verb`/`Version`/`Trace`/`Source`/`Content`
  moved out to `kiit-requests` (see Removed), everything left in this module was about a
  service's identity, not a call, so the name follows.
- Redesigned `Identity` to align with kiit-codes' `Status` (`origin`/`scope`, `:`-delimited
  accessors). `company`/`area`/`service` are replaced by `origin`/`scope` (a single free-form,
  dot-structured field). `desc` is renamed `about`. A new optional `uri` field was added.
  `name`/`full`/`id` are `:`-joined instead of `.`-joined, and a new `install` accessor sits
  between `full` and `id` (`origin:scope:agent:env:version`); `idWithTags` is removed. Every
  accessor except `instance` is lowercased. `Identity` now implements a new `IIdentity` interface
  (a plain data contract, no derived accessors of its own) for consumers who want a custom shape.
  `Agent`'s int `value` is dropped, `Svc` is renamed `Service`, and `Worker` is added. Since this
  module isn't stable yet (pre-1.0, published but with no real consumers), this ships as a clean
  break rather than a deprecation path.

### Removed
- `Verb`/`Version`/`Trace`/`Source`/`Content`/`ContentType` moved to `kiit-requests`, which now
  owns the whole call-shape domain (`Request`/`ServerRequest`/`ClientRequest`).
- `About` moved out too, a future `kiit-app` module's concern (whole-application description),
  a different granularity than `Identity`'s per-service/component scope. `kiit-identity` now
  holds just `Identity` and `Agent`. (`Identity` later gained its own, much smaller `about: String`
  field, see Changed, unrelated to the removed `About` type.)
