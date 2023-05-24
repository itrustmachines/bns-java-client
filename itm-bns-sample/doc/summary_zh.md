## 主程式說明

### 關於主程式說明文件

若您想對我們 SDK 的運作流程有更清楚的了解或是想更改 SDK 其他程式，我們建議您可以閱讀此份文件。在這份文件中，我們將依照 BNS Client 主程式的運作流程整合前面的教學內容及設定。閱讀此文件前，我們希望您已經了解所有 SDK 的設定、Callback、ReceiptDao 的實作方式。

### Configuration File

從 `PROP_PATH_LIST` 尋找設定檔並載入

- [BnsClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)
  
  ```java
  final String configPath = FileUtil.findFile(SAMPLE_PROPERTIES, PROP_PATH_LIST);
  final BnsClientConfig config = BnsClientConfig.load(configPath);
  log.info("BnsClientConfig={}", config);
  ```

### Callback 服務

- BNS Client 建立 Callback 服務，Callback 實作說明請參考 [Callback 串接實作說明](./callback_zh.md)

- [BnsClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)

  ```java
  final BnsClientCallback callback = new CallbackSample();
  ```

### ReceiptDao 服務

- BNS Client 透過 `JDBC_URL` 建立 receiptDao 服務，receiptDao 詳細說明請參考 [ReceiptDao 功能說明](./receiptDao_zh.md)

- [BnsClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)

  ```java
  final BnsClientReceiptDao receiptDao = new ReceiptDaoSample(JDBC_URL);
  ```

### 初始化 BNS Client

- 為了與 BNS Server 建立上鏈存證與驗證服務，BNS Client 會呼叫此方法進行 sample.properties, receiptDao, callback 的初始化，並向 BNS Server 註冊

- [BnsClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)

  ```java
  final BnsClient bnsClient = BnsClient.init(config, callback, receiptDao);
  ```

  ```java
  public static BnsClient init(@NonNull final BnsClientConfig config, @NonNull final BnsClientCallback callback,
      @NonNull final BnsClientReceiptDao receiptDao) {
    if (config.getVerifyBatchSize() <= 0) {
      config.setVerifyBatchSize(BnsClientConfig.DEFAULT_VERIFY_BATCH_SIZE);
    }
    BnsClient bnsClient = new BnsClient(config, callback, receiptDao);
    final boolean isRegistered = bnsClient.registerService.register();
    if (!isRegistered) {
      String errMsg = "BnsClient register fail";
      log.error("init() error, {}", errMsg);
      throw new RuntimeException(errMsg);
    }
    return bnsClient;
  }
  ```

### 資料存證

- BNS Client 初始化且成功註冊後，將 CMD 轉換成 JSON 資料型別並呼叫 `ledgerInput` 將資料送至 BNS Server 清算上鏈。CMD 詳細資訊請參考 [CMD實作說明](./cmd_zh.md)

- `ledgerInput` 會將 `cmdJSON` 以及其他資訊儲存至 `ledgerInputRequest` 並呼叫 `createLedgerInputByCmd` callback 方法回傳 `ledgerInputRequest` 內的資訊

- BNS Client 成功建立 `ledgerInputRequest` 後，會將 `ledgerInputRequest` 送至 BNS Server 清算上鏈並收到 BNS Server 回傳的 `ledgerInputResponse` 並呼叫 `obtainLedgerInputResponse` callback 方法回傳 `ledgerInputResponse` 內的資訊

- BNS Client 會呼叫 `handleReceipt` 將回條從 `ledgerInputResponse` 取出並儲存至資料庫

- BNS Client 會呼叫 `handleDoneClearanceOrderList` 透過 `doneClearanceOrder` 找出並驗證資料庫中回條的 `clearanceOrder` 小於當前的 `doneClearanceOrder` 並呼叫 `obtainReceiptEvent` 和  `obtainDoneClearanceOrderEvent` callback 將 `receipt` 和 `doneClearanceOrder` 內的資訊回傳。`receipt` 和 `doneClearanceOrder` 位於 `ledgerInputResponse` 內。
  
- 兩種 `ledgerInput` 方式

  1. `ledgerInput(@NonNull final String indexValueKey, @NonNull final String cmdJson)`

     若您有多個裝置或 `indexValueKey` 使用同一份 BNS Client，我們建議您使用此方法並自行設定 `indexValueKey`

     ```java
     public LedgerInputResponse ledgerInput(@NonNull final String indexValueKey, @NonNull final String cmdJson) {
        return ledgerInputService.ledgerInput(indexValueKey, cmdJson);
     }
     ```
  
  2. `ledgerInput(@NonNull final String cmdJson)`

      使用設定檔中的 `indexValueKey` 進行 `ledgerInput`

     ```java
     public LedgerInputResponse ledgerInput(@NonNull final String cmdJson) {
        return ledgerInputService.ledgerInput(config.getIndexValueKey(), cmdJson);
     }
     ```

### 資料驗證

- 在驗證回條前，BNS Client 會呼叫 `getNeedVerifyReceiptLocatorMap` 方法尋找需要被驗證的回條並向 BNS Server 拿取這些回條的 `merkleProof` 並呼叫 `obtainMerkleProof` callback 方法回傳 `merkleProof` 事件內的資訊。
- Bns Client 呼叫 `verify` 方法開始驗證回條。驗證回條後，BNS Client 會呼叫 `getVerifyReceiptResult` callback 方法回傳 `verifyReceiptResult` 內的資訊。

----

[首頁](../../README_ZH.md)
