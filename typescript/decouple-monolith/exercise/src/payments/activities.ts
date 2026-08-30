// [GIVEN] Payments team Activities. Thin wrappers around the gateway.
//
// An Activity is just an exported function. They must be async: proxyActivities only accepts
// Promise-returning functions, and a synchronous one is silently typed away, surfacing
// later as "Type 'Symbol' has no call signatures" at the call site in the Workflow.
import * as gateway from './gateway';
import { PaymentRequest } from '../shared/types';

export async function validatePayment(request: PaymentRequest): Promise<boolean> {
  return gateway.validatePayment(request);
}

export async function executePayment(request: PaymentRequest): Promise<string> {
  return gateway.executePayment(request);
}
