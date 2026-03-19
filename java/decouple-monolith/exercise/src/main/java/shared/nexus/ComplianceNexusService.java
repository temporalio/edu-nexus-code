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
//   2. Add @Operation annotation to BOTH methods (checkCompliance and submitReview)
//
//   The Nexus runtime validates all methods in a @Service interface at worker
//   startup. Every method must have @Operation — even ones you won't call right
//   away — or the worker will fail with "Missing @Operation annotation".
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

// TODO 1: Add @Service annotation
public interface ComplianceNexusService {

    // TODO 1: Add @Operation annotation
    ComplianceResult checkCompliance(ComplianceRequest request);

    // TODO 1: Add @Operation annotation
    ComplianceResult submitReview(ReviewRequest request);
}
