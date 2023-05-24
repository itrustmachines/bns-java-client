package com.itrustmachines.sample;

import java.sql.SQLException;
import java.util.List;

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
public class ReceiptDaoSample implements BnsClientReceiptDao {
  
  private final ReceiptService receiptService;
  
  ReceiptDaoSample(@NonNull final String jdbcUrl) throws SQLException {
    this.receiptService = new ReceiptService(new JdbcPooledConnectionSource(jdbcUrl));
  }
  
  // 儲存每次Ledger input完對應的Receipt
  @SneakyThrows
  @Override
  public void save(@NonNull final Receipt receipt) {
    log.info("save() start, receipt={}", receipt);
    receiptService.save(receipt);
    log.info("save() end");
  }
  
  @SneakyThrows
  @Override
  public List<Receipt> findPageByNotVerifiedAndAddressEqualsIgnoreCaseAndClearanceOrderLessThenEqual(String address,
      long clearanceOrder, int pageNumber, int pageSize) {
    return receiptService.findPageByNotVerifiedAndAddressEqualsIgnoreCaseAndClearanceOrderLessThenEqual(address,
        clearanceOrder, pageNumber, pageSize);
  }
  
  // 將已經驗證完成的Receipt刪除
  @SneakyThrows
  @Override
  public void delete(@NonNull final Receipt receipt, boolean verifyPassed) {
    log.debug("delete() start, receipt={}", receipt);
    final ReceiptLocator receiptLocator = ReceiptLocator.builder()
                                                        .clearanceOrder(receipt.getClearanceOrder())
                                                        .indexValue(receipt.getIndexValue())
                                                        .build();
    receiptService.deleteByLocator(receiptLocator);
    log.debug("delete() end");
  }
  
}
