# kiit-service-id — Build & Publish Guide

All Gradle commands below are run from the **repository root**.

---

## Install

### Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| JDK  | 17+     | `java -version` to verify |
| Android SDK | any | Required for `androidTarget` compilation |
| GPG  | 2.x     | `gpg --version`; must have the dev.kiit secret key imported |
| Node / npm | 24+ | Needed for `ports/kiit-service-id-ts`, `scripts/publish-npm.sh`, and `samples/sample-ts` — not for the Kotlin build itself |

Import the signing key if not already present:
```bash
gpg --import dev.kiit.seckey.asc
# Verify
gpg --list-secret-keys --keyid-format LONG
```

> **Why this import step matters:** `kiit-service-id/build.gradle.kts`'s
> `signing { useGpgCmd() }` doesn't reference `dev.kiit.seckey.asc` (or any file) directly — it tells
> Gradle's signing plugin to shell out to the external `gpg` binary on your `PATH` instead of using
> its default in-memory PGP implementation. That external `gpg` process reads from your local
> keyring (`~/.gnupg/`) and selects/unlocks the key via the `signing.gnupg.keyName` /
> `signing.gnupg.passphrase` properties (see [Setup](#setup) below). So the import above is what
> actually makes the key available — Gradle itself never touches the raw key material. This is
> also why `useGpgCmd()` is required instead of `useInMemoryPgpKeys` — see the
> ["Could not read PGP secret key"](#could-not-read-pgp-secret-key) FAQ entry.

---

## Setup

Choose **one** of the two approaches below. Both are equivalent — pick whichever fits your workflow.

### Option A — `~/.gradle/gradle.properties` (recommended for local dev)

Add the following to `~/.gradle/gradle.properties` (create the file if it does not exist):

```properties
# Maven Central credentials (portal token — not your account password)
mavenCentralUsername=<portal-token-username>
mavenCentralPassword=<portal-token-password>

# GPG signing via system keyring
signing.gnupg.keyName=<full-key-id>
signing.gnupg.passphrase=<passphrase>
```

With this in place, publishing commands need no extra flags:
```bash
./gradlew :kiit-service-id:publishAndReleaseToMavenCentral
```

### Option B — Shell environment variables (recommended for CI / scripted runs)

Export these variables in your shell profile (e.g. `~/.zshrc`) or in your CI secrets:

| Shell variable      | Gradle property             |
|---------------------|-----------------------------|
| `KIIT_MAVEN_USER`   | `mavenCentralUsername`      |
| `KIIT_MAVEN_PSWD`   | `mavenCentralPassword`      |
| `KIIT_MAVEN_GPGNAME`| `signing.gnupg.keyName`     |
| `KIIT_MAVEN_GPGPASS`| `signing.gnupg.passphrase`  |

Pass them as `-P` flags because dots in the property names are not valid bash variable names:

```bash
./gradlew :kiit-service-id:publishAndReleaseToMavenCentral \
    -Psigning.gnupg.keyName=$KIIT_MAVEN_GPGNAME \
    -Psigning.gnupg.passphrase=$KIIT_MAVEN_GPGPASS \
    -PmavenCentralUsername=$KIIT_MAVEN_USER \
    -PmavenCentralPassword=$KIIT_MAVEN_PSWD
```

---

## CI — GitHub Actions

Three workflows live under [`.github/workflows`](.github/workflows):

| Workflow | File | Trigger | What it does |
|----------|------|---------|---------------|
| CI | `ci.yml` | Every PR into `main` | `ktlintCheck`, `detekt`, `jvmTest` on `ubuntu-latest`; iOS simulator tests on `macos-latest`. |
| Release | `release.yml` | Manual (`workflow_dispatch`) | Builds, tests, publishes to Maven Central, tags, and cuts a GitHub release. Runs on `macos-latest` — required to build/sign the iOS targets. |
| Release npm | `release-npm.yml` | Manual (`workflow_dispatch`) | Builds, tests, publishes `@kiitdev/service-id` to npm, tags, and cuts a GitHub release. Independent version/cadence from the Kotlin release above. |

CI runners are ephemeral — there is no persistent GPG keyring. The secret key must be imported at the start of every release run.

### 1. Repository secrets

Add these five secrets under `Settings → Secrets and variables → Actions`:

| Secret name           | Value |
|-----------------------|-------|
| `KIIT_GPG_SECRET_KEY` | Base64-encoded GPG secret key (see below) |
| `KIIT_MAVEN_GPGNAME`  | GPG key ID |
| `KIIT_MAVEN_GPGPASS`  | GPG passphrase |
| `KIIT_MAVEN_USER`     | Maven Central portal token username |
| `KIIT_MAVEN_PSWD`     | Maven Central portal token password |

Encode your secret key for the `KIIT_GPG_SECRET_KEY` secret (run locally):
```bash
gpg --armor --export-secret-keys <your-key-id> | base64 | pbcopy
```

`release-npm.yml` needs one additional secret:

| Secret name      | Value |
|------------------|-------|
| `KIIT_NPM_TOKEN` | An npm [automation token](https://docs.npmjs.com/creating-and-viewing-access-tokens) with publish access to `@kiitdev/service-id` |

### 2. Cutting a release

Releases are **not** triggered by pushing a tag — `release.yml` creates the tag itself, from the
version already in Gradle:

1. Bump `libraryVersion` in [`kiit-service-id/build.gradle.kts`](./kiit-service-id/build.gradle.kts)
   (see the FAQ entry below) and merge that change to `main`.
2. From the GitHub Actions tab, run the **Release** workflow (`workflow_dispatch`, no inputs).
3. It reads the version via `./gradlew :kiit-service-id:printVersion`, verifies a tag for that
   version doesn't already exist, runs the same lint/test gate as CI, publishes to Maven Central,
   then pushes tag `v<version>` and creates a GitHub release with auto-generated notes
   (`gh release create --generate-notes`) covering everything since the previous release.

A failed publish never leaves behind a tag or a release — tagging and the GitHub release both
happen only after `publishAndReleaseToMavenCentral` succeeds.

### 3. GPG pinentry (if import or signing hangs / fails)

Headless runners have no terminal, so anything that makes `gpg-agent` try to spawn an interactive
or GUI pinentry (a prompt for a passphrase, or to protect newly-imported secret key material)
fails outright — on macOS this shows up as `error sending to agent: Inappropriate ioctl for
device` during import; on Linux it more often just hangs. The release workflow's "Import GPG key"
step should force loopback pinentry mode before importing:

```bash
mkdir -p ~/.gnupg
echo "allow-loopback-pinentry" >> ~/.gnupg/gpg-agent.conf
gpgconf --kill gpg-agent
gpg --batch --yes --pinentry-mode loopback --import ...
```

If you hit the same class of error locally (not in CI), add the equivalent to `~/.gnupg/gpg.conf` and `~/.gnupg/gpg-agent.conf`:

```bash
echo "pinentry-mode loopback" >> ~/.gnupg/gpg.conf
echo "allow-loopback-pinentry" >> ~/.gnupg/gpg-agent.conf
gpgconf --kill gpg-agent
```

Gradle itself already passes `--batch --pinentry-mode loopback` automatically when it shells out to `gpg` for signing, so this is normally only needed for the raw `gpg --import` step, not the actual signing step.

---

## Build

```bash
# Stop the Gradle daemon (useful after changing env vars or upgrading Gradle)
./gradlew --stop

# Clean build outputs
./gradlew :kiit-service-id:clean

# Compile all targets (JVM, Android, iOS)
./gradlew :kiit-service-id:build

# Compile only — no tests
./gradlew :kiit-service-id:assemble

# Run the Kotlin sample app
./gradlew :samples:sample-kotlin:run

# Run the Swift sample app (macOS only, needs Xcode + an iOS Simulator runtime)
./gradlew :kiit-service-id:linkDebugFrameworkIosSimulatorArm64
cd samples/sample-swift && ./run.sh
```

---

## Test (local)

```bash
# JVM tests (fastest — runs on the local JVM)
./gradlew :kiit-service-id:jvmTest

# All platform tests
./gradlew :kiit-service-id:allTests

# Publish to Maven Local (~/.m2) for integration testing against other modules
./gradlew :kiit-service-id:publishToMavenLocal
```

Maven Local artifacts are saved to:
```
~/.m2/repository/dev/kiit/kiit-service-id/
```

---

## Publish

Published artifacts (once a version has actually gone through `publishAndReleaseToMavenCentral`):
[central.sonatype.com/artifact/dev.kiit/kiit-service-id](https://central.sonatype.com/artifact/dev.kiit/kiit-service-id)

### Publish to Maven Local

No credentials required.

```bash
./gradlew :kiit-service-id:publishToMavenLocal
```

### Publish to Maven Central — Option A (gradle.properties)

Requires `~/.gradle/gradle.properties` populated per the Setup section above.

```bash
./gradlew :kiit-service-id:publishAndReleaseToMavenCentral
```

### Publish to Maven Central — Option B (env vars)

```bash
./gradlew :kiit-service-id:publishAndReleaseToMavenCentral \
    -Psigning.gnupg.keyName=$KIIT_MAVEN_GPGNAME \
    -Psigning.gnupg.passphrase=$KIIT_MAVEN_GPGPASS \
    -PmavenCentralUsername=$KIIT_MAVEN_USER \
    -PmavenCentralPassword=$KIIT_MAVEN_PSWD
```

### Sign artifacts only (dry-run check)

```bash
./gradlew :kiit-service-id:signKotlinMultiplatformPublication
```

---

## Publish — npm (`@kiitdev/service-id`)

Published artifacts: [npmjs.com/package/@kiitdev/service-id](https://www.npmjs.com/package/@kiitdev/service-id)

`@kiitdev/service-id` is published from the native TypeScript port (`ports/kiit-service-id-ts`), not
generated from the Kotlin build — it has its own version, tracked in
`ports/kiit-service-id-ts/package.json`, independent of the Kotlin library's `libraryVersion`.

### Manual, local publish

```bash
npm login          # one-time, if not already authenticated
./scripts/publish-npm.sh
```

The script installs dependencies, runs `typecheck`/`test`/`build`, previews exactly what would be
published via `npm pack --dry-run`, asks for confirmation, then runs `npm publish --access public`
from `ports/kiit-service-id-ts` — the `--access public` flag is required the first time a new scoped
(`@kiit/...`) package is published, since scoped packages default to private on npm otherwise.

### CI, via the Release npm workflow

1. Bump `version` in [`ports/kiit-service-id-ts/package.json`](ports/kiit-service-id-ts/package.json)
   and merge that change to `main`.
2. From the GitHub Actions tab, run the **Release npm** workflow (`workflow_dispatch`, no inputs).
3. It reads the version from `package.json`, verifies a tag for that version doesn't already
   exist, runs typecheck/tests, publishes to npm (`prepublishOnly` also re-runs
   typecheck/test/build), then pushes tag `npm-v<version>` and creates a GitHub release with
   auto-generated notes.

A failed publish never leaves behind a tag or a release — tagging and the GitHub release both
happen only after `npm publish` succeeds.

---

## FAQ

### "Cannot perform signing task — has no configured signatory"

The signing plugin could not find the key. Check in order:

1. Verify the key is in the GPG keyring:
   ```bash
   gpg --list-secret-keys --keyid-format LONG
   ```
2. Confirm `signing.gnupg.keyName` matches the full key ID shown above.
3. If using `~/.gradle/gradle.properties`, make sure the file is saved and the Gradle daemon is restarted (`./gradlew --stop`).
4. Do **not** use `signAllPublications()` inside `mavenPublishing {}` — it uses `providers.gradleProperty` internally which does not reliably read `~/.gradle/gradle.properties` (Gradle issue #23572). Use the `signing {}` block directly with `useGpgCmd()`.

### "GPG prompts for the wrong key's passphrase"

`signing.gnupg.keyName` is pointing at the wrong key ID. Run `gpg --list-secret-keys --keyid-format LONG`, find the `dev.kiit` key, and update the property to its full fingerprint.

### "Could not read PGP secret key"

Do not use in-memory key signing (`useInMemoryPgpKeys`). The stripped base64 format is fragile and Bouncycastle can silently produce a null signatory. Use `useGpgCmd()` instead.

### Maven Central portal returns 401

The `mavenCentralUsername` and `mavenCentralPassword` are **portal token** credentials, not your account login. Generate a token at [central.sonatype.com](https://central.sonatype.com) under Account → Generate User Token.

### How do I bump the version?

Edit the `libraryVersion` val near the top of the `mavenPublishing {}` block in
`kiit-service-id/build.gradle.kts`:
```kotlin
val libraryVersion = "0.9.0"   // ← bump here
```
This single value feeds both the published Maven coordinates and the `printVersion` task the release workflow reads to tag and name the GitHub release — see [CI — GitHub Actions](#ci--github-actions) above. The npm package's version is tracked separately, in
[`ports/kiit-service-id-ts/package.json`](ports/kiit-service-id-ts/package.json) (see the npm publish
section above).
