package com.itrustmachines.client.config;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.lang3.math.NumberUtils;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = { "privateKey", "nodeUserName", "nodePassword" })
@Builder
@Slf4j
public class BnsClientConfig {
  
  public static final int DEFAULT_VERIFY_BATCH_SIZE = 10;
  public static final int DEFAULT_VERIFY_DELAY_SEC = 1;
  public static final int DEFAULT_RETRY_DELAY_SEC = 5;

  private String privateKey;
  private String bnsServerUrl;
  private String email;
  
  // node info
  private String nodeUrl;
  private Boolean nodeNeedAuth;
  private String nodeUserName;
  private String nodePassword;
  
  private int verifyBatchSize;
  private int verifyDelaySec;
  private int retryDelaySec;
  
  @SneakyThrows
  public static BnsClientConfig load(@NonNull final String configFilePath) {
    final Properties props = new Properties();
    props.load(new FileInputStream(configFilePath));

    final String privateKey = props.getProperty("privateKey");
    final String bnsServerUrl = props.getProperty("bnsServerUrl");
    final String email = props.getProperty("email");
    final String nodeUrl = props.getProperty("nodeUrl");
    final Boolean nodeNeedAuth = Boolean.valueOf(props.getProperty("nodeNeedAuth"));
    final String nodeUserName = props.getProperty("nodeUserName");
    final String nodePassword = props.getProperty("nodePassword");
    
    int verifyBatchSize = NumberUtils.toInt(props.getProperty("verifyBatchSize"), DEFAULT_VERIFY_BATCH_SIZE);
    if (verifyBatchSize <= 0) {
      verifyBatchSize = DEFAULT_VERIFY_BATCH_SIZE;
    }
    int verifyDelaySec = NumberUtils.toInt(props.getProperty("verifyDelaySec"), DEFAULT_VERIFY_DELAY_SEC);
    int retryDelaySec = NumberUtils.toInt(props.getProperty("retryDelaySec"), DEFAULT_RETRY_DELAY_SEC);

    final BnsClientConfig result = BnsClientConfig.builder()
                                                  .privateKey(privateKey)
                                                  .bnsServerUrl(bnsServerUrl)
                                                  .email(email)
                                                  .nodeUrl(nodeUrl)
                                                  .nodeNeedAuth(nodeNeedAuth)
                                                  .nodeUserName(nodeUserName)
                                                  .nodePassword(nodePassword)
                                                  .verifyBatchSize(verifyBatchSize)
                                                  .verifyDelaySec(verifyDelaySec)
                                                  .retryDelaySec(retryDelaySec)
                                                  .build();
    log.info("load[{}] config={}", configFilePath, result);
    return result;
  }
  
}
