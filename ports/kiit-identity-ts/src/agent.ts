/**
 * The kind of runnable app or service an Identity represents. A closed, fixed set, so a plain
 * `as const` object plus a derived union type rather than an open string. Member names match
 * Kotlin's `Agent` enum. There's no int value here, nothing in the port reads one.
 */
export const Agent = {
  App: "App",
  CLI: "CLI",
  Web: "Web",
  API: "API",
  Bot: "Bot",
  Job: "Job",
  Worker: "Worker",
  Service: "Service",
  Test: "Test",
} as const;

export type Agent = (typeof Agent)[keyof typeof Agent];
