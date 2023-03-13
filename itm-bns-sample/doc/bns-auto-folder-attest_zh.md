## BNS 資料夾檔案自動存證

### 關於 BNS 資料夾檔案自動存證

BNS 資料夾檔案自動存證程式會自動掃描資料夾內的檔案並透過 ledgerInput 至 BNS server 存證到區塊鏈上。此份教學文件將透過下列步驟帶您如何設定並執行 BNS 資料夾檔案自動存證程式

<!-- no toc -->
1. [修改 BNS 資料夾檔案自動存證設定檔](#1.-修改-BNS-資料夾檔案自動存證設定檔)
2. [執行程式](#2.-執行程式)
3. [Callback 教學](#3.-Callback-教學)

完成上述步驟後，您就可以參考教學文件中的範例整合及開發我們的 SDK。


### 1. 修改 BNS 資料夾檔案自動存證設定檔

- 範例主程式 [BnsAutoFolderAttestApplication.java](../src/main/java/com/itrustmachines/bnsautofolderattest/BnsAutoFolderAttestApplication.java) 中會使用此設定檔進行私鑰、區塊鏈位址、電子郵件 ... 等設定，**本設定檔內容十分重要，請依照下方說明實作，再執行範例程式**

```Java
# 欲存證之資料夾的絕對路徑
rootFolderPath=

# 欲存放之驗證證據檔案的絕對路徑
verificationProofDownloadPath=

# 每次掃瞄檔案的間隔時間
scanDelay=300

# bns-auto-folder-attest 會將每次存證紀錄的資訊紀錄至 CSV 檔. 你可以在此設定 CSV 檔的檔名和路徑 
historyCsvPath=history.csv

# bns-auto-folder-attest 會將每次掃描檔案紀錄的資訊紀錄至 CSV 檔. 你可以在此設定 CSV 檔的檔名和路徑
scanHistoryCsvPath=scan.csv
        
# bns-auto-folder-attest 會將每次存證紀錄的資訊紀錄至資料庫. 你可以在此設定資料庫名稱和路徑
jdbcUrl=jdbc:sqlite:folder-auto-attest.db
        
## optional ##

# 取消下載驗證證據功能. 預設為 false
disableDownloadVerificationProof=false

# 取消快取功能. 預設為 false
disableCache=false
        

#### BNS Client ####
# 請參考 BNS Java Client 快速開始文件
bnsServerUrl=
nodeUrl=
email=
verifyBatchSize=10
verifyDelaySec=1
retryDelaySec=5
```


### 2. 執行程式

前往 bns-java-client 資料夾，並更改 mvnw 檔案權限

```shell
> cd bns-java-client
> chmod u+x mvnw
```

執行程式並輸入私鑰以及 PIN 碼，BNS Auto Folder Attest 會透過 PIN 碼加密您的金鑰並儲存至 `.itm.encrypted.private.key`。之後每次執行程式需使用 PIN 碼來解鎖您的私鑰
私鑰 : 從您的 MetaMask 帳戶中輸出
PIN : 任意 8 個以上的數字或英文字母
```shell
> ./mvnw.cmd clean package -DskipTests -s settings.xml
> cd bns-auto-folder-attest
> java -jar ./target/itm-bns-sample-1.1.1-SNAPSHOT.jar
```

### 3. Callback 教學

在 BNS Java Client 文件中，我們已經定義 7 個 callback 方法。在 BNS Auto Folder Attest 程式中，我們額外新增 7 個 callback 提供開發者整合。接下來，我們將介紹這 6 個 callback。
在 callback 文件中，我們將所有 callback 的資訊輸出至 CSV 檔。在閱讀完 callback 文件中，您可以在 callback 方法中實作程式，將 callback 輸出整合至您的系統中。

在 BNS Auto Folder Attest 程式中，BNS Auto Folder Attest 會掃描資料夾內每個檔案，並透過 `onScanResult` callback `scanResult` 掃描結果

- [ScanResult.java](../src/main/java/com/itrustmachines/bnsautofolderattest/vo/ScanResult.java)
    ```java
    public class ScanResult {
      public ZonedDateTime startTime;
      public long totalCount;
      public long totalBytes;
      public long addedCount;
      public long modifiedCount;
      public long attestedCount;
      public ZonedDateTime endTime;
    }
    ```
1. `onScanResult` : 當 BNS Auto Folder Attest 掃描檔案後，會呼叫 `onScanResult` Callback 將 `scanResult` 輸出至 `scan.csv`

- [CallbackImpl.java](../src/main/java/com/itrustmachines/bnsautofolderattest/service/CallbackImpl.java)

  ```java
  public void onScanResult(@NonNull final ScanResult scanResult) {
    log.info("onScanResult() scanResult={}", scanResult);
    writeScanHistory(scanResult);
  }
  ```

BNS Auto Folder Attest 將檔案透過 ledgerInput 傳送至 BNS server 存證至區塊鏈上後會回傳 `ledgerInputResponse`。
BNS Auto Folder Attest 會透過 `ledgerInputResponse` 內的資訊建立 `attestationRecord`

- [attestationRecord.java](../src/main/java/com/itrustmachines/bnsautofolderattest/vo/AttestationRecord.java)

    ```java
    public class AttestationRecord { 
        @DatabaseField(generatedId = true, columnName = ID_KEY)
        private Long id;

        @DatabaseField(columnName = "type", canBeNull = false, unknownEnumName = "UNKNOWN")
        private AttestationType type;

        @DatabaseField(columnName = STATUS_KEY, canBeNull = false, unknownEnumName = "UNKNOWN")
        private AttestationStatus status;

        private Path filePath;

        private Path relativeFilePath;

        private ZonedDateTime lastModifiedTime;

        @DatabaseField(columnName = "fileHash", canBeNull = false)
        private String fileHash;

        @ToString.Exclude
        @DatabaseField(columnName = "previousRecord", foreign = true)
        private AttestationRecord previousRecord;

        @DatabaseField(columnName = "address", canBeNull = false)
        private String address;

        private ZonedDateTime attestTime;

        @DatabaseField(columnName = CLEARANCE_ORDER_KEY, canBeNull = false)
        private Long clearanceOrder;

        @DatabaseField(columnName = INDEX_VALUE_KEY, canBeNull = false)
        private String indexValue;

        private Path proofPath;
    }
    ```


2. `onAttested` : 當檔案成功存證時，此 callback 允許 BNS Auto Folder Attest 程式回傳 `attestationRecord` 

- [CallbackImpl.java](../src/main/java/com/itrustmachines/bnsautofolderattest/service/CallbackImpl.java)

  ```java
  public void onAttested(@NonNull final AttestationRecord attestationRecord) {
    log.info("onAttested() attestationRecord={}", attestationRecord);
    writeHistory(attestationRecord);
  }
  ```

3. `onAttestFail` : 當檔案存證失敗時，此 callback 允許 BNS Auto Folder Attest 程式回傳 `attestationRecord`

    ```java
    public void onAttestFail(@NonNull final AttestationRecord attestationRecord) {
        log.error("onAttestFail() attestationRecord={}", attestationRecord);
        writeHistory(attestationRecord);
    }
    ```

4. `onVerified` : 此 callback 從 `getVerifyReceiptResult` 延伸。當證據驗證成功時，此 callback 允許 BNS Auto Folder Attest 程式回傳 `attestationRecord`

   ```java
   @SneakyThrows
   @Override
   public void onVerified(@NonNull final AttestationRecord attestationRecord) {
        log.error("onVerified() attestationRecord={}", attestationRecord);
        writeHistory(attestationRecord);
   }
   ```

5. `onVerifyFail` : 此 callback 從 `getVerifyReceiptResult` 延伸。當證據驗證失敗時，此 callback 允許 BNS Auto Folder Attest 程式回傳 `attestationRecord`

    ```java
    @Override
    public void onVerifyFail(@NonNull AttestationRecord attestationRecord) {
        log.error("onVerifyFail() attestationRecord={}", attestationRecord);
        writeHistory(attestationRecord);
    }
    ```
6. `onSaveProof` : 此 callback 從 `getVerifyReceiptResult` 延伸。BNS Auto Folder Attest 向 BNS server 成功拿取證據並儲存至資料庫後，此 callback 允許 BNS Auto Folder Attest 程式回傳 `attestationRecord`

    ```java
    @Override
    public void onSaveProof(@NonNull AttestationRecord attestationRecord) {
        log.error("onSaveProof() attestationRecord={}", attestationRecord);
        writeHistory(attestationRecord);
    }
    ```

7. `onSaveFail` : 此 callback 從 `getVerifyReceiptResult` 延伸。BNS Auto Folder Attest 向 BNS server 拿取證據失敗後，此 callback 允許 BNS Auto Folder Attest 程式回傳 `attestationRecord`

    ```java
    @Override
    public void onSaveFail(@NonNull AttestationRecord attestationRecord) {
        log.error("onSaveFail() attestationRecord={}", attestationRecord);
        writeHistory(attestationRecord);
    }
    ```

- [下一頁 : 首頁](../../README_ZH.md)
- [上一頁 : BNS Java Client 其他設定教學](./other_setting_zh.md)
