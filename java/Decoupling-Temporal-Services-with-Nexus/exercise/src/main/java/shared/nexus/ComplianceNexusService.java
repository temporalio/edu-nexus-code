package shared.nexus;

// ═══════════════════════════════════════════════════════════════════
//  TODO 3: Create the Nexus service interface (the shared contract)
// ═══════════════════════════════════════════════════════════════════
//
// This is the shared contract between teams — like an OpenAPI spec, but durable.
// Both teams depend on this interface:
//   - Payments team creates a stub from it (in the workflow)
//   - Compliance team implements a handler for it (in the worker)
//
// ── What to add: ────────────────────────────────────────────────
//
//   1. Add @Service annotation to the interface (from io.nexusrpc)
//   2. Add one method: checkCompliance(ComplianceRequest) → ComplianceResult
//   3. Mark that method with @Operation (from io.nexusrpc)
//
// ── Template: ───────────────────────────────────────────────────
//
//   @Service
//   public interface ComplianceNexusService {
//       @Operation
//       ComplianceResult checkCompliance(ComplianceRequest request);
//   }

import compliance.domain.ComplianceRequest;
import compliance.domain.ComplianceResult;
import io.nexusrpc.Operation;
import io.nexusrpc.Service;

// TODO: Add @Service annotation
public interface ComplianceNexusService {

    // TODO: Add @Operation annotation
    ComplianceResult checkCompliance(ComplianceRequest request);
}
