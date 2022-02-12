package com.itrustmachines.bnsautofolderattest.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Cmd {
  
  private AttestationType type;
  private String fileName; // required for bns front-end
  private Long lastModifiedTime;
  private String fileHash;
  private Long timestamp;
  private String description; // required for bns front-end
  
}
