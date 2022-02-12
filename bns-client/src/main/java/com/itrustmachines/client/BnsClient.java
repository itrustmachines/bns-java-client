package com.itrustmachines.client;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.itrustmachines.client.config.BnsClientConfig;
import com.itrustmachines.client.input.service.LedgerInputService;
import com.itrustmachines.client.input.vo.LedgerInputResponse;
import com.itrustmachines.client.input.vo.LedgerInputServiceParams;
import com.itrustmachines.client.login.service.LoginService;
import com.itrustmachines.client.register.service.BnsServerInfoService;
import com.itrustmachines.client.register.service.RegisterService;
import com.itrustmachines.client.service.BnsClientReceiptService;
import com.itrustmachines.client.service.ReceiptEventProcessor;
import com.itrustmachines.client.service.ReceiptLocatorService;
import com.itrustmachines.client.todo.BnsClientCallback;
import com.itrustmachines.client.todo.BnsClientReceiptDao;
import com.itrustmachines.client.verify.service.DoneClearanceOrderEventProcessor;
import com.itrustmachines.client.verify.service.MerkleProofService;
import com.itrustmachines.client.verify.service.ObtainDoneClearanceOrderService;
import com.itrustmachines.client.verify.vo.DoneClearanceOrderEvent;
import com.itrustmachines.client.vo.BnsServerInfo;
import com.itrustmachines.client.vo.ClientInfo;
import com.itrustmachines.common.ethereum.service.ClientContractService;
import com.itrustmachines.common.util.KeyInfoUtil;
import com.itrustmachines.common.vo.KeyInfo;
import com.itrustmachines.verification.service.VerifyReceiptAndMerkleProofService;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter(AccessLevel.PACKAGE)
@ToString
@Slf4j
public class BnsClient {
  
  @Getter(AccessLevel.PUBLIC)
  private final BnsClientConfig config;
  private final KeyInfo keyInfo;
  @Getter(AccessLevel.PUBLIC)
  private final ClientInfo clientInfo;
  @Getter(AccessLevel.PUBLIC)
  private final BnsServerInfo bnsServerInfo;
  
  @Getter(AccessLevel.PUBLIC)
  private final BnsClientReceiptService bnsClientReceiptService;
  private final ReceiptLocatorService receiptLocatorService;
  
  private final RegisterService registerService;
  private final LoginService loginService;
  private final BnsServerInfoService bnsServerInfoService;
  
  // ledger input and process receipt
  private final LedgerInputService ledgerInputService;
  private final ReceiptEventProcessor receiptEventProcessor;
  
  // process receipt doneCO
  private final VerifyReceiptAndMerkleProofService verifyService;
  private final ObtainDoneClearanceOrderService obtainDoneClearanceOrderService;
  private final MerkleProofService merkleProofService;
  private final DoneClearanceOrderEventProcessor doneClearanceOrderEventProcessor;
  private final ExecutorService executorService;
  
  private BnsClient(BnsClientConfig config, BnsClientCallback callback, BnsClientReceiptDao receiptDao) {
    this.config = config;
    this.keyInfo = KeyInfoUtil.buildKeyInfo(config.getPrivateKey());
    
    this.bnsClientReceiptService = new BnsClientReceiptService(receiptDao);
    this.receiptLocatorService = new ReceiptLocatorService(config.getBnsServerUrl(), config.getRetryDelaySec());
    this.registerService = new RegisterService(config.getBnsServerUrl(), callback, keyInfo, config.getRetryDelaySec(),
        config.getEmail());
    this.loginService = new LoginService(config.getBnsServerUrl(), keyInfo, config.getRetryDelaySec());
    this.bnsServerInfoService = new BnsServerInfoService(config.getBnsServerUrl(), config.getRetryDelaySec());
    
    this.obtainDoneClearanceOrderService = new ObtainDoneClearanceOrderService(config.getBnsServerUrl(),
        config.getRetryDelaySec());
    this.merkleProofService = new MerkleProofService(config.getBnsServerUrl(), callback, config.getRetryDelaySec());
    
    this.receiptEventProcessor = new ReceiptEventProcessor(callback, bnsClientReceiptService);
    
    boolean isRegistered = registerService.checkRegister();
    
    if (!isRegistered) {
      log.debug("BnsClient not registered");
      isRegistered = registerService.register();
      if (!isRegistered) {
        String errMsg = "BnsClient register fail";
        log.error("init() error, {}", errMsg);
        throw new RuntimeException(errMsg);
      }
    }
    
    this.clientInfo = loginService.login();
    
    this.bnsServerInfo = obtainBnsServerInfo(config.getBnsServerUrl());
    
    final ClientContractService clearanceRecordService = obtainClearanceRecordService(config, bnsServerInfo);
    this.verifyService = new VerifyReceiptAndMerkleProofService();
    this.doneClearanceOrderEventProcessor = new DoneClearanceOrderEventProcessor(callback, bnsClientReceiptService,
        merkleProofService, verifyService, clearanceRecordService, bnsServerInfo.getServerWalletAddress(),
        config.getVerifyBatchSize(), config.getVerifyDelaySec());
    
    final LedgerInputServiceParams params = LedgerInputServiceParams.builder()
                                                                    .keyInfo(keyInfo)
                                                                    .bnsServerUrl(config.getBnsServerUrl())
                                                                    .callback(callback)
                                                                    .bnsClientReceiptService(bnsClientReceiptService)
                                                                    .receiptLocatorService(receiptLocatorService)
                                                                    .doneClearanceOrderEventProcessor(
                                                                        doneClearanceOrderEventProcessor)
                                                                    .receiptEventProcessor(receiptEventProcessor)
                                                                    .retryDelaySec(config.getRetryDelaySec())
                                                                    .build();
    this.ledgerInputService = new LedgerInputService(params);
    this.executorService = Executors.newSingleThreadExecutor();
    log.info("new instance={}", this);
  }
  
  private BnsServerInfo obtainBnsServerInfo(@NonNull final String bnsServerUrl) {
    BnsServerInfoService serverInfoService = new BnsServerInfoService(bnsServerUrl, config.getRetryDelaySec());
    final BnsServerInfo bnsServerInfo = serverInfoService.postBnsServerInfo(keyInfo);
    log.debug("obtainBnsServerInfo() bnsServerInfo={}", bnsServerInfo);
    return bnsServerInfo;
  }
  
  private ClientContractService obtainClearanceRecordService(@NonNull final BnsClientConfig config,
      final @NonNull BnsServerInfo bnsServerInfo) {
    log.debug("obtainClearanceRecordService() config={}, bnsServerInfo={}", config, bnsServerInfo);
    ClientContractService clearanceRecordService;
    if (!Objects.isNull(config.getNodeNeedAuth()) && config.getNodeNeedAuth()) {
      clearanceRecordService = new ClientContractService(bnsServerInfo.getContractAddress(), config.getPrivateKey(),
          config.getNodeUrl(), 1.0, config.getNodeNeedAuth(), config.getNodeUserName(), config.getNodePassword(),
          config.getRetryDelaySec());
    } else {
      clearanceRecordService = new ClientContractService(bnsServerInfo.getContractAddress(), config.getPrivateKey(),
          config.getNodeUrl(), 1.0, config.getRetryDelaySec());
    }
    return clearanceRecordService;
  }
  
  public static BnsClient init(@NonNull final BnsClientConfig config, @NonNull final BnsClientCallback callback,
      @NonNull final BnsClientReceiptDao receiptDao) {
    
    log.debug("BnsClient init() start, config={}", config);
    if (config.getVerifyBatchSize() <= 0) {
      config.setVerifyBatchSize(BnsClientConfig.DEFAULT_VERIFY_BATCH_SIZE);
    }
    BnsClient bnsClient = new BnsClient(config, callback, receiptDao);
    bnsClient.executorService.submit(() -> {
      while (true) {
        TimeUnit.MINUTES.sleep(20);
        bnsClient.loginService.login();
      }
    });
    log.debug("BnsClient init() end, bnsClient={}", bnsClient);
    return bnsClient;
  }
  
  public LedgerInputResponse ledgerInput(@NonNull final KeyInfo keyInfo, @NonNull final String cmdJson) {
    return ledgerInputService.ledgerInput(keyInfo, cmdJson);
  }
  
  public LedgerInputResponse ledgerInput(@NonNull final String cmdJson) {
    return ledgerInputService.ledgerInput(keyInfo, cmdJson);
  }
  
  // TODO verify by polling doneCO
  
  public void verifyNow() {
    log.debug("verifyNow() start");
    
    final long doneCO = obtainDoneClearanceOrderService.postDoneClearanceOrder();
    doneClearanceOrderEventProcessor.process(DoneClearanceOrderEvent.builder()
                                                                    .source(DoneClearanceOrderEvent.Source.DIRECT_CALL)
                                                                    .doneClearanceOrder(doneCO)
                                                                    .build());
  }
  
  public void close() {
    log.debug("close()");
    bnsClientReceiptService.close();
    doneClearanceOrderEventProcessor.close();
  }
  
}
