package kiit.identity

/**
 * The kind of runnable/executable app or service an [Identity] represents. A closed, fixed set,
 * not open for extension, so a real `enum class` rather than a sealed hierarchy: there's no
 * runtime-supplied "other" case to support here.
 */
enum class Agent {
    App,
    CLI,
    Web,
    API,
    Bot,
    Job,
    Worker,
    Service,
    Test,
}
