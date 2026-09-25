package sample

import kiit.call.About
import kiit.call.Agent
import kiit.call.ContentFile
import kiit.call.Contents
import kiit.call.ContentTypes
import kiit.call.Identity
import kiit.call.Source
import kiit.call.Trace
import kiit.call.Verb
import kiit.call.Version

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

/**
 * Verb/Version/Trace describe the call itself, shared between an inbound request
 * (kiit-requests' Request) and an outbound one (kiit-rpc's RpcRequest).
 */
fun callShapeExample() {
    val verb = Verb.Create
    val version = Version(api = "1")
    val trace = Trace(traceId = "abc-123", parentSpanId = "span-1")

    println("verb=$verb")
    println("version=${version.api}, action override=${version.action}")
    println("trace=${trace.traceId}, sampled=${trace.sampled}")
}

/** Content carries typed byte content, e.g. a request/response body or an attached file. */
fun contentExample() {
    val json = Contents.json("""{"id":1}""")
    println("json content-type=${json.tpe.http}, raw=${Contents.toText(json)}")

    val file = ContentFile("photo.png", byteArrayOf(1, 2, 3), null, ContentTypes.Png)
    println("file name=${file.name}, size=${file.size}, type=${file.tpe.http}")
}

fun main() {
    identityExample()
    cloneExample()
    aboutExample()
    sourceExample()
    callShapeExample()
    contentExample()
}
