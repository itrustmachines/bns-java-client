package com.itrustmachines.client.register.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.web3j.crypto.Credentials;

import com.itrustmachines.client.config.BnsClientConfig;
import com.itrustmachines.client.todo.BnsClientCallback;
import com.itrustmachines.common.vo.KeyInfo;

// TODO fix test
@Disabled
public class RegisterServiceTest {
  
  @Test
  public void test_register_check_register() {
    // given
    final String email = "daniellin456@gmail.com";
    final String bnsServerUrl = "https://azure-dev-membership.itm.monster:8088";
    final String privateKey = "e0c07f854d30679c32a52af9d3cc72ac13923a4e77ba97337fb2e0cc295ddfde";
    BnsClientCallback callback = mock(BnsClientCallback.class);
    final Credentials credentials = Credentials.create(privateKey);
    final KeyInfo keyInfo = KeyInfo.builder()
                                   .address(credentials.getAddress())
                                   .privateKey(privateKey)
                                   .publicKey(credentials.getEcKeyPair()
                                                         .getPublicKey()
                                                         .toString(16))
                                   .build();
    
    RegisterService service = new RegisterService(bnsServerUrl, callback, keyInfo,
        BnsClientConfig.DEFAULT_RETRY_DELAY_SEC, email);
    
    // when
    final boolean result = service.checkRegister();
    
    // then
    assertThat(result).isTrue();
  }
  
  @Test
  public void test_register_retryAndFail() {
    // given
    final RegisterService service = new RegisterService("", mock(BnsClientCallback.class), KeyInfo.builder()
                                                                                                  .privateKey("123")
                                                                                                  .build(),
        0, "");
    // when,then
    Assertions.assertThrows(Exception.class, service::register);
  }
  
  @Test
  public void test_checkResponse() {
    // given
    final RegisterService service = new RegisterService("", null, null, BnsClientConfig.DEFAULT_RETRY_DELAY_SEC, "");
    
    String okRes = "true";
    
    // when, then
    // response ok
    assertThat(service.checkResponse(okRes)).isTrue();
    
    // response is null
    assertThat(service.checkResponse(null)).isFalse();
  }
  
}