package com.itrustmachines.client.input.vo;

import com.itrustmachines.client.service.ReceiptEventProcessor;
import com.itrustmachines.client.service.ReceiptLocatorService;
import com.itrustmachines.client.service.BnsClientReceiptService;
import com.itrustmachines.client.todo.BnsClientCallback;
import com.itrustmachines.client.verify.service.DoneClearanceOrderEventProcessor;
import com.itrustmachines.common.vo.KeyInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LedgerInputServiceParams {
  
  private KeyInfo keyInfo;
  private String bnsServerUrl;
  private BnsClientCallback callback;
  private BnsClientReceiptService bnsClientReceiptService;
  private ReceiptLocatorService receiptLocatorService;
  private DoneClearanceOrderEventProcessor doneClearanceOrderEventProcessor;
  private ReceiptEventProcessor receiptEventProcessor;
  private int retryDelaySec;
  
}
