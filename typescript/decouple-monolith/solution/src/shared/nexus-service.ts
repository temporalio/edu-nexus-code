// The shared contract between the Payments and Compliance teams. Like an OpenAPI spec,
// but durable. Payments builds a client from it; Compliance implements a handler for it.
//
// Both teams import THIS file. It is the only thing they share.
import * as nexus from 'nexus-rpc';
import { ComplianceRequest, ComplianceResult, ReviewRequest } from './types';

export const complianceService = nexus.service('ComplianceNexusService', {
  /** Slow path. Backed by a Workflow, because a risky payment may need a human. */
  checkCompliance: nexus.operation<ComplianceRequest, ComplianceResult>(),

  /** Fast path. A human decision handed to an already-running compliance check. */
  submitReview: nexus.operation<ReviewRequest, ComplianceResult>(),
});
