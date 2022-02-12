package com.itrustmachines.client_it;

import java.util.ArrayList;
import java.util.List;

import com.itrustmachines.client.input.vo.LedgerInputRequest;
import com.itrustmachines.client.input.vo.LedgerInputResponse;
import com.itrustmachines.client.register.vo.RegisterRequest;
import com.itrustmachines.client.todo.BnsClientCallback;
import com.itrustmachines.client.verify.vo.DoneClearanceOrderEvent;
import com.itrustmachines.client.vo.ReceiptEvent;
import com.itrustmachines.common.vo.MerkleProof;
import com.itrustmachines.common.vo.Receipt;
import com.itrustmachines.common.vo.ReceiptLocator;
import com.itrustmachines.verification.vo.VerifyReceiptAndMerkleProofResult;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FakeBnsClientCallback implements BnsClientCallback {
  
  private final List<VerifyReceiptAndMerkleProofResult> verifyResultList = new ArrayList<>();
  
  public List<VerifyReceiptAndMerkleProofResult> getAllVerifyResults() {
    log.debug("getAllVerifyResults() list size={}", verifyResultList.size());
    return verifyResultList;
  }
  
  @Override
  public void register(RegisterRequest registerRequest, Boolean registerResult) {
    
  }
  
  @Override
  public void createLedgerInputByCmd(ReceiptLocator receiptLocator, LedgerInputRequest ledgerInputRequest) {
    
  }
  
  @Override
  public void obtainLedgerInputResponse(ReceiptLocator locator, String cmdJson,
      LedgerInputResponse ledgerInputResponse) {
    
  }
  
  @Override
  public void obtainReceiptEvent(ReceiptEvent receiptEvent) {
    
  }
  
  @Override
  public void obtainDoneClearanceOrderEvent(DoneClearanceOrderEvent doneClearanceOrderEvent) {
    
  }
  
  @Override
  public void obtainMerkleProof(ReceiptLocator receiptLocator, MerkleProof merkleProof) {
    
  }
  
  @Override
  public void getVerifyReceiptResult(Receipt receipt, MerkleProof merkleProof,
      VerifyReceiptAndMerkleProofResult verifyReceiptAndMerkleProofResult) {
    if (verifyReceiptAndMerkleProofResult == null) {
      log.warn("getVerifyReceiptResult() verifyReceiptAndMerkleProofResult is null");
      return;
    }
    verifyResultList.add(verifyReceiptAndMerkleProofResult);
    log.debug("callback, verifyResultList size={}", verifyResultList.size());
  }
  
}
