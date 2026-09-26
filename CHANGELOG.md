# Changelog

All notable changes to kiit-identity are documented here. Format follows
[Keep a Changelog](https://keepachangelog.com/), versions follow
[Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added
- Extracted from the Kiit monorepo as its own standalone module.

### Changed
- Renamed from `kiit-call` to `kiit-identity`. Once `Verb`/`Version`/`Trace`/`Source`/`Content`
  moved out to `kiit-requests` (see Removed), everything left in this module was about a
  service's identity, not a call, so the name follows.

### Removed
- `Verb`/`Version`/`Trace`/`Source`/`Content`/`ContentType` moved to `kiit-requests`, which now
  owns the whole call-shape domain (`Request`/`ServerRequest`/`ClientRequest`).
- `About` moved out too, a future `kiit-app` module's concern (whole-application description),
  a different granularity than `Identity`'s per-service/component scope. `kiit-identity` now
  holds just `Identity` and `Agent`.
