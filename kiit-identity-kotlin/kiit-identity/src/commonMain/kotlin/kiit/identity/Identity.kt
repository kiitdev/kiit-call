@file:OptIn(ExperimentalUuidApi::class)

package kiit.identity

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** ":" — the delimiter [Identity]'s `path`/`name`/`full`/`install`/`id` are built from. */
const val IDENTITY_DELIMITER = ":"

/**
 * Contract for anything that identifies a caller or service. Deliberately a plain data shape,
 * nothing more: no `path`/`id`-style derived properties here, and no default implementations to
 * inherit. [Identity] is the canonical, constructible way to get that behavior. Implement this
 * directly only when you need a custom storage shape (wrapping an existing domain object, say);
 * you still won't get `path`/`name`/`full`/`install`/`id` for it, since those belong to
 * [Identity], not to every possible shape that happens to carry these nine fields.
 *
 * The fields build up from broad to specific:
 * 1. [origin] and [scope] say who this is.
 * 2. [agent] says what kind of thing it is.
 * 3. [env] and [version] say where it's running and which build.
 * 4. [instance] tells apart multiple running copies of the same version.
 *
 * [about] and [uri] are side information, not part of any derived identifier.
 */
interface IIdentity {
    /**
     * Domain-like label for who owns this identity, e.g. `"acme.com"` or `"acme"`.
     * 1. Same convention as `Status.origin` in kiit-codes: a real domain or any other stable id
     * 2. This is not validated, and not guaranteed unique unless it's an actual domain.
     */
    val origin: String

    /**
     * Free-form, consumer-defined label for where in [origin] this lives, e.g. "accounts.signup"
     * 1. Same convention as `Status.scope`: dots are fine for expressing hierarchy
     * 2. kiit-identity never parses or enforces the internal shape
     * 3. This shouldn't contain `:` (reserved, see [IDENTITY_DELIMITER]).
     */
    val scope: String

    /** The kind of runnable app or service this is. See [Agent]. */
    val agent: Agent

    /** dev | qat | pro, environment. A plain string, so callers pass whatever env label they already use. */
    val env: String

    /** Short, human-readable description of what this is or does. Empty string means unset. */
    val about: String

    /** The version running here. Defaults to `"latest"`. */
    val version: String

    /**
     * Id of this specific running instance, for telling apart multiple instances of the same
     * [version] (a redeploy, a pod, a worker in a pool). Random by default. Left as-is in
     * [Identity.id], unlike every other field here: its whole job is uniqueness, and lowercasing
     * it could make two different instances collide.
     */
    val instance: String

    /** Freeform metadata. Not part of any derived identifier. */
    val tags: List<String>

    /**
     * Optional reference to this instance itself, e.g. a hostname or service-discovery address.
     * Unique per environment. Not part of any derived identifier.
     */
    val uri: String?
}

/**
 * Identity used to identify services/components.
 *
 * ```
 * path    = origin:scope
 * name    = path:agent                = origin:scope:agent
 * full    = name:env                  = origin:scope:agent:env
 * install = full:version              = origin:scope:agent:env:version
 * id      = install:instance          = origin:scope:agent:env:version:instance
 * ```
 *
 * Every segment except [instance] is lowercased, so two identities that only differ by casing in
 * [origin]/[scope]/[agent]/[env]/[version] still produce the same [path]/[name]/[full]/[install]/
 * [id]. [instance] is left exactly as given, since folding its case could make two genuinely
 * different instances collide.
 *
 * Immutable. [newInstance]/[with] return a new [Identity] rather than mutating this one.
 */
data class Identity(
    override val origin: String,
    override val scope: String,
    override val agent: Agent,
    override val env: String,
    override val about: String = "",
    override val version: String = "latest",
    override val instance: String = Uuid.random().toString(),
    override val tags: List<String> = listOf(),
    override val uri: String? = null,
) : IIdentity {
    /** `origin:scope`, lowercased. */
    val path: String get() = "${origin.lowercase()}$IDENTITY_DELIMITER${scope.lowercase()}"

    /** [path] plus [agent], lowercased: `origin:scope:agent`. The same component, any environment. */
    val name: String get() = "$path$IDENTITY_DELIMITER${agent.name.lowercase()}"

    /** [name] plus [env], lowercased: `origin:scope:agent:env`. */
    val full: String get() = "$name$IDENTITY_DELIMITER${env.lowercase()}"

    /**
     * [full] plus [version], lowercased: `origin:scope:agent:env:version`. Named for what it is:
     * a specific version installed into a specific environment.
     */
    val install: String get() = "$full$IDENTITY_DELIMITER${version.lowercase()}"

    /** [install] plus [instance], not lowercased: `origin:scope:agent:env:version:instance`.
     * Unique per running instance. */
    val id: String get() = "$install$IDENTITY_DELIMITER$instance"

    /** Same identity with a new random instance id. */
    fun newInstance(): Identity = this.copy(instance = Uuid.random().toString())

    /** Same identity with the given instance id (random if null) and tags. */
    fun with(
        inst: String?,
        tags: List<String>,
    ): Identity {
        return this.copy(instance = inst ?: Uuid.random().toString(), tags = tags)
    }

    companion object {
        val empty = Identity(origin = "", scope = "empty", agent = Agent.Test, env = "empty")

        fun app(
            origin: String,
            scope: String,
            env: String = "dev",
        ): Identity = of(origin, scope, Agent.App, env)

        fun api(
            origin: String,
            scope: String,
            env: String = "dev",
        ): Identity = of(origin, scope, Agent.API, env)

        fun cli(
            origin: String,
            scope: String,
            env: String = "dev",
        ): Identity = of(origin, scope, Agent.CLI, env)

        fun job(
            origin: String,
            scope: String,
            env: String = "dev",
        ): Identity = of(origin, scope, Agent.Job, env)

        /** A test identity: scope is `"tests.<name>"`, agent is [Agent.Test], env is `"dev"`. */
        fun test(
            origin: String,
            name: String,
        ): Identity = of(origin, "tests.$name", Agent.Test, "dev")

        /** Builds an [Identity] from names, normalizing [origin]/[scope] and lowercasing [env]. */
        fun of(
            origin: String,
            scope: String,
            agent: Agent,
            env: String = "dev",
            version: String? = null,
            about: String? = null,
            instance: String? = null,
            uri: String? = null,
        ): Identity {
            return Identity(
                origin.toIdent(),
                scope.toIdent(),
                agent,
                env.lowercase(),
                about = about ?: "",
                version = version ?: "latest",
                instance = instance ?: Uuid.random().toString(),
                uri = uri,
            )
        }
    }
}

/**
 * Normalizes a name into an identifier: lowercased, trimmed, keeping only letters, digits, `-`,
 * `_`, `.`, and space, with spaces turned into `_`. Falls back to `_` if nothing survives. Dots
 * are kept so a [IIdentity.scope] like `"accounts.signup"` survives normalization intact.
 */
internal fun String.toIdent(): String {
    val trimmed = this.trim().lowercase()
    val filtered = trimmed.filter { it.isDigit() || it.isLetter() || it == '-' || it == '_' || it == '.' || it == ' ' }
    val cleaned = filtered.ifBlank { "_" }
    return cleaned.replace(' ', '_')
}
