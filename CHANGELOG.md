# Changelog

All notable changes to kiit-call are documented here. Format follows
[Keep a Changelog](https://keepachangelog.com/), versions follow
[Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added
- Extracted from the Kiit monorepo as its own standalone module.

### Removed
- `Verb`/`Version`/`Trace`/`Source`/`Content`/`ContentType` moved to `kiit-requests`, which now
  owns the whole call-shape domain (`Request`/`ServerRequest`/`ClientRequest`). kiit-call keeps
  `Identity`/`Agent`/`About` — service self-description, independent of any call.
