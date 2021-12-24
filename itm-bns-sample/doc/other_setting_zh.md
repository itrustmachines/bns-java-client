## BNS Client 其他設定說明

### 關於 BNS Client 其他設定說明文件

- 您已經完成 BNS Client 中最主要的三個實作教學，[CMD](./cmd_zh.md)，[Callback](./callback_zh.md)，[ReceiptDao](./receiptDao_zh.md)。您基本上已經可以完整地整合我們的 SDK。在此份文件，我們將引導您了解 BNS Client 剩餘的設定，讓 BNS Client 能夠符合您的需求運作。

### 設定

主程式中有 5 個設定可進行調整

<!-- no toc -->
- [Main Sample Code](#main-sample-code-settings)
  - [Sample Properties](#sample_properties)
  - [JDBC_URL](#jdbc_url)
  - [LEDGER_INPUT_DELAY_SECOND](#ledger_input_delay_second)
  - [PROP_PATH_LIST](#prop_path_list)

設定檔中有 3 個設定可進行調整

- [Configuration file](#configuration-file-settings)
  - [VerifyBatchSize](#verifybatchsize)
  - [VerifyDelaySec](#verifydelaysec)
  - [RetryDelaySec](#retrydelaysec)

### Main Sample Code Settings

#### Sample_Properties

`SAMPLE_PROPERTIES` 是設定檔 [sample.properties](../src/main/resources/sample.properties) 的檔名，若開發者更改設定檔的檔名，請務必記得至[BnsClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)更新 `SAMPLE_PROPERTIES` 的檔名

- `SAMPLE_PROPERTIES` 設定位置，請參考 [BnsClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)

  ```java
  public static String SAMPLE_PROPERTIES = "sample.properties";
  ```

#### JDBC_URL

在範例程式中，我們使用資料庫作為回條儲存位置，您可以在此設定更改資料庫名稱，預設名稱為 `BnsDevice.db`

- `JDBC_URL` 設定位置，請參考 [BnsClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)

  ```java
  public static String JDBC_URL = "jdbc:sqlite:BnsDevice.db";
  ```

#### LEDGER_INPUT_DELAY_SECOND

每次 ledgerInput 後，BNS Client 會有短暫延遲，預設為 3 毫秒。您可以在此欄位更改延遲秒數

- `LEDGER_INPUT_DELAY_SECOND` 設定，請參考 [BnsClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)
  
  ```java
  public static int LEDGER_INPUT_DELAY_SECOND = 3;
  ```

#### PROP_PATH_LIST

BNS Client 會透過 `PROP_PATH_LIST` 中的路徑尋找設定檔。 如果您更改設定檔的儲存位址，請務必記得更改 `PROP_PATH_LIST` 中的內容

- `PROP_PATH_LIST` 設定，請參考 [BnsClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)
  
  ```java
  public static final String[] PROP_PATH_LIST = new String[] { "./", "./src/main/resources/",
    "./itm-bns-java-client/itm-bns-sample/src/main/resources/", "./itm-bns-sample/src/main/resources/" };
  ```

### Configuration File Settings

下方設定，請至 [sample.properties](../src/main/resources/sample.properties) 中修改

#### verifyBatchSize

`verifyBatchSize` 設定 BNS Client 一次驗證幾筆回條

#### verifyDelaySec

`verifyDelaySec` 設定每次 BNS Client 驗證玩一個 batch 的回條的延遲時間

#### RetryDelaySec

若 BNS Client 與 BNS Server 溝通失敗，會進行重新嘗試溝通，可在 `RetryDelaySec` 設定每次重試延遲秒數

----
您現在已經了解如何調整 BNS Client 其他設定，接下來們將引導您了解 BNS 資料夾自動存證完整的運作流程

- [下一頁 : BNS 資料夾自動存證](./bns-auto-folder-attest_zh.md)
- [上一頁 : ReceiptDao 教學](./receiptDao_zh.md)
