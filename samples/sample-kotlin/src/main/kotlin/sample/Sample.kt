package sample

import kiit.serviceid.Criticality
import kiit.serviceid.Kind
import kiit.serviceid.ServiceId
import kiit.serviceid.Tag

/** Builds a ServiceId the way a service would at startup, then looks at each derived accessor. */
fun serviceIdExample() {
    val id = ServiceId.api(origin = "codehelix", scope = "accounts.signup", env = "qat")

    println("path=${id.path}")
    println("name=${id.name}")
    println("fullName=${id.fullName}")
    println("install=${id.install}")
    println("privateId=${id.privateId}")
    println("externalId=${id.externalId}")
    println("kind=${id.kind}")
}

/** ServiceId is immutable. `newInstance`/`with` return a new value rather than mutating. */
fun cloneExample() {
    val original = ServiceId.job("codehelix", "accounts.signup")
    val tagged = original.with(inst = null, tags = listOf(Tag.Basic("retry"), Tag.Keyed("batch", "42")))

    println("original tags=${original.tags}")
    println("tagged tags=${tagged.tags}")
    println("same instance? ${original.instance == tagged.instance}")
}

/** Every option `of` takes, including the ones the shortcuts (`api`, `job`, ...) leave out. */
fun ofExample() {
    val id =
        ServiceId.of(
            origin = "Code Helix",
            scope = "Accounts Team.Sign Up!",
            kind = Kind.Worker,
            env = "PRO",
            version = "1.0.2",
            about = "Sends the welcome email after signup",
            uri = "worker-7.codehelix.internal",
            criticality = Criticality.High,
            team = "payments-platform",
        )

    println("fullName=${id.fullName}")
    println("about=${id.about}")
    println("uri=${id.uri}")
    println("criticality=${id.criticality}")
    println("team=${id.team}")
    println("provenance=${id.provenance}")
}

fun main() {
    serviceIdExample()
    cloneExample()
    ofExample()
}
