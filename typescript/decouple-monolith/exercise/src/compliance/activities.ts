// [GIVEN] Compliance team Activity.
//
// In the monolith, paymentProcessingWorkflow calls this Activity directly — which means
// Compliance's code runs inside the Payments Worker process. After decoupling, the
// Activity stays here but is called from the Compliance Worker, inside the Workflow that
// the Nexus handler starts.
import { checkCompliance as runCheck } from './checker';
import { ComplianceRequest, ComplianceResult } from '../shared/types';

export async function checkCompliance(request: ComplianceRequest): Promise<ComplianceResult> {
  return runCheck(request);
}
