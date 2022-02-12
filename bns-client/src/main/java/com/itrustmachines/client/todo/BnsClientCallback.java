package com.itrustmachines.client.todo;

import com.itrustmachines.client.input.vo.LedgerInputRequest;
import com.itrustmachines.client.input.vo.LedgerInputResponse;
import com.itrustmachines.client.register.vo.RegisterRequest;
import com.itrustmachines.client.verify.vo.DoneClearanceOrderEvent;
import com.itrustmachines.client.vo.ReceiptEvent;
import com.itrustmachines.common.vo.MerkleProof;
import com.itrustmachines.common.vo.Receipt;
import com.itrustmachines.common.vo.ReceiptLocator;
import com.itrustmachines.verification.vo.VerifyReceiptAndMerkleProofResult;

public interface BnsClientCallback {
  
  /** 1. */
  void register(RegisterRequest registerRequest, Boolean registerResult);
  
  /** 2. */
  void createLedgerInputByCmd(ReceiptLocator receiptLocator, LedgerInputRequest ledgerInputRequest);
  
  /** 3.1 */
  void obtainLedgerInputResponse(ReceiptLocator locator, String cmdJson, LedgerInputResponse ledgerInputResponse);
  
  /** 4. Receipt from LedgerInputResult | Obtain from SPO Server API */
  // TODO add source
  void obtainReceiptEvent(ReceiptEvent receiptEvent);
  
  /** 5 DoneCO from LedgerInputResult | DoneCO from polling */
  void obtainDoneClearanceOrderEvent(DoneClearanceOrderEvent doneClearanceOrderEvent);
  
  /** 6.1 */
  void obtainMerkleProof(ReceiptLocator receiptLocator, MerkleProof merkleProof);
  
  /** 6.2 */
  void getVerifyReceiptResult(Receipt receipt, MerkleProof merkleProof,
      VerifyReceiptAndMerkleProofResult verifyReceiptAndMerkleProofResult);
  
}
