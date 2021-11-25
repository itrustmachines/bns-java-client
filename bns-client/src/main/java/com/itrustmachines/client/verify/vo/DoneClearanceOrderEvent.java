package com.itrustmachines.client.verify.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoneClearanceOrderEvent {
  
  public enum Source {
    
    LEDGER_INPUT_RESULT, DIRECT_CALL
  
  }
  
  private Source source;
  private long doneClearanceOrder;
  
}
