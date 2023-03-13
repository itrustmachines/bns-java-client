package com.itrustmachines.client.verify.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.itrustmachines.client.config.BnsClientConfig;
import com.itrustmachines.client.todo.BnsClientCallback;
import com.itrustmachines.common.constants.StatusConstants;
import com.itrustmachines.common.vo.MerkleProof;
import com.itrustmachines.common.vo.ReceiptLocator;

public class MerkleProofServiceTest {
  
  final String bnsServerUrl = "https://bns.itrustmachines.com/";
  
  @Test
  public void test_obtainMerkleProof_error() {
    // given
    final BnsClientCallback callback = mock(BnsClientCallback.class);
    MerkleProofService service = new MerkleProofService(bnsServerUrl, callback,
        BnsClientConfig.DEFAULT_RETRY_DELAY_SEC);
    final ReceiptLocator locator = ReceiptLocator.builder()
                                                 .indexValue("TestError_R0")
                                                 .clearanceOrder(1L)
                                                 .build();
    
    // when
    final MerkleProof result = service.postMerkleProof(locator);
    
    // then
    assertThat(result).isNull();
    verify(callback, times(1)).obtainMerkleProof(any(), any());
  }
  
  @Test
  public void test_obtainMerkleProof_retryAndFail() {
    // given
    final BnsClientCallback callback = mock(BnsClientCallback.class);
    MerkleProofService service = new MerkleProofService("", callback, 0);
    final ReceiptLocator locator = ReceiptLocator.builder()
                                                 .indexValue("TestError_R0")
                                                 .clearanceOrder(1L)
                                                 .build();
    
    // when,then
    Assertions.assertThrows(Exception.class, () -> service.postMerkleProof(locator));
  }
  
  @Test
  public void test_getMerkleProofResponse_error() {
    // given
    final BnsClientCallback callback = mock(BnsClientCallback.class);
    MerkleProofService service = new MerkleProofService(bnsServerUrl, callback,
        BnsClientConfig.DEFAULT_RETRY_DELAY_SEC);
    final ReceiptLocator locator = ReceiptLocator.builder()
                                                 .indexValue("TestError_R0")
                                                 .clearanceOrder(1L)
                                                 .build();
    
    final MerkleProofService.MerkleProofResponse result = service.getMerkleProofResponse(locator);
    
    // then
    assertThat(result.getMerkleProof()).isNull();
  }
  
  @Test
  public void test_checkResponse() {
    // given
    final MerkleProofService service = new MerkleProofService("", mock(BnsClientCallback.class),
        BnsClientConfig.DEFAULT_RETRY_DELAY_SEC);
    
    final MerkleProofService.MerkleProofResponse okRes = MerkleProofService.MerkleProofResponse.builder()
                                                                                               .status(
                                                                                                   StatusConstants.OK.name())
                                                                                               .build();
    final MerkleProofService.MerkleProofResponse errRes = MerkleProofService.MerkleProofResponse.builder()
                                                                                                .status(
                                                                                                    StatusConstants.ERROR.name())
                                                                                                .description(
                                                                                                    "MerkleProof not found")
                                                                                                .build();
    // when,then
    assertThat(service.checkResponse(okRes)).isTrue();
    assertThat(service.checkResponse(errRes)).isTrue();
    assertThat(service.checkResponse(null)).isFalse();
  }
  
}