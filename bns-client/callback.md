# SpoClientCallback介面方法說明

```
/** 1. */
void register(RegisterRequest registerRequest, String registerResult);
```
Spo Client啟動時，會先與Spo Server註冊，開發者能夠實作此方法，將註冊結果(registerResult) callback。

----

```
/** 2. */
void createLedgerInputByCmd(ReceiptLocator receiptLocator, LedgerInputRequest ledgerInputRequest);
```
LedgerInput為Client端傳送資料至Spo Server的行為，在裝置端要求存證前，會先產生`LedgerInputRequest`，開發者實作此方法，將與資料存證要求的內容callback。
- receiptLocator: 包含資料回條於TPMTree中的定位值。
- ledgerInputRequest: 存證要求的相關內容，當中的Cmd欄位會包含裝置端存證的Raw Data，同時也包含其他存證資料，如:時間戳記..等。

----

```
/** 3.1 */
void obtainLedgerInputResponse(ReceiptLocator locator, String cmdJson, LedgerInputResponse ledgerInputResponse);
```
取得LedgerInputResponse，當裝置端對Spo Server要求存證(LedgerInput)後，開發者能夠實作此方法將存證結果callback。
    
- receiptLocator: 包含資料回條於TPMTree中的定位值。
- cmdJson: Cmd Json字串。Cmd類別當中包含裝置端要求存證的Raw Data，同時也包含其他存證資料，如:時間戳記..等。
- ledgerInputResponse: Spo Server接收裝置端的存證請求的結果，其中包含本次回覆裝置端的回條(receipt)、以及目前Spo Server已清算的序號(doneClearanceOrderList)等。
 
```
/** 3.2 */
void obtainBinaryLedgerInputResponse(ReceiptLocator locator, String cmdJson,
    BinaryLedgerInputResponse ledgerInputResponse);
```
取得BinaryLedgerInputResponse，當裝置端對Spo Server要求Binary檔案存證(BinaryLedgerInput)後，開發者能夠實作此方法將存證結果callback。
- 參數項目與3.1之LedgerInput相似，唯Response中會多包含binaryFileMetadata，且cmdJson中必包含檔案的FileHash，Spo Server存證的是檔案的Hash值。

----

```
/** 4. Receipt from LedgerInputResult | Obtain from SPO Server API */
void obtainReceiptEvent(ReceiptEvent receiptEvent);
````
當裝置與Spo Server要求資料存證後，會收到Spo Server所發給的回條(Receipt)，開發者能夠實作此方法，將取得的回條callback。

----

```
/** 5 DoneCO from LedgerInputResult | DoneCO from polling */
void obtainDoneClearanceOrderEvent(DoneClearanceOrderEvent doneClearanceOrderEvent);
```
裝置向Spo Server取得已清算上鏈序號(doneClearanceOrder)，裝置將判斷是否能夠驗證儲存的回條。開發者能夠實作此方法，將裝置取得已上鏈序號(doneClearanceOrder) callback。

裝置會由兩個動作執行 `obtainDoneClearanceOrderEvent` :
1. 每次LedgerInput完，Spo Server的回應都會帶有doneClearanceOrder數值。
2. 開發者呼叫SpoClient `verifyNow()` 方法能夠主動和Spo Server要求doneClearanceOrder。[[參考程式碼]](/src/main/java/com/itrustmachines/client/BnsClient.java)

---- 

```
/** 6.1 */
void obtainMerkleProof(ReceiptLocator receiptLocator, MerkleProof merkleProof);
```

裝置在驗證回條時，會先與Spo Server取得MerkleProof證據，開發者能夠實作此方法將資料索引值(receiptLocator)與MerkleProof callback。

```
/** 6.2 */
void getVerifyReceiptResult(Receipt receipt, MerkleProof merkleProof,
    VerifyReceiptAndMerkleProofResult verifyReceiptAndMerkleProofResult);
```

裝置驗證回條之結果，開發者能夠實作此方法將驗證結果callback。 `VerifyReceiptAndMerkleProofResult` 中會有該回條驗證結果之細節，主要屬性說明如下:

1. Long clearanceOrder: 上鏈清算序號
2. String indexValue: 清算索引值
3. boolean pass: 驗證是否成功
4. String status: 驗證狀態
5. Long timestamp: 驗證時間
6. String description: 驗證失敗的詳細說明

其他驗證技術細節使用的屬性結果不贅述。