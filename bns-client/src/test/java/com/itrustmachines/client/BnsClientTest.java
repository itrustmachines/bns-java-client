package com.itrustmachines.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.itrustmachines.client.config.BnsClientConfig;
import com.itrustmachines.client.todo.BnsClientCallback;
import com.itrustmachines.client.todo.BnsClientReceiptDao;

// TODO fix test
@Disabled
public class BnsClientTest {
  
  private static final String bnsServerUrl = "https://azure-dev-membership.itm.monster:8088";
  private static final String privateKey = "e0c07f854d30679c32a52af9d3cc72ac13923a4e77ba97337fb2e0cc295ddfde";
  
  @Test
  public void test_init() {
    final BnsClientConfig config = BnsClientConfig.builder()
                                                  .bnsServerUrl(bnsServerUrl)
                                                  .privateKey(privateKey)
                                                  .nodeUrl(
                                                      "https://rinkeby.infura.io/v3/c889a8d21e2b4179ab331713efb92a7d")
                                                  .build();
    BnsClientCallback callback = mock(BnsClientCallback.class);
    BnsClientReceiptDao receiptDao = mock(BnsClientReceiptDao.class);
    final BnsClient bnsClient = BnsClient.init(config, callback, receiptDao);
    assertThat(bnsClient).isNotNull();
    bnsClient.close();
  }
  
}