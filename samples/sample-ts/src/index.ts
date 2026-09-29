/**
 * Mirrors samples/sample-kotlin: build a ServiceId the way a service would at startup, then show
 * each derived accessor and that the type is immutable. Type-checked against the native port with
 * `npm run typecheck`.
 */
import { Criticality, Kind, ServiceId, Tag } from "@kiitdev/identity";

/** Builds a ServiceId the way a service would at startup, then looks at each derived accessor. */
function serviceIdExample(): void {
  const id = ServiceId.api("codehelix", "accounts.signup", "qat");

  console.log(`path=${id.path}`);
  console.log(`name=${id.name}`);
  console.log(`fullName=${id.fullName}`);
  console.log(`install=${id.install}`);
  console.log(`privateId=${id.privateId}`);
  console.log(`externalId=${id.externalId}`);
  console.log(`kind=${id.kind}`);
}

/** ServiceId is immutable. `newInstance`/`with` return a new value rather than mutating. */
function cloneExample(): void {
  const original = ServiceId.job("codehelix", "accounts.signup");
  const tagged = original.with(null, [Tag.Basic("retry"), Tag.Keyed("batch", "42")]);

  console.log(`original tags=[${original.tags.map((t) => t.raw).join(", ")}]`);
  console.log(`tagged tags=[${tagged.tags.map((t) => t.raw).join(", ")}]`);
  console.log(`same instance? ${original.instance === tagged.instance}`);
  console.log(`equal? ${original.equals(tagged)}`);
}

/** Every option `of` takes, including the ones the shortcuts (`api`, `job`, ...) leave out. */
function ofExample(): void {
  const id = ServiceId.of({
    origin: "Code Helix",
    scope: "Accounts Team.Sign Up!",
    kind: Kind.Worker,
    env: "PRO",
    version: "1.0.2",
    about: "Sends the welcome email after signup",
    uri: "worker-7.codehelix.internal",
    criticality: Criticality.High,
    team: "payments-platform",
  });

  console.log(`fullName=${id.fullName}`);
  console.log(`about=${id.about}`);
  console.log(`uri=${id.uri}`);
  console.log(`criticality=${id.criticality}`);
  console.log(`team=${id.team}`);
  console.log(`provenance=${id.provenance}`);
}

serviceIdExample();
cloneExample();
ofExample();
