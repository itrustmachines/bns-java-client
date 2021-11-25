## CMD 設計說明

### 關於 CMD 設計說明文件

- CMD 為 SPO Client 傳送至 SPO Server 存證的資料。開發者必須要將上鏈存證的資訊放在 CMD 中，才能使用我們 SPO 服務。在[快速開始](./quick_start_zh.md)文件中，您已經了解如何從 ITM 公版 Dashboard 查看 CMD 內容。現在我們將透過這份文件引導您設計 CMD 的內容。

- CMD 可依照下列兩種情境進行設計
  - [在 ITM 公版 Dashboard 顯示](#在-itm-公版-dashboard-顯示) : 引導您設計可在 ITM 公版 Dashboard 上呈現的 CMD 內容，**實作時必須包含 `deviceId` 與 `timestamp` 兩欄位**，才能在 ITM Dashboard 上顯示
  - [使用 Verification Server 進行檔案驗證](#使用-verification-server-進行檔案驗證) : 引導您將檔案轉換成 CMD 並使用我們提供的公開驗證服務 [Verification Server](https://verification.itrustmachines.com/) 中的原始檔案驗證功能

- CMD 設計規範
  - 建議使用 JSON 資料型別且**最多為 1000 字元**
  
### 在 ITM 公版 Dashboard 顯示

- 假設今天我們想將太陽能發電版的資訊 (裝置編號、時間戳記、電壓、電流、瓦數) 傳送至 SPO Server 存證上鏈，我們必須把這些太陽能電板的資訊放在 CMD 的欄位，如下方範例程式所示。並透過 `Gson` 將資訊依照 JSON 格式儲存在 `cmdJSON` 中。

- 此段程式在 [SpoClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java) 中，開發者可嘗試自行設計 CMD 內容，然後依照快速入門文件中的步驟執行程式，並確認 CMD 內容可以在 [ITM 公版 Dashboard](https://azure-prod-rinkeby.itm.monster:8443) 中顯示，如下圖所示。

  ![cmd_example](../image/cmd_example_easy.png)

- [Cmd.java](../src/main/java/com/itrustmachines/sample/Cmd.java)

  ```java
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public class Cmd {
    String deviceId;
    Long timestamp;
    Double voltage;
    Double current;
    Double watt;
  }
  ```

- [SpoClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)

  ```java
  final String deviceId = spoClient.getConfig().getIndexValueKey();
  final long timestamp = System.currentTimeMillis();
  final double watt = 15.00 + (Math.random() * 100 % 100 / 100);
  final double voltage = 1000.00;
  final double current = 9999.99;

  final Cmd cmd = Cmd.builder()
                     .deviceId(deviceId)
                     .timestamp(timestamp)
                     .watt(watt)
                     .voltage(voltage)
                     .current(current)
                     .build();
  log.info("cmd create={}", cmd);
  final String cmdJson = new Gson().toJson(cmd);
  ```

### 使用 Verification Server 進行檔案驗證

- 假設我們今天想將 ITM 公司的 ICON 傳送到 SPO Server 存證上鏈，除了 `deviceId` 與 `timestamp` 兩欄位，我們需要增加 `fileName` 及 `fileHash` 兩欄位，總共至少四個欄位。`fileName` 為圖片檔案名稱 ，`fileHash` 欄位為圖片檔案內容的雜湊值。檔案內容的雜湊值可使用我們提供的 `SHA256` 演算法，請見下方範例程式。**除了上述必須包含的四個欄位外，開發者可依照需求增加 CMD 內容，最多為 1000 字元。**

  - **關於 SHA256 演算法，請參考 [HashUtils.java](../../spo-common-domain-objects/src/main/java/com/itrustmachines/common/util/HashUtils.java)**
  - **使用原始檔案驗證功能，需使用 `binaryLedgerInput`，並實作 `obtainBinaryLedgerInput`，下一章節將會詳細說明**

- [Cmd.java](../src/main/java/com/itrustmachines/sample/Cmd.java)

  ```java
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public class Cmd {
    String deviceId;
    Long timestamp;
    String binaryFileName;
    String binaryFileHash;
  }
  ```

- [SpoClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)

  ```java


  final String deviceId = spoClient.getConfig().getIndexValueKey();
  final long timestamp = System.currentTimeMillis();
  final String imgFileName = "_MG_2293.jpg";
  final String imgFilePath = "./src/main/java/com/itrustmachines/sample/" + imgFileName;
     
  File imgFile = new File(imgFilePath);
      
  final String imgFileHash = HashUtils.sha256(imgFile);
      

  final Cmd cmd = Cmd.builder()
                     .deviceId(deviceId)
                     .timestamp(timestamp)
                     .binaryFileName(imgFileName)
                     .binaryFileHash(imgFileHash)
                     .build();
  log.info("cmd create={}", cmd);
  final String cmdJson = new Gson().toJson(cmd);
  final BinaryLedgerInputResponse binaryLedgerInputResponse = spoClient.binaryLedgerInput(cmdJson, imgFile.toPath());
  log.info("ledger input result={}", binaryLedgerInputResponse);
  ```

1. 依照快速入門文件中的步驟執行程式
2. 前往 [ITM Dashboard Website](https://azure-prod-rinkeby.itm.monster:8443)
3. 點選左邊側欄的 photo icon
4. 點選你上傳的照片，可查看 CMD 和 駐泰
5. 如果照片狀態為綠色已上練，下載 off-chain proof，若灰色則為為上鍊
6. 前往 verification server
7. 點選原始檔案驗證功能
8. 上傳照片和照片的 off-chain proof，即可確認檔案是否有被篡改

您現在已經了解如何設計 CMD 內容，下一部分，我們將會引導您將 SPO Client 程式執行中發生的事件串接至 ITM 公版 Dashboard 顯示或是您的系統中

----

- [下一頁 : Callback 服務串接說明](./callback_zh.md)
- [上一頁 : 快速開始](./quick_start_zh.md)
