package com.itrustmachines.client.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BnsServerInfo {
  
  private String serverWalletAddress;
  private String contractAddress;
  private Boolean registerAuthConfig;
  
}