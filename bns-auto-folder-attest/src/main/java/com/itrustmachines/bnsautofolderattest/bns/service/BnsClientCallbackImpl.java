package com.itrustmachines.bnsautofolderattest.bns.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.itrustmachines.bnsautofolderattest.callback.Callback;
import com.itrustmachines.bnsautofolderattest.config.Config;
import com.itrustmachines.bnsautofolderattest.service.AttestationRecordService;
import com.itrustmachines.bnsautofolderattest.vo.AttestationRecord;
import com.itrustmachines.bnsautofolderattest.vo.AttestationStatus;
import com.itrustmachines.bnsautofolderattest.vo.VerificationProofResponse;
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

import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@ToString(exclude = { "gson" })
@Slf4j
public class BnsClientCallbackImpl implements BnsClientCallback {
  
  private final Config config;
  private final Callback callback;
  private final AttestationRecordService attestationRecordService;
  private final VerificationProofService verificationProofService;
  private final Gson gson;
  
  public BnsClientCallbackImpl(@NonNull final Config config, @NonNull final Callback callback,
      @NonNull final AttestationRecordService attestationRecordService,
      @NonNull final VerificationProofService verificationProofService) {
    this.config = config;
    this.callback = callback;
    this.attestationRecordService = attestationRecordService;
    this.verificationProofService = verificationProofService;
    this.gson = new Gson();
    
    log.info("new instance={}", this);
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
    final AttestationRecord attestationRecord = attestationRecordService.findLastByCOAndIVAndStatus(
        receipt.getClearanceOrder(), receipt.getIndexValue(), AttestationStatus.ATTESTED);
    if (attestationRecord != null) {
      if (attestationRecord.getPreviousRecord() != null) {
        attestationRecord.setPreviousRecord(attestationRecordService.findById(attestationRecord.getPreviousRecord()
                                                                                               .getId()));
      }
      handleVerifyResult(verifyReceiptAndMerkleProofResult, attestationRecord);
      if (verifyReceiptAndMerkleProofResult.isPass() && !config.isDisableDownloadVerificationProof()) {
        handleSaveProof(attestationRecord);
      }
    } else {
      log.error("onVerified() attestationRecord not found");
    }
  }
  
  private void handleVerifyResult(VerifyReceiptAndMerkleProofResult verifyReceiptAndMerkleProofResult,
      AttestationRecord attestationRecord) {
    attestationRecord.setStatus(
        verifyReceiptAndMerkleProofResult.isPass() ? AttestationStatus.VERIFIED : AttestationStatus.VERIFY_FAIL);
    attestationRecordService.save(attestationRecord);
    if (verifyReceiptAndMerkleProofResult.isPass()) {
      callback.onVerified(attestationRecord);
    } else {
      callback.onVerifyFail(attestationRecord);
    }
  }
  
  private void handleSaveProof(AttestationRecord attestationRecord) {
    try {
      final VerificationProofResponse verificationProof = verificationProofService.getVerificationProof(
          attestationRecord);
      final String proofName = String.format("%s_%d_%s.itm", attestationRecord.getFilePath()
                                                                              .getFileName()
                                                                              .toString(),
          attestationRecord.getClearanceOrder(), attestationRecord.getIndexValue());
      final Path proofPath = config.getVerificationProofDownloadPath()
                                   .resolve(proofName);
      Files.write(proofPath, gson.toJson(verificationProof)
                                 .getBytes(StandardCharsets.UTF_8));
      attestationRecord.setStatus(AttestationStatus.SAVE_PROOF);
      attestationRecord.setProofPath(proofPath);
      attestationRecordService.save(attestationRecord);
      callback.onSaveProof(attestationRecord);
    } catch (IOException e) {
      log.error("getVerifyReceiptResult() error", e);
      attestationRecord.setStatus(AttestationStatus.SAVE_FAIL);
      attestationRecordService.save(attestationRecord);
      callback.onSaveFail(attestationRecord);
    }
  }
  
}
