# ITM SPO CLIENT JAVA
## 程式方法說明
主要程式介面：
[SpoClient.class](./src/main/java/com/itrustmachines/client/BnsClient.java)

### 1. 初始化SpoClient

```
+ init(config: SpoClientConfig , callback: SpoClientCallback, receiptDao: SpoClientReceiptDao): SpoClient
```

說明:

使用SpoClient前必須要先初始化SpoClient instance，傳入值為 `SpoClientConfig` , `SpoClientCallback` , 以及 `SpoClientReceiptDao`。

- [SpoClientConfig](./src/main/java/com/itrustmachines/client/config/BnsClientConfig.java)當中會包含初始化SpoClient必須要的設定值，如: privateKey, indexValueKey, spoServerUrl, nodeUrl...等。可用該類別中提供的 `load` 方法讀取設定檔建立該物件。
- [SpoClientCallback](./src/main/java/com/itrustmachines/client/todo/SpoClientCallback.java)介面提供使用者自行實作Spo Client callback行為。相關說明請參考 [[Callback文件]](./callback.md)。
- [ReceiptDao](./src/main/java/com/itrustmachines/client/todo/BnsClientReceiptDao.java)介面提供使用者自行實作Spo Client儲存回條的方式。相關說明請參考 [[ReceiptDao文件]](./receiptDao.md)。

### 2. LedgerInput(資料存證)


#### 2-1. 

```
+ ledgerInput(cmdJson: String): LedgerInputResponse
```

說明: 
- `cmdJson` 為包含RawData的Json字串
- 呼叫此方法將會把 `cmdJson` 內容傳送給Spo Server要求資料存證
- 目前Spo Server限制 `cmdJson` 的字串長度為1000字元
- LedgerInputResponse會有要求存證的結果

#### 2-2.

```
+ ledgerInput(indexValueKey: String, cmdJson: String): LedgerInputResponse
```

說明:
- 此方法主要使用用途與 2-1. 相同。
- 差別在於: 使用者呼叫此方法能夠改變存證的 `indexValueKey`，而非使用 `SpoClientConfig` 預設值。
- `indexValueKey` 為辨識存證裝置與存證資料的索引值。
- 建議不同裝置間使用不同的 `indexValueKey` 值，而通常每個裝置會有不同的Config設定啟動不同的SpoClient物件，因此通常開發者僅需使用 2-1. 提供的存證方法。
- 建議使用時機: 當有不同的裝置或使用者使用同一個SpoClient物件時。

### 3. BinaryLedgerInput (Binary檔案存證)

#### 3-1.

``` 
+ binaryLedgerInput(cmdJson: String, binaryPath: Path): BinaryLedgerInputResponse
```

說明:
- 呼叫此方法傳送Binary檔案至Spo Server進行存證
- `cmdJson` 為存證資料Json字串，當中需包含 `binaryFileHash` 欄位, 此欄位為 `binaryPath` 檔案雜湊Hash值。
- `binaryFileHash` 可透過 [HashUtils](../spo-common-domain-objects/src/main/java/com/itrustmachines/common/util/HashUtils.java) 中所提供的 `sha256` 方法取得
- 注意: 使用 `binaryLedgerInput` 將會在Spo Server中留存檔案以便在上鏈後做檔案正確性比對，若不希望在Spo Server中留存檔案，請使用 `2. LedgerInput` 方法，將取完雜湊的 `binaryFileHash` 值帶入 `cmdJson` 即可。


#### 3-2.

``` 
+ binaryLedgerInput(indexValueKey: String, cmdJson: String, binaryPath: Path): BinaryLedgerInputResponse
```

說明:
- 此方法目的同 3-1.。
- 增加indexValueKey傳入值的目的如 2-2. 所說明。

### VerifyNow (立即驗證)

```
+ verifyNow(): void
```

說明:
- 呼叫此方法後，SpoClient將會確認Spo Server當前已上鏈序號(doneClearanceOrder)，若所儲存的回條當中有小於此序號值的回條，SpoClient將會開始進行驗證。
- 若不呼叫此方法，SpoClient也會在每一次的資料存證(ledgerInput或binaryLedgerInput)
  後確認Spo Server的已上鏈序號，並且根據此結果確認是否需要執行驗證。
- 建議使用時機: 如果裝置不會一直打資料給Spo Server，則使用者需要定期主動呼叫此方法，裝置才會執行驗證。

### Close (關閉服務)

```
+ close(): void
```

說明:
- 呼叫此方法後，可以將SpoClient服務關閉
- 建議使用時機: 當已建立的SpoClient物件無須再使用時，可以呼叫此方法關閉SpoClient。

----

## 建議整合SpoClient實作流程
1. 設定SpoClientConfig 
   
    建立設定檔並用load方法讀取

2. 實作Callback及ReceiptDao

3. initial SpoClient
   
    呼叫init方法建立SpoClient物件

4. ledgerInput
   
    執行ledgerInput存證資料

5. verifyNow
   
    若沒有loop傳送存證資料，則要定期呼叫verifyNow裝置才會驗證。

6. close
   
    呼叫close方法關閉SpoClient物件

----

##  整合SpoClientIXAuth

 以 `SpoClientIXAuthConfig` 建立 `SpoClientIXAuth` 使用，使用方式同 `SpoClient`

`payload` 為欲使用 IX Trio 簽章之資料

主要程式介面：
[SpoClientIXAuth.class](./src/main/java/com/itrustmachines/client/ix/SpoClientIXAuth.java)

----

## 範例程式碼
Java Client整合範例請參考: [itm-spo-sdk-java-sample](https://github.com/itrustmachines/itm-spo-sdk-java)
(註: 欲參考範例，請先與ITM開發人員要求權限)
