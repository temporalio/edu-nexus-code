// [GIVEN] Simulated payment gateway. In production this would call Stripe, PayPal, SWIFT.
//
// The 10% failure rate is deliberate: it shows Temporal retrying an Activity for free.
// Because of it, never assert on a clean first attempt — assert on final Workflow state.
import { PaymentRequest } from '../shared/types';

const money = (n: number) => `$${n.toFixed(2)}`;
const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));

export function validatePayment(request: PaymentRequest): boolean {
  if (request.amount <= 0) {
    console.log(`[PaymentGateway] REJECTED: Invalid amount for ${request.transactionId}`);
    return false;
  }
  if (!request.senderAccount || !request.receiverAccount) {
    console.log(`[PaymentGateway] REJECTED: Missing account info for ${request.transactionId}`);
    return false;
  }
  console.log(`[PaymentGateway] Validation passed for ${request.transactionId}`);
  return true;
}

export async function executePayment(request: PaymentRequest): Promise<string> {
  console.log(
    `[PaymentGateway] Processing ${request.transactionId}` +
      ` | ${money(request.amount)}` +
      ` | ${request.senderCountry} -> ${request.receiverCountry}`,
  );

  await sleep(500 + Math.random() * 500);

  // Simulate occasional gateway failures. Temporal retries automatically.
  if (Math.random() < 0.1) {
    throw new Error(
      `Payment gateway timeout for ${request.transactionId}, connection to banking network failed`,
    );
  }

  // Derived from the transaction ID, not the clock, so a retry of this Activity returns
  // the SAME confirmation number instead of minting a second one. With a 10% failure
  // rate above, a timestamp here would make retries look like distinct payments.
  // Idempotency is what makes an at-least-once retry safe.
  const confirmationNumber = `CONF-${request.transactionId}`;
  console.log(`[PaymentGateway] Payment executed: ${confirmationNumber}`);
  return confirmationNumber;
}
