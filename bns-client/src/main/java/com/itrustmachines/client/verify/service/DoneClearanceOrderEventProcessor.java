package com.itrustmachines.client.verify.service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.itrustmachines.client.service.BnsClientReceiptService;
import com.itrustmachines.client.todo.BnsClientCallback;
import com.itrustmachines.client.verify.vo.DoneClearanceOrderEvent;
import com.itrustmachines.common.ethereum.service.ClientContractService;
import com.itrustmachines.common.vo.*;
import com.itrustmachines.verification.service.VerifyReceiptAndMerkleProofService;
import com.itrustmachines.verification.vo.VerifyReceiptAndMerkleProofResult;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@ToString
@Slf4j
public class DoneClearanceOrderEventProcessor {
  
  private final BnsClientCallback callback;
  private final BnsClientReceiptService receiptService;
  private final MerkleProofService merkleProofService;
  private final VerifyReceiptAndMerkleProofService verifyService;
  private final ClientContractService contractService;
  private final int verifyBatchSize;
  private final int verifyDelaySec;
  
  private final String serverWalletAddress;
  private KeyInfo keyInfo;
  private long doneClearanceOrder;
  private boolean isCloseCalled;
  private final ExecutorService executorService;
  
  public DoneClearanceOrderEventProcessor(@NonNull final BnsClientCallback callback,
      @NonNull final BnsClientReceiptService receiptService, @NonNull final MerkleProofService merkleProofService,
      @NonNull final VerifyReceiptAndMerkleProofService verifyService,
      @NonNull final ClientContractService contractService, @NonNull final String serverWalletAddress,
      final int verifyBatchSize, final int verifyDelaySec, @NonNull final KeyInfo keyInfo ) {
    this.callback = callback;
    this.receiptService = receiptService;
    this.merkleProofService = merkleProofService;
    this.verifyService = verifyService;
    this.contractService = contractService;
    this.verifyBatchSize = verifyBatchSize;
    this.verifyDelaySec = verifyDelaySec;
    this.serverWalletAddress = serverWalletAddress;
    this.keyInfo = keyInfo;
    this.isCloseCalled = false;
    this.executorService = Executors.newSingleThreadExecutor();
    executorService.submit(this::verifyReceipts);
    log.info("new instance={}", this);
  }
  
  public void process(@NonNull final DoneClearanceOrderEvent event) {
    log.debug("process() start, event={}", event);
    try {
      callback.obtainDoneClearanceOrderEvent(event);
    } catch (Exception e) {
      if (Thread.currentThread()
                .isInterrupted()) {
        throw e;
      }
      log.error("process() callback obtainDoneClearanceOrderEvent error, event={}", event, e);
    }
    
    if (doneClearanceOrder < event.getDoneClearanceOrder()) {
      doneClearanceOrder = event.getDoneClearanceOrder();
    }
  }
  
  @SneakyThrows
  private void verifyReceipts() {
    log.debug("verifyReceipts() start");
    while (!isCloseCalled && !Thread.currentThread()
                                    .isInterrupted()) {
      try {
        final Map<Long, Set<String>> needVerifyReceiptLocatorMap = receiptService.getNeedVerifyReceiptLocatorMap(
            doneClearanceOrder);
        
        int delayCount = 0;
        for (long co : needVerifyReceiptLocatorMap.keySet()) {
          Set<String> indexValues = needVerifyReceiptLocatorMap.get(co);
          for (String iv : indexValues) {
            final ReceiptLocator locator = ReceiptLocator.builder()
                                                         .clearanceOrder(co)
                                                         .indexValue(iv)
                                                         .build();
            log.debug("process() locator={}", locator);
            Receipt receipt = receiptService.findByLocator(locator);
            
            MerkleProof merkleProof = null;
            VerifyReceiptAndMerkleProofResult verifyResult;
            try {
              merkleProof = merkleProofService.postMerkleProof(locator, keyInfo);
              final ClearanceRecord clearanceRecord = contractService.obtainClearanceRecord(
                  merkleProof.getClearanceOrder());
              verifyResult = verifyService.verify(receipt, merkleProof, serverWalletAddress, clearanceRecord);
            } catch (Exception e) {
              if (Thread.currentThread()
                        .isInterrupted()) {
                throw e;
              }
              log.error("verify receipt error, receipt={}", receipt, e);
              
              verifyResult = VerifyReceiptAndMerkleProofResult.builder()
                                                              .clearanceOrder(receipt.getClearanceOrder())
                                                              .indexValue(receipt.getIndexValue())
                                                              .pass(false)
                                                              .status("error")
                                                              .timestamp(System.currentTimeMillis())
                                                              .description(e.getMessage())
                                                              .merkleproofSignatureOk(false)
                                                              .receiptSignatureOk(false)
                                                              .clearanceOrderOk(false)
                                                              .pbPairOk(false)
                                                              .sliceOk(false)
                                                              .clearanceRecordRootHashOk(false)
                                                              .build();
            }
            
            try {
              callback.getVerifyReceiptResult(receipt, merkleProof, verifyResult);
            } catch (Exception e) {
              if (Thread.currentThread()
                        .isInterrupted()) {
                throw e;
              }
              log.error("process() callback getVerifyReceiptResult error, receipt={}, merkleProof={}, verifyResult={}",
                  receipt, merkleProof, verifyResult, e);
            }
            receiptService.delete(receipt);
            
            if (++delayCount >= verifyBatchSize) {
              TimeUnit.SECONDS.sleep(verifyDelaySec);
              delayCount = 0;
            }
          }
        }
      } catch (final Exception e) {
        if (Thread.currentThread()
                  .isInterrupted()) {
          throw e;
        }
        log.error("verifyReceipts() error", e);
      }
      TimeUnit.SECONDS.sleep(3L);
    }
    log.debug("verifyReceipts() end");
  }
  
  public void close() {
    isCloseCalled = true;
  }
  
}
