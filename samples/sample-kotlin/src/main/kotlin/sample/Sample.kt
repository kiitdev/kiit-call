package sample

import kiit.context.About
import kiit.context.Agent
import kiit.context.Identity
import kiit.context.Source

/** Builds an Identity the way a service would at startup, then derives its own logging id. */
fun identityExample() {
    val identity = Identity.api(company = "codehelix", area = "accounts", service = "signup", env = "qat")

    println("name=${identity.name}")
    println("full=${identity.full}")
    println("id=${identity.id}")
}

/** Identity is immutable. `newInstance`/`with` return a new value rather than mutating. */
fun cloneExample() {
    val original = Identity.job("codehelix", "accounts", "signup")
    val tagged = original.with(inst = null, tags = listOf("retry", "batch-42"))

    println("original tags=${original.tags}")
    println("tagged tags=${tagged.tags}")
    println("same instance? ${original.instance == tagged.instance}")
}

/** About is the prose complement to Identity, and converts directly into one. */
fun aboutExample() {
    val about = About.simple(
        company = "codehelix",
        area = "accounts",
        name = "signup",
        desc = "Handles new user signup.",
    )

    println(about.toStringProps())
    println("as identity: ${about.toId().full}")
}

/** Source classifies which protocol/channel a call arrived on. */
fun sourceExample() {
    println("api -> ${Source.parse("api").id}")
    println("cli -> ${Source.parse("cli").id}")
    println("webhook -> ${Source.parse("webhook")}") // falls back to Source.Other("webhook")
    println("App agent value=${Agent.App.value}")
}

fun main() {
    identityExample()
    cloneExample()
    aboutExample()
    sourceExample()
}
