/**
 * Mirrors samples/sample-kotlin: build an Identity the way a service would at startup, then show
 * each derived accessor and that the type is immutable. Type-checked against the native port with
 * `npm run typecheck`.
 */
import { Agent, Identity, Tag } from "@kiitdev/identity";

/** Builds an Identity the way a service would at startup, then looks at each derived accessor. */
function identityExample(): void {
  const identity = Identity.api("codehelix", "accounts.signup", "qat");

  console.log(`path=${identity.path}`);
  console.log(`name=${identity.name}`);
  console.log(`full=${identity.full}`);
  console.log(`install=${identity.install}`);
  console.log(`id=${identity.id}`);
  console.log(`agent=${identity.agent}`);
}

/** Identity is immutable. `newInstance`/`with` return a new value rather than mutating. */
function cloneExample(): void {
  const original = Identity.job("codehelix", "accounts.signup");
  const tagged = original.with(null, [Tag.Basic("retry"), Tag.Keyed("batch", "42")]);

  console.log(`original tags=[${original.tags.map((t) => t.raw).join(", ")}]`);
  console.log(`tagged tags=[${tagged.tags.map((t) => t.raw).join(", ")}]`);
  console.log(`same instance? ${original.instance === tagged.instance}`);
  console.log(`equal? ${original.equals(tagged)}`);
}

/** Every option `of` takes, including the ones the shortcuts (`api`, `job`, ...) leave out. */
function ofExample(): void {
  const identity = Identity.of({
    origin: "Code Helix",
    scope: "Accounts Team.Sign Up!",
    agent: Agent.Worker,
    env: "PRO",
    version: "1.0.2",
    about: "Sends the welcome email after signup",
    uri: "worker-7.codehelix.internal",
  });

  console.log(`full=${identity.full}`);
  console.log(`about=${identity.about}`);
  console.log(`uri=${identity.uri}`);
}

identityExample();
cloneExample();
ofExample();
