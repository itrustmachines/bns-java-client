package com.itrustmachines.client.verify.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.itrustmachines.client.config.BnsClientConfig;

public class ObtainDoneClearanceOrderServiceTest {
  
  final String bnsServerUrl = "https://azure-dev-membership.itm.monster/";
  
  @Test
  public void test_obtainDoneClearanceOrder() {
    // given
    ObtainDoneClearanceOrderService service = new ObtainDoneClearanceOrderService(bnsServerUrl,
        BnsClientConfig.DEFAULT_RETRY_DELAY_SEC);
    
    // when
    final long result = service.postDoneClearanceOrder();
    
    // then
    assertThat(result).isNotNull();
    assertThat(result).isGreaterThanOrEqualTo(0);
  }
  
  @Test
  public void test_obtainDoneClearanceOrder_retryAndFail() {
    // given
    final String bnsServerUrl = "";
    ObtainDoneClearanceOrderService service = new ObtainDoneClearanceOrderService(bnsServerUrl, 0);
    
    // when, then
    // Assertions.assertThrows(Exception.class, service::obtainDoneClearanceOrder);
    Assertions.assertThrows(Exception.class, service::postDoneClearanceOrder);
  }
  
}