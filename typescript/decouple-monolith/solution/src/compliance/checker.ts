// [GIVEN] Rule-based compliance checker. Deterministic, no API keys needed.
// Same logic whether it runs as an Activity or behind a Nexus handler.
//
// Rules:
//   - OFAC sanctioned country on either side      -> HIGH risk, blocked
//   - Amount over $50,000                          -> HIGH risk, blocked (TXN-C hits this)
//   - Over $10,000 or an unusual jurisdiction      -> MEDIUM risk (TXN-B hits this)
//   - Everything else                              -> LOW risk, approved
import { ComplianceRequest, ComplianceResult } from '../shared/types';

const SANCTIONED = new Set(['North Korea', 'Iran', 'Cuba', 'Syria', 'Venezuela']);
const COMMON = new Set(['US', 'UK', 'Canada', 'Germany', 'France', 'Japan', 'Australia']);

export function checkCompliance(request: ComplianceRequest): ComplianceResult {
  console.log(
    `[ComplianceChecker] Evaluating ${request.transactionId}` +
      ` | $${request.amount.toFixed(2)}` +
      ` | ${request.senderCountry} -> ${request.receiverCountry}`,
  );

  if (SANCTIONED.has(request.receiverCountry) || SANCTIONED.has(request.senderCountry)) {
    return {
      transactionId: request.transactionId,
      approved: false,
      riskLevel: 'HIGH',
      explanation:
        'Destination/source country is OFAC-sanctioned. Transaction blocked per regulatory requirements.',
    };
  }

  if (request.amount > 50_000) {
    return {
      transactionId: request.transactionId,
      approved: false,
      riskLevel: 'HIGH',
      explanation:
        'Transaction amount exceeds $50,000 threshold. Requires enhanced due diligence review.',
    };
  }

  const isInternational = request.senderCountry !== request.receiverCountry;
  const isUnusualJurisdiction = isInternational && !COMMON.has(request.receiverCountry);

  if (request.amount > 10_000 || isUnusualJurisdiction) {
    return {
      transactionId: request.transactionId,
      approved: true,
      riskLevel: 'MEDIUM',
      explanation: 'International transfer above $10K threshold. Approved with AML monitoring note.',
    };
  }

  return {
    transactionId: request.transactionId,
    approved: true,
    riskLevel: 'LOW',
    explanation: 'Routine domestic/standard international transfer. No regulatory concerns.',
  };
}
