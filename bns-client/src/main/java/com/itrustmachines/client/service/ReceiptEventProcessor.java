package com.itrustmachines.client.service;

import com.itrustmachines.client.todo.BnsClientCallback;
import com.itrustmachines.client.vo.ReceiptEvent;

import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@ToString
@Slf4j
public class ReceiptEventProcessor {
  
  private final BnsClientCallback callback;
  private final BnsClientReceiptService receiptService;
  
  public ReceiptEventProcessor(final @NonNull BnsClientCallback callback,
      final @NonNull BnsClientReceiptService receiptService) {
    this.callback = callback;
    this.receiptService = receiptService;
    log.info("new instance={}", this);
  }
  
  public void handleReceiptEvent(final @NonNull ReceiptEvent event) {
    log.debug("handleReceiptEvent() begin, event={}", event);
    try {
      callback.obtainReceiptEvent(event);
    } catch (Exception e) {
      if (Thread.currentThread()
                .isInterrupted()) {
        throw e;
      }
      log.error("handleReceiptEvent() callback obtainReceiptEvent error, event={}", event, e);
    }
    final boolean isReceiptSaved = receiptService.save(event.getReceipt());
    log.debug("handleReceiptEvent() end, isReceiptSaved={}", isReceiptSaved);
  }
  
}
