package sample

import kiit.identity.Agent
import kiit.identity.Identity
import kiit.identity.Tag

/** Builds an Identity the way a service would at startup, then looks at each derived accessor. */
fun identityExample() {
    val identity = Identity.api(origin = "codehelix", scope = "accounts.signup", env = "qat")

    println("path=${identity.path}")
    println("name=${identity.name}")
    println("fullName=${identity.fullName}")
    println("install=${identity.install}")
    println("id=${identity.id}")
    println("agent=${identity.agent}")
}

/** Identity is immutable. `newInstance`/`with` return a new value rather than mutating. */
fun cloneExample() {
    val original = Identity.job("codehelix", "accounts.signup")
    val tagged = original.with(inst = null, tags = listOf(Tag.Basic("retry"), Tag.Keyed("batch", "42")))

    println("original tags=${original.tags}")
    println("tagged tags=${tagged.tags}")
    println("same instance? ${original.instance == tagged.instance}")
}

/** Every option `of` takes, including the ones the shortcuts (`api`, `job`, ...) leave out. */
fun ofExample() {
    val identity =
        Identity.of(
            origin = "Code Helix",
            scope = "Accounts Team.Sign Up!",
            agent = Agent.Worker,
            env = "PRO",
            version = "1.0.2",
            about = "Sends the welcome email after signup",
            uri = "worker-7.codehelix.internal",
        )

    println("fullName=${identity.fullName}")
    println("about=${identity.about}")
    println("uri=${identity.uri}")
}

fun main() {
    identityExample()
    cloneExample()
    ofExample()
}
