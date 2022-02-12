package com.itrustmachines.client.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BnsClientConfigTest {
  
  @Test
  public void test_loadConfig() {
    String configFilePath = "./src/test/resources/application.properties";
    assertThat(Paths.get(configFilePath)).isRegularFile();
    
    final BnsClientConfig config = BnsClientConfig.load(configFilePath);
    assertThat(config).isNotNull();
  }
  
}