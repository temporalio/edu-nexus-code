package shared.nexus;

// ═══════════════════════════════════════════════════════════════════
//  TODO 1: Define the Nexus service interface (the shared contract)
// ═══════════════════════════════════════════════════════════════════
//
// This is the shared contract between teams — like an OpenAPI spec, but durable.
// Both teams depend on this interface:
//   - Payments team creates a stub from it (in the workflow)
//   - Compliance team implements a handler for it (in the worker)
//
// ── What to add for TODO 1: ─────────────────────────────────────
//
//   1. Add @Service annotation to the interface (from io.nexusrpc)
//   2. Add @Operation annotation to checkCompliance (from io.nexusrpc)
//
// ── What to add for TODO 6a: ────────────────────────────────────
//
//   Add a second operation for submitting human review decisions:
//
//   @Operation
//   ComplianceResult submitReview(ReviewRequest request);
//
//   This operation sends a Workflow Update to an already-running
//   ComplianceWorkflow via a sync Nexus handler on the Compliance side.
//
// ── Template: ───────────────────────────────────────────────────
//
//   @Service
//   public interface ComplianceNexusService {
//       @Operation
//       ComplianceResult checkCompliance(ComplianceRequest request);
//
//       @Operation
//       ComplianceResult submitReview(ReviewRequest request);
//   }

import compliance.domain.ComplianceRequest;
import compliance.domain.ComplianceResult;
import io.nexusrpc.Operation;
import io.nexusrpc.Service;
import shared.domain.ReviewRequest;

// TODO: Add @Service annotation
public interface ComplianceNexusService {

    // TODO: Add @Operation annotation
    ComplianceResult checkCompliance(ComplianceRequest request);

    // TODO 6a: Add @Operation annotation
    ComplianceResult submitReview(ReviewRequest request);
}
