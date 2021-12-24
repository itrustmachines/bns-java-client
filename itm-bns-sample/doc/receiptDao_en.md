## Build the ReceiptDao Applications

### About the ReceiptDao

The ReceiptDao ( Receipt Data Access Object ) is the interface which help developer to store, find, delete the receipt. Developer can implement the code in ReceiptDao to store the receipt in memory, database, cloud platform or other services. In this document, we store the receipt in local database as an example to guide you understand the ReceiptDao.

### Prerequisites

- Complete the quickstarts document
- Complete the CMD document
- Complete the Callback document

#### jdbcUrl

BNS Client will create a new database with `JDBC_URL`. Developers can configure the database name or path in `JDBC_URL`

For the `JDBC_URL` setting, pleaser refer to [BnsClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)

```java
public static String JDBC_URL = "jdbc:sqlite:BnsDevice.db";
```

#### save

`save` method allows developers save the receipt after BNS Java Client extract receipt from `ledgerInputResponse`.

- [ReceiptDaoSample.java](../src/main/java/com/itrustmachines/sample/ReceiptDaoSample.java)

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

#### saveAll

`saveAll` methods allows developers save a list of receipt after BNS Java Client extract receipt from `ledgerInputResponse`.

- [ReceiptDaoSample.java](../src/main/java/com/itrustmachines/sample/ReceiptDaoSample.java)

  ```java
  @Override
  public boolean saveAll(@NonNull final List<Receipt> receipts) {
    log.debug("saveAll() start, receiptsSize={}", receipts.size());
    final List<Receipt> savedReceipts = receiptService.saveAll(receipts);
    log.debug("saveAll() end, savedReceiptsSize={}", savedReceipts.size());
    return receipts.size() == savedReceipts.size();
  }
  ```

#### findAll

`findAll` method allows developers find and return all the receipt in database

- [ReceiptDaoSample.java](../src/main/java/com/itrustmachines/sample/ReceiptDaoSample.java)
  
  ```java
  @Override
  public List<Receipt> findAll() {
    log.debug("findAll() start");
    final List<Receipt> receipts = receiptService.findAll();
    log.debug("findAll() end, receiptsSize={}", receipts.size());
    return receipts;
  }
  ```

#### findAll

`findAll` methods with arguments allows developers find specific receipt by giving `pageNumber` and `pageSize` arguments. BNS Java Client will calculate the offset by `pageNumber` x `pageSize` and use offset to find the receipt.

- [ReceiptDaoSample.java](../src/main/java/com/itrustmachines/sample/ReceiptDaoSample.java)
  
  ```java
  @Override
  public List<Receipt> findAll(final int pageNumber, final int pageSize) {
    log.debug("findAll() start, pageNumber={}, pageSize={}", pageNumber, pageSize);
    final List<Receipt> receipts = receiptService.findAll(pageNumber, pageSize);
    log.debug("findAll() end, receiptsSize={}", receipts.size());
    return receipts;
  }
  ```

#### countAll

`countAll` method return current count of receipt.

- [ReceiptDaoSample.java](../src/main/java/com/itrustmachines/sample/ReceiptDaoSample.java)

  ```java
  @Override
  public long countAll() {
    log.debug("countAll() start");
    final long size = receiptService.countAll();
    log.debug("countAll() end, size={}", size);
    return size;
  }
  ```

#### findByLocator

`findByLocator` method allows developers find the receipt by giving `receiptLocator`. `receiptLocator` includes `indexValue` and `clearanceOrder`. BNS Java Client will locate the receipt by `indexValue` and `clearanceOrder`.

- For the code of `findByLocator` method, please refer to [ReceiptDaoSample.java](../src/main/java/com/itrustmachines/sample/ReceiptDaoSample.java)

  ```java
  @Override
  public Receipt findByLocator(@NonNull final ReceiptLocator receiptLocator) {
    log.debug("findByLocator() start, receiptLocator={}", receiptLocator);
    final Receipt receipt = receiptService.findByLocator(receiptLocator);
    log.debug("findByLocator() end, receipt={}", receipt);
    return receipt;
  }
  ```

#### findByLocators

`findByLocators` method allows developers find a list of receipt by giving the list of `receiptLocator`.
`receiptLocator` includes `indexValue` and `clearanceOrder`. BNS Java Client will locate the receipt by `indexValue` and `clearanceOrder`.

- [ReceiptDaoSample.java](../src/main/java/com/itrustmachines/sample/ReceiptDaoSample.java)

  ```java
  @Override
  public List<Receipt> findByLocators(@NonNull final List<ReceiptLocator> receiptLocators) {
    log.debug("findByLocators() start, receiptLocatorsSize={}", receiptLocators.size());
    final List<Receipt> result = receiptService.findByLocators(receiptLocators);
    log.debug("findByLocators() end, resultSize={}", result.size());
    return result;
  }
  ```

#### delete

`delete` method allows developers delete the receipt.

- [ReceiptDaoSample.java](../src/main/java/com/itrustmachines/sample/ReceiptDaoSample.java)

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

#### deleteAll

`delete` method allows developers delete a list of receipt.

- [ReceiptDaoSample.java](../src/main/java/com/itrustmachines/sample/ReceiptDaoSample.java)

  ```java
  @Override
  public boolean deleteAll(@NonNull final List<Receipt> receipts) {
    log.debug("deleteAll() start, receiptsSize={}", receipts.size());
    final int deleteCount = receiptService.deleteAll();
    log.debug("deleteAll() end, deleteCount={}", deleteCount);
    return deleteCount >= 0;
  }
  ```

#### deleteAllByLocators

`deleteAllByLocators` method allows developers delete a list of receipts by giving a list of `receiptLocator`.
`receiptLocator` includes `indexValue` and `clearanceOrder`. BNS Client will use `indexValue` and `clearanceOrder` to find and delete the receipts.

- For the code of `deleteAllByLocators` method, please refer to [ReceiptDaoSample.java](../src/main/java/com/itrustmachines/sample/ReceiptDaoSample.java)

  ```java
  @Override
  public boolean deleteAllByLocators(@NonNull final List<ReceiptLocator> receiptLocators) {
    log.debug("deleteAllByLocators() start, receiptLocatorsSize={}", receiptLocators.size());
    final int deleteCount = receiptService.deleteByLocatorList(receiptLocators);
    log.debug("deleteAllByLocators() end, deleteCount={}", deleteCount);
    return deleteCount == receiptLocators.size();
  }
  ```

#### getNeedVerifyReceiptLocatorMap

`getNeedVerifyReceiptLocatorMap` method allows BNS Client find out the receipts need to be verified. BNS Client will verify the receipt, if the receipt's `clearanceOrder` is less than `doneClearanceOrder`  

- [BnsClientReceiptService.java](../../bns-client/src/main/java/com/itrustmachines/client/service/BnsClientReceiptService.java)

  ```java
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
  ```

----
Build the ReceiptDao Applications document is now complete. Next, learn how to configure the remaining setting of BNS Client

## Next Steps

Next Page : [Configure the Setting of BNS Client](./other_setting_en.md)  
Last Page : [Build the Callback Applications](./callback_en.md)