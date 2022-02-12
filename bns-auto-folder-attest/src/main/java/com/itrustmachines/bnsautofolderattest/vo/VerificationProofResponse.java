package com.itrustmachines.bnsautofolderattest.vo;

import java.util.List;

import com.itrustmachines.common.ethereum.EthereumEnv;
import com.itrustmachines.common.vo.ClearanceRecord;
import com.itrustmachines.common.vo.SpoSignature;
import com.itrustmachines.verification.vo.ExistenceProof;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerificationProofResponse {
  
  private String status;
  
  private String description;
  
  // VerificationProof:
  
  private String version;
  
  private String query;
  
  private Long timestamp;
  
  private String contractAddress;
  
  private String serverWalletAddress;
  
  private EthereumEnv env;
  
  private String nodeConnectionString;
  
  private List<ExistenceProof> existenceProofs;
  
  private List<ClearanceRecord> clearanceRecords;
  
  private SpoSignature sigServer;
  
}
