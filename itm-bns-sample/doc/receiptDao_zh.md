## ReceiptDao 功能說明

### 關於 ReceiptDao 功能說明文件

- ReceiptDao (Receipt Data Access Object) 的功能為存取回條並找出待驗證的回條，開發者可實作 ReceiptDao 將回條儲存在資料庫、雲端 ... 等其他服務中。在上一份文件中，您已經了解 Callback 的功能以及實作方式。在此份文件中，我們使用資料庫作為回條儲存地點引導您了解 BNS Client 如何透過 receiptDao 存取回條並找出待驗證的回條進行驗證

#### jdbcUrl 說明

此範例中，我們使用將回條儲存於資料庫之中，開發者可在 [BnsClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java) 中修改資料庫位址

```java
public static String JDBC_URL = "jdbc:sqlite:BnsDevice.db";
```

```java
@SneakyThrows
ReceiptDaoSample(@NonNull final String jdbcUrl) {
  this.receiptService = new ReceiptService(new JdbcPooledConnectionSource(jdbcUrl));
}
```

#### save 說明

BNS Client 收到 `ledgerInputResponse`  後，會將回條從 `ledgerInputResponse` 取出，並透過 `save` 方法儲存於資料庫

- 關於 `save` 方法的程式, 請參考 [ReceiptDaoSample.java](../src/main/java/com/itrustmachines/sample/ReceiptDaoSample.java)

  ```java
  @Override
  public boolean save(@NonNull final Receipt receipt) {
    log.info("save() start, receipt={}", receipt);
    if (receipt == null) {
      return false;
    } else {
      final Receipt savedReceipt = receiptService.save(receipt);
      log.info("save() end, savedReceipt={}", savedReceipt);
      return Objects.nonNull(savedReceipt);
    }
  }
  ```

- 關於資料庫存取的程式，請參考 [ReceiptService.java](../src/main/java/com/itrustmachines/sample/ReceiptService.java)

  ```java
  private ReceiptEntity save(@NonNull final ReceiptEntity receiptEntity) {
    sigClientDao.createIfNotExists(receiptEntity.getSigClient());
    sigServerDao.createIfNotExists(receiptEntity.getSigServer());
    return receiptDao.createIfNotExists(receiptEntity);
  }
  
  public Receipt save(@NonNull final Receipt receipt) {
    final ReceiptEntity createEntity = save(ReceiptUtil.toEntity(receipt));
    return ReceiptUtil.toDomain(createEntity);
  }
  ```

#### saveAll 說明

除了儲存單一回條外，開發者可選擇儲存一個 list 的回條

- 關於 `saveAll` 方法的程式, 請參考 [ReceiptDaoSample.java](../src/main/java/com/itrustmachines/sample/ReceiptDaoSample.java)

  ```java
  @Override
  public boolean saveAll(@NonNull final List<Receipt> receipts) {
    log.debug("saveAll() start, receiptsSize={}", receipts.size());
    final List<Receipt> savedReceipts = receiptService.saveAll(receipts);
    log.debug("saveAll() end, savedReceiptsSize={}", savedReceipts.size());
    return receipts.size() == savedReceipts.size();
  }
  ```

- 關於資料庫存取的程式，請參考 [ReceiptService.java](../src/main/java/com/itrustmachines/sample/ReceiptService.java)

  ```java
  public List<Receipt> saveAll(@NonNull final List<Receipt> receipts) {
    return receipts.stream()
                   .map(this::save)
                   .collect(Collectors.toList());
  }
  ```

#### findAll 說明

`findAll` 方法會找出所有儲存於資料庫的回條

- 關於 `findAll` 方法的程式, 請參考 [ReceiptDaoSample.java](../src/main/java/com/itrustmachines/sample/ReceiptDaoSample.java)
  
  ```java
  @Override
  public List<Receipt> findAll() {
    log.debug("findAll() start");
    final List<Receipt> receipts = receiptService.findAll();
    log.debug("findAll() end, receiptsSize={}", receipts.size());
    return receipts;
  }
  ```

- 關於資料庫存取的程式，請參考 [ReceiptService.java](../src/main/java/com/itrustmachines/sample/ReceiptService.java)
  
  ```java
  public List<Receipt> findAll() {
    return receiptDao.queryForAll()
                     .stream()
                     .map(ReceiptUtil::toDomain)
                     .collect(Collectors.toList());
  }
  ```

#### findAll 說明

除了找出資料庫所有的回條外，開發者可根據 `pageNumber` 和 `pageSize` 使用此 `findAll` 方法找尋特定回條

- 關於 `findAll` 方法的程式, 請參考 [ReceiptDaoSample.java](../src/main/java/com/itrustmachines/sample/ReceiptDaoSample.java)
  
  ```java
  @Override
  public List<Receipt> findAll(final int pageNumber, final int pageSize) {
    log.debug("findAll() start, pageNumber={}, pageSize={}", pageNumber, pageSize);
    final List<Receipt> receipts = receiptService.findAll(pageNumber, pageSize);
    log.debug("findAll() end, receiptsSize={}", receipts.size());
    return receipts;
  }
  ```

- 關於資料庫存取的程式，請參考 [ReceiptService.java](../src/main/java/com/itrustmachines/sample/ReceiptService.java)
  
  ```java
  public List<Receipt> findAll(final int pageNumber, final int pageSize) {
    final QueryBuilder<ReceiptEntity, Long> queryBuilder = receiptDao.queryBuilder();
    queryBuilder.offset((long) (pageNumber * pageSize))
                .limit((long) pageSize);
    return queryBuilder.query()
                       .stream()
                       .map(ReceiptUtil::toDomain)
                       .collect(Collectors.toList());
  }
  ```

#### countAll 說明

`countAll` 方法會計算當前資料庫回條的數量

- 關於 `countAll` 方法的程式, 請參考 [ReceiptDaoSample.java](../src/main/java/com/itrustmachines/sample/ReceiptDaoSample.java)

  ```java
  @Override
  public long countAll() {
    log.debug("countAll() start");
    final long size = receiptService.countAll();
    log.debug("countAll() end, size={}", size);
    return size;
  }
  ```

- 關於資料庫存取的程式，請參考 [ReceiptService.java](../src/main/java/com/itrustmachines/sample/ReceiptService.java)

  ```java
  public long countAll() {
    return receiptDao.countOf();
  }
  ```

#### findByLocator 說明

根據 `receiptLocator` 找出回條。`receiptLocator` 中包含 `indexValue` 和 `clearanceOrder`。BNS Client 會透過 `indexValue` 和 `clearanceOrder` 計算回條位置

- 關於 `findByLocator` 方法的程式, 請參考 [ReceiptDaoSample.java](../src/main/java/com/itrustmachines/sample/ReceiptDaoSample.java)

  ```java
  @Override
  public Receipt findByLocator(@NonNull final ReceiptLocator receiptLocator) {
    log.debug("findByLocator() start, receiptLocator={}", receiptLocator);
    final Receipt receipt = receiptService.findByLocator(receiptLocator);
    log.debug("findByLocator() end, receipt={}", receipt);
    return receipt;
  }
  ```

- 關於資料庫存取的程式，請參考 [ReceiptService.java](../src/main/java/com/itrustmachines/sample/ReceiptService.java)

  ```java
  public Receipt findByLocator(@NonNull final ReceiptLocator receiptLocator) {
    final QueryBuilder<ReceiptEntity, Long> queryBuilder = receiptDao.queryBuilder();
    queryBuilder.where()
                .eq(ReceiptEntity.CLEARANCE_ORDER_KEY, receiptLocator.getClearanceOrder())
                .and()
                .eq(ReceiptEntity.INDEX_VALUE_KEY, receiptLocator.getIndexValue());
    final ReceiptEntity entity = queryBuilder.queryForFirst();
    return ReceiptUtil.toDomain(entity);
  }
  ```

#### findByLocators 說明

根據一個 list 的 `receiptLocator` 找出一個 list 的回條。`receiptLocator` 中包含 `indexValue` 和 `clearanceOrder`。BNS Client 會透過 `indexValue` 和 `clearanceOrder` 計算回條位置

- 關於 `findByLocator` 方法的程式, 請參考 [ReceiptDaoSample.java](../src/main/java/com/itrustmachines/sample/ReceiptDaoSample.java)

  ```java
  @Override
  public List<Receipt> findByLocators(@NonNull final List<ReceiptLocator> receiptLocators) {
    log.debug("findByLocators() start, receiptLocatorsSize={}", receiptLocators.size());
    final List<Receipt> result = receiptService.findByLocators(receiptLocators);
    log.debug("findByLocators() end, resultSize={}", result.size());
    return result;
  }
  ```

- 關於資料庫存取的程式，請參考 [ReceiptService.java](../src/main/java/com/itrustmachines/sample/ReceiptService.java)

  ```java
  public List<Receipt> findByLocators(@NonNull final List<ReceiptLocator> receiptLocators) {
    return receiptLocators.stream()
                          .map(this::findByLocator)
                          .collect(Collectors.toList());
  }
  ```

#### delete 說明

`delete` 方法會會從回條取出 `indexvalue` and `clearanceOrder` 並呼叫 `deleteByLocator` 刪除相對應的回條

- 關於 `delete` 方法的程式, 請參考 [ReceiptDaoSample.java](../src/main/java/com/itrustmachines/sample/ReceiptDaoSample.java)

  ```java
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
  ```

- 關於資料庫存取的程式，請參考 [ReceiptService.java](../src/main/java/com/itrustmachines/sample/ReceiptService.java)

  ```java
  public int deleteByLocator(@NonNull final ReceiptLocator receiptLocator) {
    DeleteBuilder<ReceiptEntity, Long> deleteBuilder = receiptDao.deleteBuilder();
    deleteBuilder.where()
                 .eq(ReceiptEntity.INDEX_VALUE_KEY, receiptLocator.getIndexValue())
                 .and()
                 .eq(ReceiptEntity.CLEARANCE_ORDER_KEY, receiptLocator.getClearanceOrder());
    PreparedDelete<ReceiptEntity> preparedDelete = deleteBuilder.prepare();
    return receiptDao.delete(preparedDelete);
  }
  ```

#### deleteAll 說明

`deleteAll` 會將一個 list 的 回條刪除

- 關於 `deleteAll` 方法的程式, 請參考 [ReceiptDaoSample.java](../src/main/java/com/itrustmachines/sample/ReceiptDaoSample.java)

  ```java
  @Override
  public boolean deleteAll(@NonNull final List<Receipt> receipts) {
    log.debug("deleteAll() start, receiptsSize={}", receipts.size());
    final int deleteCount = receiptService.deleteAll();
    log.debug("deleteAll() end, deleteCount={}", deleteCount);
    return deleteCount >= 0;
  }
  ```

- 關於資料庫存取的程式，請參考 [ReceiptService.java](../src/main/java/com/itrustmachines/sample/ReceiptService.java)

  ```java
  public int deleteAll() {
    final DeleteBuilder<ReceiptEntity, Long> deleteBuilder = receiptDao.deleteBuilder();
    deleteBuilder.where();
    final PreparedDelete<ReceiptEntity> preparedDelete = deleteBuilder.prepare();
    return receiptDao.delete(preparedDelete);
  }
  ```

#### deleteAllByLocators 說明

`deleteAllByLocators` 會根據一個 list 的 `receiptLocators` 刪除相對應的回條。BNS Client 會根據 `receiptLocators` 找出回條位置並刪除

- 關於 `deleteAllByLocators` 方法的程式, 請參考 [ReceiptDaoSample.java](../src/main/java/com/itrustmachines/sample/ReceiptDaoSample.java)

  ```java
  @Override
  public boolean deleteAllByLocators(@NonNull final List<ReceiptLocator> receiptLocators) {
    log.debug("deleteAllByLocators() start, receiptLocatorsSize={}", receiptLocators.size());
    final int deleteCount = receiptService.deleteByLocatorList(receiptLocators);
    log.debug("deleteAllByLocators() end, deleteCount={}", deleteCount);
    return deleteCount == receiptLocators.size();
  }
  ```

- 關於資料庫存取的程式，請參考 [ReceiptService.java](../src/main/java/com/itrustmachines/sample/ReceiptService.java)

  ```java
  public int deleteByLocatorList(@NonNull final List<ReceiptLocator> receiptLocators) {
    return receiptLocators.stream()
                          .map(this::deleteByLocator)
                          .reduce(Integer::sum)
                          .orElse(0);
  }
  ```


您現在已經了解 BNS Client receiptDao 的功能。接下來我們將引導您了解並調整 BNS Client 的其他設定

----

- [下一頁 : BNS Client 其他設定](./other_setting_zh.md)
- [上一頁 : Callback 串接實作教學](./callback_zh.md)