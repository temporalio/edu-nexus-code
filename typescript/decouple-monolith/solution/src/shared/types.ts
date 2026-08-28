// [GIVEN] The data that crosses team boundaries.
//
// These are plain interfaces. Unlike the Kotlin version, nothing here needs a
// no-arg constructor or parameter-name retention: the TypeScript SDK's default
// data converter is JSON, so a structural type is all the Nexus boundary needs.

/** A payment transaction to be processed. */
export interface PaymentRequest {
  transactionId: string;
  amount: number;
  currency: string;
  senderCountry: string;
  receiverCountry: string;
  description: string;
  senderAccount: string;
  receiverAccount: string;
}

/** The outcome of a payment Workflow. */
export interface PaymentResult {
  success: boolean;
  transactionId: string;
  status: 'COMPLETED' | 'REJECTED' | 'DECLINED_COMPLIANCE' | 'FAILED';
  riskLevel?: RiskLevel;
  explanation?: string;
  confirmationNumber?: string;
  error?: string;
}

export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH';

/** Input to the compliance check. Sent by Payments to Compliance. */
export interface ComplianceRequest {
  transactionId: string;
  amount: number;
  senderCountry: string;
  receiverCountry: string;
  description: string;
}

/** Result of a compliance check. Returned by Compliance to Payments. */
export interface ComplianceResult {
  transactionId: string;
  approved: boolean;
  riskLevel: RiskLevel;
  explanation: string;
}

/** A human review decision, submitted through Nexus. */
export interface ReviewRequest {
  transactionId: string;
  approved: boolean;
  explanation: string;
}

export const PAYMENTS_TASK_QUEUE = 'payments-processing';
export const COMPLIANCE_TASK_QUEUE = 'compliance-risk';
export const PAYMENTS_NAMESPACE = 'payments-namespace';
export const COMPLIANCE_NAMESPACE = 'compliance-namespace';
export const COMPLIANCE_ENDPOINT = 'compliance-endpoint';
