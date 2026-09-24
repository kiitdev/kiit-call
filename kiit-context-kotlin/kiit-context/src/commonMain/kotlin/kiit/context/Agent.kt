package kiit.context

/**
 * The type of service/component an [Identity] represents. A closed, fixed set, not open for
 * extension, so a real `enum class` rather than a sealed hierarchy: there's no runtime-supplied
 * "other" case to support here, unlike `Source`.
 */
enum class Agent(val value: Int) {
    App(0),
    CLI(1),
    Web(2),
    API(3),
    Bot(4),
    Job(5),
    Cmd(6),
    Svc(7),
    Test(8),
}
