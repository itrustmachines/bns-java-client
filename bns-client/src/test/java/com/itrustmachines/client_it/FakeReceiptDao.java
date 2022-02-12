package com.itrustmachines.client_it;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.itrustmachines.client.todo.BnsClientReceiptDao;
import com.itrustmachines.common.vo.Receipt;
import com.itrustmachines.common.vo.ReceiptLocator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FakeReceiptDao implements BnsClientReceiptDao {
  
  private final List<Receipt> savingReceipts = new ArrayList<>();
  
  @Override
  public boolean save(final Receipt receipt) {
    log.debug("save() receipt={}", receipt);
    if (receipt == null) {
      return false;
    } else {
      savingReceipts.add(receipt);
      return true;
    }
  }
  
  @Override
  public boolean saveAll(List<Receipt> receipts) {
    log.debug("saveAll() receipts={}", receipts);
    if (receipts == null || receipts.isEmpty()) {
      return false;
    } else {
      for (Receipt receipt : receipts) {
        save(receipt);
      }
      return true;
    }
  }
  
  @Override
  public List<Receipt> findAll() {
    log.debug("findAll() receipts size={}", savingReceipts.size());
    return savingReceipts.stream()
                         .sorted(Comparator.comparing(Receipt::getTimestamp))
                         .collect(Collectors.toList());
  }
  
  @Override
  public List<Receipt> findAll(int pageNumber, int pageSize) {
    log.debug("findAll() pageNumber={}, pageSize={}", pageNumber, pageSize);
    return savingReceipts.stream()
                         .sorted(Comparator.comparing(Receipt::getTimestamp))
                         .skip(pageNumber * pageSize)
                         .limit(pageSize)
                         .collect(Collectors.toList());
  }
  
  @Override
  public long countAll() {
    log.debug("countAll()");
    return savingReceipts.size();
  }
  
  @Override
  public Receipt findByLocator(ReceiptLocator receiptLocator) {
    log.debug("findByLocator() receiptLocator={}", receiptLocator);
    return savingReceipts.stream()
                         .filter(receipt -> {
                           return receipt.getClearanceOrder()
                                         .equals(receiptLocator.getClearanceOrder())
                               && receipt.getIndexValue()
                                         .equals(receiptLocator.getIndexValue());
                         })
                         .findFirst()
                         .orElse(null);
  }
  
  @Override
  public List<Receipt> findByLocators(List<ReceiptLocator> receiptLocators) {
    log.debug("findByLocators() receiptLocatorsSize={}", receiptLocators.size());
    return receiptLocators.stream()
                          .map(this::findByLocator)
                          .collect(Collectors.toList());
  }
  
  @Override
  public boolean delete(Receipt receipt) {
    log.debug("delete() begin");
    return savingReceipts.removeIf(receiptInList -> {
      return receiptInList.getClearanceOrder()
                          .equals(receipt.getClearanceOrder())
          && receiptInList.getIndexValue()
                          .equals(receipt.getIndexValue());
    });
  }
  
  @Override
  public boolean deleteAll(List<Receipt> receipts) {
    log.debug("deleteAll() receiptsSize={}", receipts.size());
    return savingReceipts.stream()
                         .allMatch(this::delete);
  }
  
  @Override
  public boolean deleteAllByLocators(List<ReceiptLocator> receiptLocators) {
    log.debug("deleteAllByLocators() receiptLocatorsSize={}", receiptLocators.size());
    return receiptLocators.stream()
                          .allMatch(receiptLocator -> {
                            return savingReceipts.removeIf(receipt -> {
                              return receipt.getClearanceOrder()
                                            .equals(receiptLocator.getClearanceOrder())
                                  && receipt.getIndexValue()
                                            .equals(receiptLocator.getIndexValue());
                            });
                          });
  }
  
}
