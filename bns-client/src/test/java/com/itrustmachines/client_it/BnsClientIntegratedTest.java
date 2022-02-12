package com.itrustmachines.client_it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.itrustmachines.client.BnsClient;
import com.itrustmachines.client.config.BnsClientConfig;
import com.itrustmachines.client.input.service.LedgerInputService;
import com.itrustmachines.client.input.vo.LedgerInputResponse;
import com.itrustmachines.client.input.vo.LedgerInputServiceParams;
import com.itrustmachines.client.register.service.BnsServerInfoService;
import com.itrustmachines.client.register.service.RegisterService;
import com.itrustmachines.client.service.BnsClientReceiptService;
import com.itrustmachines.client.service.ReceiptEventProcessor;
import com.itrustmachines.client.service.ReceiptLocatorService;
import com.itrustmachines.client.verify.service.DoneClearanceOrderEventProcessor;
import com.itrustmachines.client.verify.service.MerkleProofService;
import com.itrustmachines.client.verify.service.ObtainDoneClearanceOrderService;
import com.itrustmachines.client.vo.BnsServerInfo;
import com.itrustmachines.common.constants.StatusConstants;
import com.itrustmachines.common.ethereum.service.ClientContractService;
import com.itrustmachines.common.util.KeyInfoUtil;
import com.itrustmachines.common.vo.KeyInfo;
import com.itrustmachines.common.vo.Receipt;
import com.itrustmachines.verification.service.VerifyReceiptAndMerkleProofService;
import com.itrustmachines.verification.vo.VerifyReceiptAndMerkleProofResult;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Disabled
@Slf4j
public class BnsClientIntegratedTest {
  
  private static final String CONFIG_FILE_PATH = "./src/test/resources/client-integrated-test.properties";
  private static final FakeBnsClientCallback callback = new FakeBnsClientCallback();
  private static final FakeReceiptDao receiptDao = new FakeReceiptDao();
  
  @Test
  public void test_bns_client_IT() throws InterruptedException {
    // init client
    final BnsClientConfig config = BnsClientConfig.load(CONFIG_FILE_PATH);
    log.debug("config={}", config);
    BnsClient bnsClient = BnsClient.init(config, callback, receiptDao);
    assertThat(bnsClient).isNotNull();
    
    final String email = config.getEmail();
    final String bnsServerUrl = config.getBnsServerUrl();
    final KeyInfo keyInfo = KeyInfoUtil.buildKeyInfo(config.getPrivateKey());
    // register
    final RegisterService registerService = new RegisterService(bnsServerUrl, callback, keyInfo,
        config.getRetryDelaySec(), email);
    final boolean isRegister = registerService.checkRegister();
    log.debug("isRegister={}", isRegister);
    assertThat(isRegister).isTrue();
    
    // init ledger input service
    final ObtainDoneClearanceOrderService obtainDoneClearanceOrderService = new ObtainDoneClearanceOrderService(
        bnsServerUrl, config.getRetryDelaySec());
    final BnsClientReceiptService bnsClientReceiptService = new BnsClientReceiptService(receiptDao);
    final ReceiptLocatorService receiptLocatorService = new ReceiptLocatorService(bnsServerUrl,
        config.getRetryDelaySec());
    final MerkleProofService merkleProofService = new MerkleProofService(bnsServerUrl, callback,
        config.getRetryDelaySec());
    
    final BnsServerInfo serverInfo = obtainBnsServerInfo(bnsServerUrl);
    ClientContractService contractService = new ClientContractService(serverInfo.getContractAddress(),
        keyInfo.getPrivateKey(), config.getNodeUrl(), 1.0, 5);
    // TODO remove mock
    final VerifyReceiptAndMerkleProofService verifyReceiptAndMerkleProofService = mock(
        VerifyReceiptAndMerkleProofService.class);
    
    final DoneClearanceOrderEventProcessor doneClearanceOrderEventProcessor = new DoneClearanceOrderEventProcessor(
        callback, bnsClientReceiptService, merkleProofService, verifyReceiptAndMerkleProofService, contractService,
        serverInfo.getServerWalletAddress(), config.getVerifyBatchSize(), config.getVerifyDelaySec());
    final ReceiptEventProcessor receiptEventProcessor = new ReceiptEventProcessor(callback, bnsClientReceiptService);
    
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
    
    final LedgerInputService ledgerInputService = new LedgerInputService(params);
    
    // do ledger input
    final String cmdJson = "{}";
    final long beforeInputCO = receiptLocatorService.postReceiptLocator(keyInfo)
                                                    .getClearanceOrder();
    // final long beforeInputCO = obtainClearanceOrder(bnsServerUrl);
    int receiptCount = 0;
    
    LedgerInputResponse inputResponse;
    while (receiptLocatorService.postReceiptLocator(keyInfo)
                                .getClearanceOrder() <= beforeInputCO) {
      inputResponse = ledgerInputService.ledgerInput(keyInfo, cmdJson);
      log.debug("inputResponse={}", inputResponse);
      receiptCount++;
      log.debug("receiptCount={}", receiptCount);
      assertThat(inputResponse.getStatus()).isEqualToIgnoringCase(StatusConstants.OK.toString());
      assertThat(receiptDao.findAll()
                           .size()).isEqualTo(receiptCount);
      TimeUnit.SECONDS.sleep(1);
    }
    
    // TODO set time out
    while (obtainDoneClearanceOrderService.postDoneClearanceOrder() < beforeInputCO) {
      log.debug("wait for clearance");
      TimeUnit.SECONDS.sleep(1);
    }
    
    bnsClient.verifyNow();
    
    final List<Receipt> allReceipts = receiptDao.findAll();
    assertThat(allReceipts.size()).isEqualTo(0);
    final List<VerifyReceiptAndMerkleProofResult> verifyResults = callback.getAllVerifyResults();
    assertThat(verifyResults.size()).isEqualTo(receiptCount);
    
    verifyResults.stream()
                 .map(VerifyReceiptAndMerkleProofResult::isPass)
                 .forEach(isPass -> assertThat(isPass).isTrue());
    bnsClient.close();
  }
  
  private BnsServerInfo obtainBnsServerInfo(@NonNull final String bnsServerUrl) {
    final String privateKey = "e0c07f854d30679c32a52af9d3cc72ac13923a4e77ba97337fb2e0cc295ddfde";
    final KeyInfo keyInfo = KeyInfoUtil.buildKeyInfo(privateKey);
    BnsServerInfoService serverInfoService = new BnsServerInfoService(bnsServerUrl, 5);
    final BnsServerInfo bnsServerInfo = serverInfoService.postBnsServerInfo(keyInfo);
    log.debug("obtainBnsServerInfo() bnsServerInfo={}", bnsServerInfo);
    return bnsServerInfo;
  }
  
}
