## 主程式說明

### 關於主程式說明文件

若您想對我們 SDK 的運作流程有更清楚的了解或是想更改 SDK 其他程式，我們建議您可以閱讀此份文件。在這份文件中，我們將依照 SPO Client 主程式的運作流程整合前面的教學內容及設定。閱讀此文件前，我們希望您已經了解所有 SDK 的設定、Callback、ReceiptDao 的實作方式。

### Configuration File

從 `PROP_PATH_LIST` 尋找設定檔並載入

- [SpoClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)
  
  ```java
  final String configPath = FileUtil.findFile(SAMPLE_PROPERTIES, PROP_PATH_LIST);
  final SpoClientConfig config = SpoClientConfig.load(configPath);
  log.info("SpoClientConfig={}", config);
  ```

### Callback 服務

- SPO Client 透過 `DASHBOARD_URL` 建立 Callback 服務，Callback 實作說明請參考 [Callback 串接實作說明](./callback_zh.md)

- [SpoClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)

  ```java
  final SpoClientCallback callback = new CallbackSample(DASHBOARD_URL);
  ```

### ReceiptDao 服務

- SPO Client 透過 `JDBC_URL` 建立 receiptDao 服務，receiptDao 詳細說明請參考 [ReceiptDao 功能說明](./receiptDao_zh.md)

- [SpoClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)

  ```java
  final SpoClientReceiptDao receiptDao = new ReceiptDaoSample(JDBC_URL);
  ```

### 初始化 SPO Client

![](../image/spo_client_init.png)

- 為了與 SPO Server 建立上鏈存證與驗證服務，SPO Client 會呼叫此方法進行 sample.properties, receiptDao, callback 的初始化，並向 SPO Server 註冊

- [SpoClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)

  ```java
  final SpoClient spoClient = SpoClient.init(config, callback, receiptDao);
  ```

- 關於初始化的程式，請參考 [SpoClient.java](../../spo-client/src/main/java/com/itrustmachines/client/BnsClient.java)

  ```java
  public static SpoClient init(@NonNull final SpoClientConfig config, @NonNull final SpoClientCallback callback,
      @NonNull final SpoClientReceiptDao receiptDao) {
    if (config.getVerifyBatchSize() <= 0) {
      config.setVerifyBatchSize(SpoClientConfig.DEFAULT_VERIFY_BATCH_SIZE);
    }
    SpoClient spoClient = new SpoClient(config, callback, receiptDao);
    final boolean isRegistered = spoClient.registerService.register();
    if (!isRegistered) {
      String errMsg = "SpoClient register fail";
      log.error("init() error, {}", errMsg);
      throw new RuntimeException(errMsg);
    }
    return spoClient;
  }
  ```

### 資料存證

![](../image/spo_client_ldeger_input_and_verify.png)

- SPO Client 初始化且成功註冊後，將 CMD 轉換成 JSON 資料型別並呼叫 `ledgerInput` 將資料送至 SPO Server 清算上鍊。CMD 詳細資訊請參考 [CMD實作說明](./cmd_zh.md)

- 若您想使用我們的原始檔案驗證系統，則需要使用 `binaryLedgerInput`

- `ledgerInput` / `binaryLedgerInput` 會將 `cmdJSON` 以及其他資訊儲存至 `ledgerInputRequest` 並呼叫 `createLedgerInputByCmd` callback 方法將 `ledgerInputRequest` 事件內的資訊傳送至 ITM Dashboard 或是您整合的系統

- SPO Client 成功建立 `ledgerInputRequest` 後，會將 `ledgerInputRequest` 送至 SPO Server 清算上鍊並收到 SPO Server 回傳的 `ledgerInputResponse` / `binaryLedgerInputResponse`。若您有實作 `obtainLedgerInputResponse` / `obtainBinaryLedgerInputResponse` callback 方法，則 SPO Client 會將 `obtainLedgerInputResponse` / `obtainBinaryLedgerInputResponse` 事件內的資訊傳送至 ITM Dashboard 或是您整合的系統

- SPO Client 會呼叫 `handleReceipt` 將回條從 `ledgerInputResponse` / `binaryLedgerInputResponse` 取出並儲存至資料庫

- SPO Client 會呼叫 `handleDoneClearanceOrderList` 透過 `doneClearanceOrder` 找出並驗證資料庫中回條的 `clearanceOrder` 小於當前的 `doneClearanceOrder`。若開發者有實作 `obtainReceiptEvent` 和  `obtainDoneClearanceOrderEvent`，SPO Client 會將 `receipt` 和 `doneClearanceOrder` 事件內的資訊傳送至 ITM Dashboard 或是您整合的系統。`receipt` 和 `doneClearanceOrder` 位於 `ledgerInputResponse` / `binaryLedgerInputResponse` 內。
  
- 關於 `ledgerInput` 和 `binaryLedgerInput` 的程式，請參考 [SpoClient.java](../../spo-client/src/main/java/com/itrustmachines/client/BnsClient.java)

  1. `ledgerInput(@NonNull final String indexValueKey, @NonNull final String cmdJson)`

     若您有多個裝置或 `indexValueKey` 使用同一份 SPO Client，我們建議您使用此方法並自行設定 `indexValueKey`

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

  3. `binaryLedgerInput(@NonNull final String indexValueKey, @NonNull final String cmdJson, @NonNull final Path binaryPath)`

     若您有多個裝置或 `indexValueKey` 使用同一份 SPO Client，我們建議您使用此方法並自行設定 `indexValueKey`

     ```java
     public BinaryLedgerInputResponse binaryLedgerInput(@NonNull final String indexValueKey, @NonNull final String cmdJson, @NonNull final Path binaryPath) {
        return binaryLedgerInputService.binaryLedgerInput(indexValueKey, cmdJson, binaryPath);
     }
     ```
  
  4. `binaryLedgerInput(@NonNull final String cmdJson, @NonNull final Path binaryPath)`

     使用設定檔中的 `indexValueKey` 進行 `binaryLedgerInput`

      ```java
      public BinaryLedgerInputResponse binaryLedgerInput(@NonNull final String cmdJson, @NonNull final Path binaryPath) {
        return binaryLedgerInputService.binaryLedgerInput(config.getIndexValueKey(), cmdJson, binaryPath);
      }
      ```

### 資料驗證

- 在驗證回條前，SPO Client 會呼叫 `getNeedVerifyReceiptLocatorMap` 方法尋找需要被驗證的回條並向 SPO Server 索取這些回條的 `merkleProof`

- SPO Client 會呼叫 `obtainMerkleProof` 方法並呼叫 `verify` 方法開始驗證回條。若開發者有實作 `obtainMerkleProof` 方法，SPO Client 會將 `merkleProof` 事件內的資訊傳送至 ITM Dashboard 或是您整合的系統。

- 驗證回條後，SPO Client 會呼叫 `getVerifyReceiptResult` 方法將 `verifyReceiptResult` 事件內的資訊傳送至 ITM Dashboard 或是您整合的系統。

----

[首頁](../README_ZH.md)
