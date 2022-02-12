package com.itrustmachines.client.todo;

import java.util.List;

import com.itrustmachines.common.vo.Receipt;
import com.itrustmachines.common.vo.ReceiptLocator;

public interface BnsClientReceiptDao {
  
  boolean save(Receipt receipt);
  
  boolean saveAll(List<Receipt> receipts);
  
  List<Receipt> findAll();
  
  List<Receipt> findAll(int pageNumber, int pageSize);
  
  long countAll();
  
  Receipt findByLocator(ReceiptLocator receiptLocator);
  
  List<Receipt> findByLocators(List<ReceiptLocator> receiptLocators);
  
  boolean delete(Receipt receipt);
  
  boolean deleteAll(List<Receipt> receipts);
  
  boolean deleteAllByLocators(List<ReceiptLocator> receiptLocators);
  
}
