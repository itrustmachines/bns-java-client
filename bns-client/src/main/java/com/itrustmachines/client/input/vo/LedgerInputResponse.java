package com.itrustmachines.client.input.vo;


import java.util.List;

import com.itrustmachines.common.vo.Receipt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LedgerInputResponse {
  
  private String status;
  private String description;
  private Receipt receipt;
  private List<Long> doneClearanceOrderList;
  
}
