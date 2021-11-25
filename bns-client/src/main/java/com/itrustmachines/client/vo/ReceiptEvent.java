package com.itrustmachines.client.vo;

import com.itrustmachines.common.vo.Receipt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReceiptEvent {
  
  public enum Source {
    
    LEDGER_INPUT_RESULT
  
  }
  
  private Source source;
  private Receipt receipt;
  
}
