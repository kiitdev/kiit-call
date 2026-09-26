package sample

import kiit.identity.Identity

/** Builds an Identity the way a service would at startup, then derives its own logging id. */
fun identityExample() {
    val identity = Identity.api(company = "codehelix", area = "accounts", service = "signup", env = "qat")

    println("name=${identity.name}")
    println("full=${identity.full}")
    println("id=${identity.id}")
    println("agent=${identity.agent}")
}

/** Identity is immutable. `newInstance`/`with` return a new value rather than mutating. */
fun cloneExample() {
    val original = Identity.job("codehelix", "accounts", "signup")
    val tagged = original.with(inst = null, tags = listOf("retry", "batch-42"))

    println("original tags=${original.tags}")
    println("tagged tags=${tagged.tags}")
    println("same instance? ${original.instance == tagged.instance}")
}

fun main() {
    identityExample()
    cloneExample()
}
