package com.itrustmachines.client.service;

import com.itrustmachines.client.config.BnsClientConfig;
import com.itrustmachines.client.login.service.LoginService;
import com.itrustmachines.common.constants.StatusConstants;
import com.itrustmachines.common.util.KeyInfoUtil;
import com.itrustmachines.common.vo.KeyInfo;
import com.itrustmachines.common.vo.ReceiptLocator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class ReceiptLocatorServiceTest {
  
  @Test
  public void test_obtainReceiptLocator() {
    final String privateKey = "e0c07f854d30679c32a52af9d3cc72ac13923a4e77ba97337fb2e0cc295ddfde";
    final String bnsServerUrl = "https://bns.itrustmachines.com/";
    
    final KeyInfo keyInfo = KeyInfoUtil.buildKeyInfo(privateKey);
    new LoginService(bnsServerUrl, keyInfo, BnsClientConfig.DEFAULT_RETRY_DELAY_SEC).login();
    final ReceiptLocatorService service = new ReceiptLocatorService(bnsServerUrl,
        BnsClientConfig.DEFAULT_RETRY_DELAY_SEC);
    
    final ReceiptLocator receiptLocator = service.postReceiptLocator(keyInfo);
    log.info("receiptLocator={}", receiptLocator);
    assertThat(receiptLocator).isNotNull();
    assertThat(receiptLocator.getClearanceOrder()).isGreaterThanOrEqualTo(1L);
    assertThat(receiptLocator.getIndexValue()).startsWith(keyInfo.getAddress());
  }
  
  @Test
  public void test_obtainReceiptLocator_retryAndFail() {
    // given
    final String privateKey = "e0c07f854d30679c32a52af9d3cc72ac13923a4e77ba97337fb2e0cc295ddfde";
    final KeyInfo keyInfo = KeyInfoUtil.buildKeyInfo(privateKey);
    final ReceiptLocatorService service = new ReceiptLocatorService("", 0);
    
    // when,then
    Assertions.assertThrows(Exception.class, () -> service.postReceiptLocator(keyInfo));
  }
  
  @Test
  public void test_checkResponse() {
    // given
    final ReceiptLocatorService service = new ReceiptLocatorService("", BnsClientConfig.DEFAULT_RETRY_DELAY_SEC);
    
    final ReceiptLocatorService.ReceiptLocatorResponse okRes = ReceiptLocatorService.ReceiptLocatorResponse.builder()
                                                                                                           .status(
                                                                                                               StatusConstants.OK.name())
                                                                                                           .build();
    final ReceiptLocatorService.ReceiptLocatorResponse errRes = ReceiptLocatorService.ReceiptLocatorResponse.builder()
                                                                                                            .status(
                                                                                                                StatusConstants.ERROR.name())
                                                                                                            .build();
    // when, then
    // response status ok
    assertThat(service.checkResponse(okRes)).isTrue();
    
    // response null
    assertThat(service.checkResponse(null)).isFalse();
    
    // response status error
    assertThat(service.checkResponse(errRes)).isFalse();
  }
}