## CMD 設計說明

### 關於 CMD 設計說明文件

- CMD 為 BNS Client 傳送至 BNS Server 存證的資料。開發者必須要將上鏈存證的資訊放在 CMD 中，才能使用我們 BNS 服務。在[快速開始](./quick_start_zh.md)文件中，您已經了解如何執行範例程式。現在我們將透過這份文件引導您設計 CMD 的內容。

- CMD 可依照下列兩種情境進行設計
  - [文字存證](#文字存證) : 引導您設計文字存證的 CMD 內容
  - [檔案存證](#檔案存證) : 引導您將檔案轉換成 CMD 並執行程式進行檔案存證

- CMD 設計規範
  - 建議使用 JSON 資料型別
  
### 文字存證

- 假設今天我們想將太陽能發電版的資訊傳送至 BNS Server 存證上鏈，我們必須把這些太陽能電板的資訊放在 CMD 的欄位，如下方範例程式所示。並透過 `Gson` 將資訊依照 JSON 格式儲存在 `cmdJSON` 中。

- 此段程式在 [BnsClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java) 中，開發者可嘗試自行設計 CMD 內容，然後依照快速入門文件中的步驟執行程並前往 [BNS Website](https://bns.itrustmachines.com/) 查看您 ledgerinput 的結果。

- [Cmd.java](../src/main/java/com/itrustmachines/sample/Cmd.java)

  ```java
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public class Cmd {
    String deviceId;
    Long timestamp;
    Double watt;
  }
  ```

- [BnsClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)

  ```java
  final String deviceId = keyInfo.getAddress();
  final long timestamp = System.currentTimeMillis();
  final double watt = 15.00 + (Math.random() * 100 % 100 / 100);

  final double watt = 15.00 + (Math.random() * 100 % 100 / 100);
  final Cmd cmd = Cmd.builder()
                .deviceId(deviceId)
                .timestamp(timestamp)
                .watt(watt)
                .build();
  log.info("cmd create={}", cmd);
  cmdJson = new Gson().toJson(cmd);
  ```

### 檔案存證

- 假設我們今天想將圖片上傳至 BNS Server 存證上鏈，須先將檔案內容取雜湊值。檔案內容的雜湊值可使用我們提供的 `SHA256` 演算法，請見下方範例程式。

- [CmdBinary.java](../src/main/java/com/itrustmachines/sample/CmdBinary.java)

  ```java
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public class CmdBinary {

    String deviceId;
    Long timestamp;
    String fileName;
    String fileHash;
  }
  ```

- [BnsClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)

  ```java
  final String deviceId = keyInfo.getAddress();
  final File imgFile = new File(filePath);
  final String fileName = imgFile.getName();
  final String binaryFileHash = HashUtils.sha256(imgFile);
  final CmdBinary cmdBinary = CmdBinary.builder()
            .deviceId(deviceId)
            .timestamp(timestamp)
            .fileName(fileName)
            .fileHash(binaryFileHash)
            .build();
  log.info("cmdBinary create={}", cmdBinary);
  cmdJson = new Gson().toJson(cmdBinary);
  ```

1. 依照[快速入門文件](./quick_start_zh.md)中的步驟並使用 `java -jar ./target/itm-bns-sample-1.1.1-SNAPSHOT.jar --file {filepath}` 執行程式
2. 前往 [BNS Website](https://bns.itrustmachines.com/) 查看您 ledgerinput 的結果

您現在已經了解如何設計 CMD 內容，下一部分，我們將會引導您將 BNS Client 程式執行中發生的事件串接您的系統中

----

- [下一頁 : Callback 服務串接說明](./callback_zh.md)
- [上一頁 : 快速開始](./quick_start_zh.md)
