package com.itrustmachines.client.service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import com.itrustmachines.client.todo.BnsClientReceiptDao;
import com.itrustmachines.common.vo.Receipt;
import com.itrustmachines.common.vo.ReceiptLocator;

import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @link com.itrustmachines.sample.ReceiptDaoSample;
 */
@ToString
@Slf4j
public class BnsClientReceiptService {
  
  public static final int INIT_RECEIPT_PAGE_SIZE = 10;
  private final BnsClientReceiptDao dao;
  private final ReadWriteLock rwLock;
  
  // key: CO, value: need verified indexValues with CO=key
  private final Map<Long, Set<String>> receiptLocatorsMap;
  
  private boolean isCloseCalled;
  
  public BnsClientReceiptService(@NonNull final BnsClientReceiptDao dao) {
    this.dao = dao;
    this.rwLock = new ReentrantReadWriteLock();
    this.receiptLocatorsMap = new LinkedHashMap<>();
    log.info("new instance={}", this);
    final ExecutorService executorService = Executors.newSingleThreadExecutor();
    executorService.submit(() -> {
      int pageNumber = 0;
      List<Receipt> receipts;
      do {
        receipts = this.findAll(pageNumber, INIT_RECEIPT_PAGE_SIZE);
        rwLock.writeLock()
              .lock();
        try {
          receipts.forEach(this::addNeedVerifyReceiptLocatorByReceipt);
        } finally {
          rwLock.writeLock()
                .unlock();
        }
        pageNumber++;
      } while (!receipts.isEmpty() && !isCloseCalled);
    });
  }
  
  public boolean save(@NonNull final Receipt receipt) {
    log.debug("save() receipt={}", receipt);
    rwLock.writeLock()
          .lock();
    try {
      addNeedVerifyReceiptLocatorByReceipt(receipt);
      return dao.save(receipt);
    } finally {
      rwLock.writeLock()
            .unlock();
    }
  }
  
  public boolean saveAll(@NonNull final List<Receipt> receipts) {
    log.debug("saveAll() receipts={}", receipts);
    rwLock.writeLock()
          .lock();
    try {
      receipts.forEach(this::addNeedVerifyReceiptLocatorByReceipt);
      return dao.saveAll(receipts);
    } finally {
      rwLock.writeLock()
            .unlock();
    }
  }
  
  public List<Receipt> findAll() {
    log.debug("findAll()");
    rwLock.readLock()
          .lock();
    try {
      return dao.findAll();
    } finally {
      rwLock.readLock()
            .unlock();
    }
  }
  
  public List<Receipt> findAll(final int pageNumber, final int pageSize) {
    log.debug("findAll() pageNumber={}, pageSize={}", pageNumber, pageSize);
    rwLock.readLock()
          .lock();
    try {
      return dao.findAll(pageNumber, pageSize);
    } finally {
      rwLock.readLock()
            .unlock();
    }
  }
  
  public Receipt findByLocator(@NonNull final ReceiptLocator receiptLocator) {
    log.debug("findByLocator() receiptLocator={}", receiptLocator);
    rwLock.readLock()
          .lock();
    try {
      return dao.findByLocator(receiptLocator);
    } finally {
      rwLock.readLock()
            .unlock();
    }
  }
  
  public List<Receipt> findByLocators(@NonNull final List<ReceiptLocator> receiptLocators) {
    log.debug("findByLocators() receiptLocators={}", receiptLocators);
    rwLock.readLock()
          .lock();
    try {
      return dao.findByLocators(receiptLocators);
    } finally {
      rwLock.readLock()
            .unlock();
    }
  }
  
  public boolean delete(@NonNull final Receipt receipt) {
    log.debug("delete() receipt={}", receipt);
    rwLock.writeLock()
          .lock();
    try {
      removeNeedVerifyReceiptLocatorByReceipt(receipt);
      return dao.delete(receipt);
    } finally {
      rwLock.writeLock()
            .unlock();
    }
  }
  
  public boolean deleteAll(@NonNull final List<Receipt> receipts) {
    log.debug("deleteAll() receipts={}", receipts);
    rwLock.writeLock()
          .lock();
    try {
      receipts.forEach(this::removeNeedVerifyReceiptLocatorByReceipt);
      return dao.deleteAll(receipts);
    } finally {
      rwLock.writeLock()
            .unlock();
    }
  }
  
  public boolean deleteAllByLocators(@NonNull final List<ReceiptLocator> receiptLocators) {
    log.debug("deleteAllByLocators() receiptLocators={}", receiptLocators);
    rwLock.writeLock()
          .lock();
    try {
      receiptLocators.forEach(this::removeNeedVerifyReceiptLocatorByReceiptLocator);
      return dao.deleteAllByLocators(receiptLocators);
    } finally {
      rwLock.writeLock()
            .unlock();
    }
  }
  
  /**
   * @return key: CO, value: indexValue set
   */
  public Map<Long, Set<String>> getNeedVerifyReceiptLocatorMap(final long doneClearanceOrder) {
    log.debug("getNeedVerifyReceiptLocatorMap() doneClearanceOrder={}", doneClearanceOrder);
    rwLock.readLock()
          .lock();
    try {
      final Map<Long, Set<String>> result = new LinkedHashMap<>();
      final List<Long> coList = receiptLocatorsMap.keySet()
                                                  .stream()
                                                  .filter(co -> co <= doneClearanceOrder)
                                                  .sorted(Long::compareTo)
                                                  .collect(Collectors.toList());
      coList.forEach(co -> result.put(co, new LinkedHashSet<>(receiptLocatorsMap.get(co))));
      return result;
    } finally {
      rwLock.readLock()
            .unlock();
    }
  }
  
  public void close() {
    log.debug("close()");
    isCloseCalled = true;
  }
  
  private void addNeedVerifyReceiptLocatorByReceipt(@NonNull final Receipt receipt) {
    final Set<String> indexValueSet = receiptLocatorsMap.computeIfAbsent(receipt.getClearanceOrder(),
        k -> new LinkedHashSet<>());
    indexValueSet.add(receipt.getIndexValue());
  }
  
  private void removeNeedVerifyReceiptLocatorByReceipt(@NonNull final Receipt receipt) {
    final ReceiptLocator receiptLocator = ReceiptLocator.builder()
                                                        .clearanceOrder(receipt.getClearanceOrder())
                                                        .indexValue(receipt.getIndexValue())
                                                        .build();
    removeNeedVerifyReceiptLocatorByReceiptLocator(receiptLocator);
  }
  
  private void removeNeedVerifyReceiptLocatorByReceiptLocator(@NonNull final ReceiptLocator receiptLocator) {
    final Set<String> indexValueSet = receiptLocatorsMap.get(receiptLocator.getClearanceOrder());
    if (Objects.nonNull(indexValueSet)) {
      indexValueSet.remove(receiptLocator.getIndexValue());
      if (indexValueSet.isEmpty()) {
        receiptLocatorsMap.remove(receiptLocator.getClearanceOrder());
      }
    }
  }
  
}
