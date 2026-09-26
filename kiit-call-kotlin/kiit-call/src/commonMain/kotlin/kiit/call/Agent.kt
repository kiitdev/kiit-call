package kiit.call

/**
 * The kind of runnable/executable app or service an [Identity] represents. A closed, fixed set,
 * not open for extension, so a real `enum class` rather than a sealed hierarchy: there's no
 * runtime-supplied "other" case to support here.
 */
enum class Agent(val value: Int) {
    App(0),
    CLI(1),
    Web(2),
    API(3),
    Bot(4),
    Job(5),
    Svc(6),
    Test(7),
}
