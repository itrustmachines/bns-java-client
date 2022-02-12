package com.itrustmachines.client.input.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.web3j.crypto.Credentials;

import com.itrustmachines.client.config.BnsClientConfig;
import com.itrustmachines.client.input.vo.LedgerInputRequest;
import com.itrustmachines.client.input.vo.LedgerInputResponse;
import com.itrustmachines.client.input.vo.LedgerInputServiceParams;
import com.itrustmachines.client.service.BnsClientReceiptService;
import com.itrustmachines.client.service.ReceiptEventProcessor;
import com.itrustmachines.client.service.ReceiptLocatorService;
import com.itrustmachines.client.todo.BnsClientCallback;
import com.itrustmachines.client.verify.service.DoneClearanceOrderEventProcessor;
import com.itrustmachines.common.vo.KeyInfo;
import com.itrustmachines.common.vo.ReceiptLocator;

import lombok.extern.slf4j.Slf4j;

//TODO fix test
@Disabled
@Slf4j
public class LedgerInputServiceTest {
  
  final String bnsServerUrl = "https://azure-dev-membership.itm.monster:8088/";
  final String privateKey = "e0c07f854d30679c32a52af9d3cc72ac13923a4e77ba97337fb2e0cc295ddfde";
  
  @Test
  public void test_buildLedgerInputRequest() {
    // given
    final BnsClientCallback callback = mock(BnsClientCallback.class);
    final BnsClientReceiptService bnsClientReceiptService = mock(BnsClientReceiptService.class);
    final DoneClearanceOrderEventProcessor doneClearanceOrderEventProcessor = mock(
        DoneClearanceOrderEventProcessor.class);
    final ReceiptLocatorService receiptLocatorService = mock(ReceiptLocatorService.class);
    final ReceiptEventProcessor receiptEventProcessor = mock(ReceiptEventProcessor.class);
    
    final Credentials credentials = Credentials.create(privateKey);
    
    final KeyInfo keyInfo = KeyInfo.builder()
                                   .privateKey(privateKey)
                                   .address(credentials.getAddress())
                                   .publicKey(credentials.getEcKeyPair()
                                                         .getPublicKey()
                                                         .toString(16))
                                   .build();
    
    final LedgerInputServiceParams params = LedgerInputServiceParams.builder()
                                                                    .keyInfo(keyInfo)
                                                                    .bnsServerUrl(bnsServerUrl)
                                                                    .callback(callback)
                                                                    .bnsClientReceiptService(bnsClientReceiptService)
                                                                    .receiptLocatorService(receiptLocatorService)
                                                                    .doneClearanceOrderEventProcessor(
                                                                        doneClearanceOrderEventProcessor)
                                                                    .receiptEventProcessor(receiptEventProcessor)
                                                                    .retryDelaySec(
                                                                        BnsClientConfig.DEFAULT_RETRY_DELAY_SEC)
                                                                    .build();
    
    final LedgerInputService service = new LedgerInputService(params);
    
    final ReceiptLocator locator = ReceiptLocator.builder()
                                                 .clearanceOrder(1L)
                                                 .indexValue("Test_R0")
                                                 .build();
    final String cmdJson = "{}";
    
    // when
    final LedgerInputRequest result = service.buildLedgerInputRequest(locator, cmdJson);
    
    // then
    assertThat(result).isNotNull();
    assertThat(result.getIndexValue()).isEqualTo(locator.getIndexValue());
    assertThat(result.getCallerAddress()).isEqualTo(keyInfo.getAddress());
    
  }
  
  @Test
  public void test_postLedgerInput() {
    // given
    final BnsClientCallback callback = mock(BnsClientCallback.class);
    final BnsClientReceiptService bnsClientReceiptService = mock(BnsClientReceiptService.class);
    final DoneClearanceOrderEventProcessor doneClearanceOrderEventProcessor = mock(
        DoneClearanceOrderEventProcessor.class);
    final ReceiptLocatorService receiptLocatorService = mock(ReceiptLocatorService.class);
    final ReceiptEventProcessor receiptEventProcessor = mock(ReceiptEventProcessor.class);
    
    final Credentials credentials = Credentials.create(privateKey);
    
    final KeyInfo keyInfo = KeyInfo.builder()
                                   .privateKey(privateKey)
                                   .address(credentials.getAddress())
                                   .publicKey(credentials.getEcKeyPair()
                                                         .getPublicKey()
                                                         .toString(16))
                                   .build();
    
    final LedgerInputServiceParams params = LedgerInputServiceParams.builder()
                                                                    .keyInfo(keyInfo)
                                                                    .bnsServerUrl(bnsServerUrl)
                                                                    .callback(callback)
                                                                    .bnsClientReceiptService(bnsClientReceiptService)
                                                                    .receiptLocatorService(receiptLocatorService)
                                                                    .doneClearanceOrderEventProcessor(
                                                                        doneClearanceOrderEventProcessor)
                                                                    .receiptEventProcessor(receiptEventProcessor)
                                                                    .retryDelaySec(
                                                                        BnsClientConfig.DEFAULT_RETRY_DELAY_SEC)
                                                                    .build();
    
    final LedgerInputService service = new LedgerInputService(params);
    
    final LedgerInputRequest inputRequest = LedgerInputRequest.builder()
                                                              .callerAddress(keyInfo.getAddress())
                                                              .clearanceOrder(1L)
                                                              .indexValue("Test_R0")
                                                              .metadata("")
                                                              .cmd("{Test_CMD}")
                                                              .timestamp("" + Instant.now()
                                                                                     .toEpochMilli())
                                                              .build()
                                                              .sign(keyInfo.getPrivateKey());
    
    // when
    final LedgerInputResponse result = service.postLedgerInput(inputRequest);
    log.debug("result={}", result);
    
    // then
    assertThat(result).isNotNull();
    assertThat(result.getStatus()).isEqualToIgnoringCase("ERROR");
  }
  
  @Test
  public void test_ledgerInput() {
    // given
    final BnsClientCallback callback = mock(BnsClientCallback.class);
    final BnsClientReceiptService bnsClientReceiptService = mock(BnsClientReceiptService.class);
    final DoneClearanceOrderEventProcessor doneClearanceOrderEventProcessor = mock(
        DoneClearanceOrderEventProcessor.class);
    final ReceiptLocatorService receiptLocatorService = new ReceiptLocatorService(bnsServerUrl,
        BnsClientConfig.DEFAULT_RETRY_DELAY_SEC);
    final ReceiptEventProcessor receiptEventProcessor = mock(ReceiptEventProcessor.class);
    
    final Credentials credentials = Credentials.create(privateKey);
    
    final KeyInfo keyInfo = KeyInfo.builder()
                                   .privateKey(privateKey)
                                   .address(credentials.getAddress())
                                   .publicKey(credentials.getEcKeyPair()
                                                         .getPublicKey()
                                                         .toString(16))
                                   .build();
    
    final LedgerInputServiceParams params = LedgerInputServiceParams.builder()
                                                                    .keyInfo(keyInfo)
                                                                    .bnsServerUrl(bnsServerUrl)
                                                                    .callback(callback)
                                                                    .bnsClientReceiptService(bnsClientReceiptService)
                                                                    .receiptLocatorService(receiptLocatorService)
                                                                    .doneClearanceOrderEventProcessor(
                                                                        doneClearanceOrderEventProcessor)
                                                                    .receiptEventProcessor(receiptEventProcessor)
                                                                    .retryDelaySec(
                                                                        BnsClientConfig.DEFAULT_RETRY_DELAY_SEC)
                                                                    .build();
    
    final LedgerInputService service = new LedgerInputService(params);
    
    // when
    final String cmdJson = "{TEST_CMD}";
    final LedgerInputResponse result = service.ledgerInput(keyInfo, cmdJson);
    // then
    assertThat(result).isNotNull();
    assertThat(result.getStatus()
                     .toLowerCase()).isIn("ok", "error");
    verify(callback, times(1)).createLedgerInputByCmd(any(), any());
    verify(callback, times(1)).obtainLedgerInputResponse(any(), any(), any());
  }
  
  @Disabled
  @Test
  public void test_ledgerInput_retryAndFail() {
    // given
    final BnsClientCallback callback = mock(BnsClientCallback.class);
    final BnsClientReceiptService bnsClientReceiptService = mock(BnsClientReceiptService.class);
    final DoneClearanceOrderEventProcessor doneClearanceOrderEventProcessor = mock(
        DoneClearanceOrderEventProcessor.class);
    final ReceiptLocatorService receiptLocatorService = new ReceiptLocatorService(bnsServerUrl, 0);
    final ReceiptEventProcessor receiptEventProcessor = mock(ReceiptEventProcessor.class);
    
    final Credentials credentials = Credentials.create(privateKey);
    
    final KeyInfo keyInfo = KeyInfo.builder()
                                   .privateKey(privateKey)
                                   .address(credentials.getAddress())
                                   .publicKey(credentials.getEcKeyPair()
                                                         .getPublicKey()
                                                         .toString(16))
                                   .build();
    
    final LedgerInputServiceParams params = LedgerInputServiceParams.builder()
                                                                    .keyInfo(keyInfo)
                                                                    .bnsServerUrl("")
                                                                    .callback(callback)
                                                                    .bnsClientReceiptService(bnsClientReceiptService)
                                                                    .receiptLocatorService(receiptLocatorService)
                                                                    .doneClearanceOrderEventProcessor(
                                                                        doneClearanceOrderEventProcessor)
                                                                    .receiptEventProcessor(receiptEventProcessor)
                                                                    .retryDelaySec(0)
                                                                    .build();
    
    final LedgerInputService service = new LedgerInputService(params);
    
    // when,then
    Assertions.assertThrows(Exception.class, () -> service.ledgerInput(keyInfo, "{TEST_CMD}"));
  }
  
  @Test
  public void test_checkInputDescriptionNeedResend() {
    // given: build service
    final BnsClientCallback callback = mock(BnsClientCallback.class);
    final BnsClientReceiptService bnsClientReceiptService = mock(BnsClientReceiptService.class);
    final DoneClearanceOrderEventProcessor doneClearanceOrderEventProcessor = mock(
        DoneClearanceOrderEventProcessor.class);
    final ReceiptLocatorService receiptLocatorService = new ReceiptLocatorService(bnsServerUrl,
        BnsClientConfig.DEFAULT_RETRY_DELAY_SEC);
    final ReceiptEventProcessor receiptEventProcessor = mock(ReceiptEventProcessor.class);
    final KeyInfo keyInfo = KeyInfo.builder()
                                   .build();
    
    final LedgerInputServiceParams params = LedgerInputServiceParams.builder()
                                                                    .keyInfo(keyInfo)
                                                                    .bnsServerUrl(bnsServerUrl)
                                                                    .callback(callback)
                                                                    .bnsClientReceiptService(bnsClientReceiptService)
                                                                    .receiptLocatorService(receiptLocatorService)
                                                                    .doneClearanceOrderEventProcessor(
                                                                        doneClearanceOrderEventProcessor)
                                                                    .receiptEventProcessor(receiptEventProcessor)
                                                                    .retryDelaySec(
                                                                        BnsClientConfig.DEFAULT_RETRY_DELAY_SEC)
                                                                    .build();
    final LedgerInputService service = new LedgerInputService(params);
    
    // given: test data
    final LedgerInputResponse ok = LedgerInputResponse.builder()
                                                      .description("OK")
                                                      .build();
    final LedgerInputResponse clearance_order_error = LedgerInputResponse.builder()
                                                                         .description("CLEARANCE_ORDER_ERROR")
                                                                         .build();
    // when
    final boolean ok_result = service.checkInputDescriptionNeedResend(ok);
    final boolean clearance_order_error_result = service.checkInputDescriptionNeedResend(clearance_order_error);
    
    // then
    assertThat(ok_result).isFalse();
    assertThat(clearance_order_error_result).isTrue();
  }
}