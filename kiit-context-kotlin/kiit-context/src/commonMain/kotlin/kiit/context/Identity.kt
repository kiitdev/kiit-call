@file:OptIn(ExperimentalUuidApi::class)

package kiit.context

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Identity used to identify services/components.
 *
 * form = company.area.service.agent.env.version.instance
 * name = app1.accounts.signup.alerts.job.qat
 * vers = app1.accounts.signup.alerts.job.qat.1_0_2_3
 * full = app1.accounts.signup.alerts.job.qat.1_0_2_3.4a3b300b-d0ac-4776-8a9c-31aa75e412b3
 *
 * @param company app1 | jetbrains, company name/origin
 * @param area area | dept | org, logical group
 * @param service user1 | job | svc, distinguishes multiple agents/users
 * @param agent the type of service, api | app | job
 * @param env dev | qat | pro, environment. A plain String, not an EnvMode type: kiit-context has
 * no dependency on kiit-conf-envs (Identity is needed well beyond env-aware bootstrap code), so
 * callers pass whatever env label they're already using.
 * @param instance id of the instance, for multiple instances of a service
 */
data class Identity(
    val company: String,
    val area: String,
    val service: String,
    val agent: Agent,
    val env: String,
    val instance: String = Uuid.random().toString(),
    val version: String = "latest",
    val desc: String = "",
    val tags: List<String> = listOf(),
) {
    private val tagged = tags.joinToString()

    /**
     * Enforced naming convention for a component's simple name:
     * {company}.{area}.{service}.{agent}, e.g. app1.signup.alerts.job
     */
    val name: String = "$company.$area.$service.${agent.name.lowercase()}"

    /**
     * Enforced naming convention for the full name with env and version:
     * {company}.{area}.{service}.{agent}.{env}.{version}
     */
    val full: String = "$name.${env.lowercase()}.${version.replace(".", "_")}"

    /** Includes the instance id, e.g. app1.signup.alerts.job.qat.4a3b300b-... */
    val id: String = "$full.$instance"

    /** Includes the instance id and any tags. */
    val idWithTags: String = "$full.$instance" + if (tagged.isEmpty()) "" else ".$tagged"

    fun newInstance(): Identity = this.copy(instance = Uuid.random().toString())

    fun with(inst: String?, tags: List<String>): Identity {
        return this.copy(instance = inst ?: Uuid.random().toString(), tags = tags)
    }

    companion object {
        val empty = Identity("", "empty", "empty", Agent.Test, "empty")

        fun app(
            company: String,
            area: String,
            service: String,
            env: String = "dev"
        ): Identity = of(company, area, service, Agent.App, env)

        fun api(
            company: String,
            area: String,
            service: String,
            env: String = "dev"
        ): Identity = of(company, area, service, Agent.API, env)

        fun cli(
            company: String,
            area: String,
            service: String,
            env: String = "dev"
        ): Identity = of(company, area, service, Agent.CLI, env)

        fun job(
            company: String,
            area: String,
            service: String,
            env: String = "dev"
        ): Identity = of(company, area, service, Agent.Job, env)

        fun test(company: String, name: String): Identity = of(company, "tests", name, Agent.Test, "dev")

        fun of(
            company: String,
            area: String,
            service: String,
            agent: Agent,
            env: String = "dev",
            version: String? = null,
            desc: String? = null,
            instance: String? = null,
        ): Identity {
            return Identity(
                company.toIdent(),
                area.toIdent(),
                service.toIdent(),
                agent,
                env.lowercase(),
                instance = instance ?: Uuid.random().toString(),
                version = version ?: "latest",
                desc = desc ?: "",
            )
        }
    }
}

/**
 * Normalizes a name into an identifier: lowercased, trimmed, keeping only letters, digits,
 * `-`, `_`, and space, with spaces turned into `_`. Falls back to `_` if nothing survives.
 */
internal fun String.toIdent(lowerCase: Boolean = true): String {
    val trimmed = if (lowerCase) this.trim().lowercase() else this.trim()
    val filtered = trimmed.filter { it.isDigit() || it.isLetter() || it == '-' || it == '_' || it == ' ' }
    val cleaned = filtered.ifBlank { "_" }
    return cleaned.replace(' ', '_')
}
