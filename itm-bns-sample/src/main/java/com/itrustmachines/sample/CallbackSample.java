package com.itrustmachines.sample;

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

/**
 * 使用者須自行實作callback方法，方法說明及規範請參照規範文件。
 * 此CallbackSample示範方法為：將Client資料傳送及驗證結果與Dashboard服務串接
 **/
@Slf4j
public class CallbackSample implements BnsClientCallback {

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
  }
}
