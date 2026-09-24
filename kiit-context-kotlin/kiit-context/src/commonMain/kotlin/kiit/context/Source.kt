package kiit.context

/** String constants for [Source], for hosts/config that want the raw id rather than the type. */
object Sources {
    /** Reference to a parent value, e.g. if set on an action, refers to its parent API. */
    const val PARENT = "@parent"

    /** Enables all protocols. */
    const val ALL = "*"

    /** HTTP for standard web/REST requests. */
    const val API = "api"

    /** Automation requests. */
    const val AUTO = "auto"

    /** Bots/chat. */
    const val BOT = "bot"

    /** Command line interactive. */
    const val CLI = "cli"

    /** Commands. */
    const val CMD = "cmd"

    /** File-based, e.g. requests processed from a file for automation. */
    const val FILE = "file"

    /** Queue-based, requests saved and processed from a queue. */
    const val QUEUE = "queue"

    /** Stream-based. */
    const val STREAM = "stream"

    /** HTTP for standard web/REST requests. */
    const val WEB = "web"
}

/** The protocol/channel a call arrived on, or is targeting for an outbound call. */
sealed class Source(val id: String) {
    object All : Source(Sources.ALL)

    object Parent : Source(Sources.PARENT)

    object API : Source(Sources.API)

    object Auto : Source(Sources.AUTO)

    object Bot : Source(Sources.BOT)

    object CLI : Source(Sources.CLI)

    object Cmd : Source(Sources.CMD)

    object File : Source(Sources.FILE)

    object Queue : Source(Sources.QUEUE)

    object Stream : Source(Sources.STREAM)

    object Web : Source(Sources.WEB)

    data class Other(val name: String) : Source("other")

    fun isParentReference(): Boolean = this.id == Sources.PARENT

    fun orElse(other: Source): Source = if (isParentReference()) other else this

    companion object {
        fun parse(name: String): Source {
            return when (name.lowercase()) {
                Parent.id -> Parent
                All.id -> All
                "all" -> All
                API.id -> API
                Auto.id -> Auto
                Bot.id -> Bot
                CLI.id -> CLI
                Cmd.id -> Cmd
                File.id -> File
                Queue.id -> Queue
                Stream.id -> Stream
                Web.id -> Web
                else -> Other(name)
            }
        }
    }
}
