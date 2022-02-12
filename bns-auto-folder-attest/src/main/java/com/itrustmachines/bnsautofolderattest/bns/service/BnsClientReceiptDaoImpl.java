package com.itrustmachines.bnsautofolderattest.bns.service;

import java.util.List;
import java.util.Objects;

import com.itrustmachines.client.todo.BnsClientReceiptDao;
import com.itrustmachines.common.vo.Receipt;
import com.itrustmachines.common.vo.ReceiptLocator;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * 使用者須自行實作儲存Receipt的方法，Client在驗證資料時，會使用存取Receipt的方法。
 * 方法說明及規範請參照使用流程，此Dao示範方法為：利用memory儲存receipt
 **/
@Slf4j
public class BnsClientReceiptDaoImpl implements BnsClientReceiptDao {
  
  private final ReceiptService receiptService;
  
  @SneakyThrows
  public BnsClientReceiptDaoImpl(@NonNull final String jdbcUrl) {
    this.receiptService = new ReceiptService(new JdbcPooledConnectionSource(jdbcUrl));
  }
  
  // 儲存每次Ledger input完對應的Receipt
  @Override
  public boolean save(@NonNull final Receipt receipt) {
    log.debug("save() start, receipt={}", receipt);
    final Receipt savedReceipt = receiptService.save(receipt);
    log.debug("save() end, savedReceipt={}", savedReceipt);
    return Objects.nonNull(savedReceipt);
  }
  
  @Override
  public boolean saveAll(@NonNull final List<Receipt> receipts) {
    log.debug("saveAll() start, receiptsSize={}", receipts.size());
    final List<Receipt> savedReceipts = receiptService.saveAll(receipts);
    log.debug("saveAll() end, savedReceiptsSize={}", savedReceipts.size());
    return receipts.size() == savedReceipts.size();
  }
  
  @Override
  public List<Receipt> findAll() {
    log.debug("findAll() start");
    final List<Receipt> receipts = receiptService.findAll();
    log.debug("findAll() end, receiptsSize={}", receipts.size());
    return receipts;
  }
  
  @Override
  public List<Receipt> findAll(final int pageNumber, final int pageSize) {
    log.debug("findAll() start, pageNumber={}, pageSize={}", pageNumber, pageSize);
    final List<Receipt> receipts = receiptService.findAll(pageNumber, pageSize);
    log.debug("findAll() end, receiptsSize={}", receipts.size());
    return receipts;
  }
  
  @Override
  public long countAll() {
    log.debug("countAll() start");
    final long size = receiptService.countAll();
    log.debug("countAll() end, size={}", size);
    return size;
  }
  
  @Override
  public Receipt findByLocator(@NonNull final ReceiptLocator receiptLocator) {
    log.debug("findByLocator() start, receiptLocator={}", receiptLocator);
    final Receipt receipt = receiptService.findByLocator(receiptLocator);
    log.debug("findByLocator() end, receipt={}", receipt);
    return receipt;
  }
  
  @Override
  public List<Receipt> findByLocators(@NonNull final List<ReceiptLocator> receiptLocators) {
    log.debug("findByLocators() start, receiptLocatorsSize={}", receiptLocators.size());
    final List<Receipt> result = receiptService.findByLocators(receiptLocators);
    log.debug("findByLocators() end, resultSize={}", result.size());
    return result;
  }
  
  // 將已經驗證完成的Receipt刪除
  @Override
  public boolean delete(@NonNull final Receipt receipt) {
    log.debug("delete() start, receipt={}", receipt);
    final ReceiptLocator receiptLocator = ReceiptLocator.builder()
                                                        .clearanceOrder(receipt.getClearanceOrder())
                                                        .indexValue(receipt.getIndexValue())
                                                        .build();
    final int deleteCount = receiptService.deleteByLocator(receiptLocator);
    log.debug("delete() end, deleteCount={}", deleteCount);
    return deleteCount == 1;
  }
  
  @Override
  public boolean deleteAll(@NonNull final List<Receipt> receipts) {
    log.debug("deleteAll() start, receiptsSize={}", receipts.size());
    final int deleteCount = receiptService.deleteAll();
    log.debug("deleteAll() end, deleteCount={}", deleteCount);
    return deleteCount >= 0;
  }
  
  @Override
  public boolean deleteAllByLocators(@NonNull final List<ReceiptLocator> receiptLocators) {
    log.debug("deleteAllByLocators() start, receiptLocatorsSize={}", receiptLocators.size());
    final int deleteCount = receiptService.deleteByLocatorList(receiptLocators);
    log.debug("deleteAllByLocators() end, deleteCount={}", deleteCount);
    return deleteCount == receiptLocators.size();
  }
  
}
