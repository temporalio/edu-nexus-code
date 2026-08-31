// The shared contract between the Payments and Compliance teams. Like an OpenAPI spec,
// but durable. Payments builds a client from it; Compliance implements a handler for it.
//
// Both teams import THIS file. It is the only thing they share.
//
// Looking things up during this lab: start at
//   https://docs.temporal.io/develop/typescript/nexus/feature-guide
// Faster still, the "Ask AI" button on https://docs.temporal.io answers in plain
// language. Try "how do I define a Nexus Service contract in TypeScript?" Get used to
// it now — the later TODOs are harder.
//
// You may see `@experimental` when you hover `nexus.service` or `nexus.operation`. That
// is `nexus-rpc`, the contract package, which is versioned separately and still pre-1.0.
// Nexus itself is GA in the Temporal TypeScript SDK as of v1.23.0.
import * as nexus from 'nexus-rpc';
import { ComplianceRequest, ComplianceResult, ReviewRequest } from './types';

// ── TODO 1 ────────────────────────────────────────────────────────────────────────
// Declare the two Operations this Service offers. Each one is a name paired with
// nexus.operation<Input, Output>(), which carries no implementation — only the types
// that both teams agree on.
//
//   checkCompliance   ComplianceRequest -> ComplianceResult
//   submitReview      ReviewRequest     -> ComplianceResult
//
// Add them one at a time and watch what happens: the moment you declare an Operation
// here, TypeScript demands a matching handler in compliance/nexus-handler.ts. The
// contract is enforced at compile time, which is the whole point of sharing it.
export const complianceService = nexus.service('ComplianceNexusService', {
  // your Operations go here
});
