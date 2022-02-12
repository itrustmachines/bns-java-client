package com.itrustmachines.client.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.itrustmachines.client.config.BnsClientConfig;
import com.itrustmachines.client.register.service.BnsServerInfoService;
import com.itrustmachines.client.vo.BnsServerInfo;
import com.itrustmachines.common.util.KeyInfoUtil;
import com.itrustmachines.common.vo.KeyInfo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class bnsServerInfoServiceTest {
  
  @Test
  public void test_postBnsServerInfo_success() {
    final String privateKey = "e0c07f854d30679c32a52af9d3cc72ac13923a4e77ba97337fb2e0cc295ddfde";
    final String bnsServerUrl = "https://azure-dev-membership.itm.monster:8088";
    final KeyInfo keyInfo = KeyInfoUtil.buildKeyInfo(privateKey);
    final BnsServerInfoService service = new BnsServerInfoService(bnsServerUrl,
        BnsClientConfig.DEFAULT_RETRY_DELAY_SEC);
    final BnsServerInfo bnsServerInfo = service.postBnsServerInfo(keyInfo);
    
    log.info("bnsServerInfo={}", bnsServerInfo);
    assertThat(bnsServerInfo).isNotNull();
    assertThat(bnsServerInfo.getServerWalletAddress()).isNotBlank();
    assertThat(bnsServerInfo.getContractAddress()).isNotBlank();
  }
  
  @Test
  public void test_getBnsServerInfo_retryAndFail() {
    // given
    final String privateKey = "b8059c31844941a8b37d4cac37b331d7b8059c31344941a8b37d4cac37b331d7";
    final KeyInfo keyInfo = KeyInfoUtil.buildKeyInfo(privateKey);
    final BnsServerInfoService service = new BnsServerInfoService("", 0);
    Assertions.assertThrows(Exception.class, () -> service.postBnsServerInfo(keyInfo));
  }
  
}