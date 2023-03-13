## Callback 串接實作說明

### 關於 Callback 實作說明文件

- Callback 的功能為將 BNS Client 與 BNS Server API 的 Request Body 和 Response 傳送至自己的系統中。在上一份文件中，你已經了解如何設計 CMD。在這份文件中，我們將引導您實作 Callback 將事件傳送至自己的系統。
- API 文件可至 [BNS API DOC website](https://bns.itrustmachines.com/api) 查看

- 我們總共定義 7 個事件可以 Callback，分別為 :

  1. `register` : BNS Client 初始化時，會向 BNS Server 進行註冊，開發者可透過實作此方法將 `registerRequest` 和 `registerResult` Callback
  
  2. `createLedgerInputByCmd` : BNS Client 初始化成功，會將 CMD 等資訊放入 `ledgerInputRequst` 並進行 **ledgerInput** 開發者可透過實作此方法將 `ledgerInputRequest` Callback。
  
  3. `obtainLedgerInputResponse` : BNS Client ledgerInput 後會收到 BNS Server 回傳的 `ledgerInputResult`，開發者可透過實作此方法將 `ledgerInputResult` Callback
  
  4. `obtainReceiptEvent` : 將 `ledgerInputResult` 中的 `receipt` Callback
  
  5. `obtainDoneClearanceOrderEvent` : 將 `ledgerInputResult` 中的 `doneClearanceOrder` Callback
  
  6. `obtainMerkleProof` : BNS Client 驗證回條前，會先向 BNS Server 拿取 MerkleProof 作為驗證依據，開發者可透過實作使方法將 `merkleProof` Callback
  
  7. `getVerifyReceiptResult` : BNS Client 取得 MerkleProof 後會開始驗證回條，並將驗證結果放入 `verifyReceiptResult`，開發者可實作此方法將 `verifyReceiptResult` Callback
  
- Callback 相關檔案

  - [CallbackSample.java](../src/main/java/com/itrustmachines/sample/CallbackSample.java)
  - [BnsClientCallback.java](../../bns-client/src/main/java/com/itrustmachines/client/todo/BnsClientCallback.java)
    
### register 說明

##### BNS Client 初始化時，會向 BNS Server 進行註冊，若您想將註冊資訊 Callback，可閱讀下方說明及程式

BNS Client 初始化時，會向 BNS Server 請求註冊 `registerRequest`。BNS Server 收到請求註冊後會將註冊結果 `registerResult` 回傳給 BNS Client。

- 開發者可在 [CallbackSample.java](../src/main/java/com/itrustmachines/sample/CallbackSample.java) 的 `register` 方法撰寫程式將 `registerRequest` 和 `registerResult` Callback 至自己的系統

- [RegisterRequest.java](../../bns-client/src/main/java/com/itrustmachines/client/register/vo/RegisterRequest.java)

  ```java
  public class RegisterRequest implements Serializable, Cloneable {
    private String address;         // MetaMask 錢包位址
    private String email;           // 註冊 BNS 服務的 email
    private String toSignMessage;   // address
    private SpoSignature sig;       // 透過 SPECK256K1 演算法取得 toSignMessage 的簽章
  }
  ```

- [CallbackSample.java](../src/main/java/com/itrustmachines/sample/CallbackSample.java)

  ```java
  public void register(RegisterRequest registerRequest, Bool registerResult) {
  }
  ```

### createLedgerInputByCmd 說明

##### BNS Client 初始化成功，會將 CMD 等資訊放入 ledgerInputRequest 並進行 ledgerInput，若您想將 ledgerInputReqeust 的資訊 Callback，可閱讀下方說明及程式

BNS Client 初始化成功後，會將 `CMD`, `timestamp`資訊建立為 ledgerInputRequest 並進行 ledgerInput

- 開發者可在 [CallbackSample.java](../src/main/java/com/itrustmachines/sample/CallbackSample.java) 的 `createLedgerInputByCmd` 撰寫程式將 `ledgerInputRequest` 的資訊 Callback

- **建議實作時可將 `ledgerInputRequest` 中的 `clearanceOrder` 和 `indexValue` 儲存至 `receiptLocator`** 方便日後搜尋資料

- [LedgerInputRequest.java](../../bns-client/src/main/java/com/itrustmachines/client/input/vo/LedgerInputRequest.java)
  ```java
  public class LedgerInputRequest implements Serializable, Cloneable {
    private String callerAddress;   // MetaMask 錢包位址
    private String timestamp;       // 建立 ledgerInputRequest 時的時間戳記
    private String cmd;             // 存證內容
    private String indexValue;      // 裝置序號 + 'R' + SN，裝置序號為您的 MetaMask 錢包位址
    private String metadata;        // 對於 CMD 的額外描述
    private Long clearanceOrder;    // 目前 BNS Client 的清算序號
    private SpoSignature sigClient; // BNS Client 對 ledgerInputRequest 的電子簽章
  }
  ```

- [CallbackSample.java](../src/main/java/com/itrustmachines/sample/CallbackSample.java)

  ```java
  public void createLedgerInputByCmd(ReceiptLocator receiptLocator, LedgerInputRequest ledgerInputRequest) {
  }
  ```

### obtainLedgerInputResponse 說明

##### BNS Client ledgerInput 後會收到 BNS Server 回傳的 ledgerInputResult，若您想將 ledgerInputResult 的資訊 Callback 至自己的系統，可閱讀下方說明及程式

- BNS Client 將 `ledgerInputRequest` 傳送至 BNS Server 進行存證後，BNS Server 會將存證結果 `ledgerInputResult` 回傳給 BNS Client

- 開發者可在 [CallbackSample.java](../src/main/java/com/itrustmachines/sample/CallbackSample.java) 的 `obtainLedgerInputResponse` 撰寫程式將 `ledgerInputResponse` 的資訊 Callback

- **建議實作時可將 `ledgerInputResponse` 中的 `clearanceOrder` 和 `indexValue` 儲存至 `receiptLocator`**，方便日後搜尋資料

- [LedgerInputResponse.java](../../bns-client/src/main/java/com/itrustmachines/client/input/vo/LedgerInputResponse.java)
  ```java
  public class LedgerInputResponse {
    private String status;                      // ledgerInput 的狀態
    private String description;                 // 對於此次 ledgerInput 的描述
    private Receipt receipt;                    // 回條
    private List<Long> doneClearanceOrderList;  // BNS Server 目前的已清算序號
  }
  ```

- [CallbackSample.java](../src/main/java/com/itrustmachines/sample/CallbackSample.java)
  
  ```java
  public void obtainLedgerInputResponse(ReceiptLocator locator, String cmdJson,
      LedgerInputResponse ledgerInputResponse) {
  }
  ```

### obtainReceiptEvent 說明

##### 若您想將 ledgerInputResponse 中的回條 Callback，可閱讀下方說明及程式

- 開發者可在 [CallbackSample.java](../src/main/java/com/itrustmachines/sample/CallbackSample.java) 的 `obtainReceiptEvent` 撰寫程式將 `ledgerInputResponse` 中的 `receipt` 的資訊 Callback

- **建議實作時建議將回條 `receipt` 中的 `indexValue` 和 `clearanceOrder` Callback 至自己系統，以方便搜尋回條資料**

- [Receipt.java](../../spo-common-domain-objects/src/main/java/com/itrustmachines/common/vo/Receipt.java)
  ```java
  public class Receipt implements Serializable, Cloneable {
    private String callerAddress;       // MetaMask 錢包位址
    private Long timestamp;             // ledgerInput 時的 Timestamp
    private String cmd;                 // 存證內容
    private String indexValue;          // 裝置序號 + 'R' + SN，裝置序號為您的 MetaMask 錢包位址
    private String metadata;            // 對於 CMD 的額外描述
    private Long clearanceOrder;        // 目前 BNS Client 的清算序號
    private SpoSignature sigClient;     // BNS Client 對 ledgerInputRequest 的電子簽章
    private Long timestampSPO;          // BNS Server 收到 ledgerInputRequest 時的時間戳記
    private String result;              // ledgerInputRequest 的結果
    private SpoSignature sigServer;     // BNS 對此回條的電子簽章
  }
  ```
- [CallbackSample.java](../src/main/java/com/itrustmachines/sample/CallbackSample.java)

  ```java
  public void obtainReceiptEvent(ReceiptEvent receiptEvent) {
  }
  ```

### obtainDoneClearanceOrderEvent 說明

##### 若您想將 ledgerInputResponse  中的 doneClearanceOrder Callback，可閱讀下方說明及程式

- 如果回條的 `clearanceOrder` 小於 BNS Server `doneClearanceOrder`，這些回條便稱為待驗證回條。BNS Client 會驗證這些待驗證的回條

- 開發者可在 [CallbackSample.java](../src/main/java/com/itrustmachines/sample/CallbackSample.java) 的 `obtainDoneClearanceOrderEvent` 撰寫程式將 `doneClearanceOrder` 的資訊 Callback

- [DoneClearanceOrderEvent.java](../../bns-client/src/main/java/com/itrustmachines/client/verify/vo/DoneClearanceOrderEvent.java)
  
  ```java
  public class DoneClearanceOrderEvent {
    private long doneClearanceOrder;        // BNS Server 目前的已清算序號
  }
  ```

- [CallbackSample.java](../src/main/java/com/itrustmachines/sample/CallbackSample.java)
  
  ```java
  public void obtainDoneClearanceOrderEvent(DoneClearanceOrderEvent doneClearanceOrderEvent) {
  }
  ```

### obtainMerkleProof 說明

##### BNS Client 驗證回條前，會先向 BNS Server 拿取 MerkleProof 作為驗證依據，若您想將 MerkleProof Callback，可閱讀下方說明及程式

- 當 BNS Client 要驗證回條時，BNS Client 會先透過 `doneClearanceOrder` 尋找待驗證回條，並向 BNS Server 取得待驗證回條雜湊值所在葉節點的 Merkle Proof，才會開始驗證回條。

- 開發者可在 [CallbackSample.java](../src/main/java/com/itrustmachines/sample/CallbackSample.java) 的 `obtainMerkleProof` 撰寫程式將 `merkleProof` 的資訊 Callback 至自己的系統

- [MerkleProof.java](../../spo-common-domain-objects/src/main/java/com/itrustmachines/common/vo/MerkleProof.java)

  ```java
  public class MerkleProof implements Serializable {
    private String slice;               // TP-Merkle Tree 的切片資訊
    private List<PBPairValue> pbPair;   // 回條在 TP-Merkle Tree 的葉節點資訊
    private Long clearanceOrder;        // 此回條在 BNS Server 上的清算序號
    private SpoSignature sigServer;     // BNS Server 的電子簽章
  }
  ```

- [CallbackSample.java](../src/main/java/com/itrustmachines/sample/CallbackSample.java)
  
  ```java
  public void obtainMerkleProof(ReceiptLocator receiptLocator, MerkleProof merkleProof) {
  }
  ```

### getVerifyReceiptResult 說明

##### BNS Client 取得 MerkleProof 後會開始驗證回條，並將驗證結果放入 verifyReceiptResult，若您想將 verifyReceiptResult Callback，可閱讀下方說明及程式

- BNS Client 取得 `merkleProof` 後會開始驗證回條

- 開發者可在 [CallbackSample.java](../src/main/java/com/itrustmachines/sample/CallbackSample.java) 的 `getVerifyReceiptResult` 撰寫程式將 `verifyReceiptResult` 的資訊 Callback 至自己的系統
 
- **建議實作將 `verifyReceiptResult` 中的 `indexValue` 和 `clearanceOrder` Callback，才能更新驗證資料**

- [VerifyReceiptAndMerkleProofResult.java](../../spo-common-verification/src/main/java/com/itrustmachines/verification/vo/VerifyReceiptAndMerkleProofResult.java)

  ```java
  public class VerifyReceiptAndMerkleProofResult {
    private Long clearanceOrder;                // ledgerInputRequest 裡的 clearanceOrder
    private String indexValue;                  // ledgerInputRequest 裡的 indexValue
    private ExistenceType existenceType;        // 存證結果是否存在於證據中
    private boolean pass;                       // 驗證結果
    private String status;                      // request 的狀態
    private ProofExistStatus proofExistStatus;  // 證據存證的狀態
    private String txHash;                      // 區塊鏈交易的 hash 值
    private Long timestamp;                     // 驗證時間
    private Long ledgerInputTimestamp;          // ledgerInputRequest 裡的 timestamp
    private Long receiptTimestamp;              // receipt 裡的 timestamp
    private String merkleProofRootHash;         // merkle proof 的 root hash
    private String contractRootHash;            // 存證內容在區塊鏈上的 hash 值
    private String description;                 // 對 request 狀態的描述
    private String cmd;                         // 存證內容
    private String descriptionReport;           // 描述報告
    private String verifyPbPairsReport;         // 驗證 pbPair 的報告
    private String verifySliceReport;           // 驗證 slice 的報告
    private boolean merkleproofSignatureOk;     // merkleProofSignature 的驗證結果
    private boolean receiptSignatureOk;         // receiptSignature 的驗證結果
    private boolean clearanceOrderOk;           // clearanceOrder 的驗證結果
    private boolean pbPairOk;                   // pbPair 的驗證結果
    private boolean sliceOk;                    // slice 的驗證結果
    private boolean clearanceRecordRootHashOk;  // clearanceRecordRootHash 的驗證結果
    private VerifyNotExistProofStatus verifyNotExistProofResult; // 不存在證據的驗證結果
  }
  ```

- [CallbackSample.java](../src/main/java/com/itrustmachines/sample/CallbackSample.java)
  
  ```java
  public void getVerifyReceiptResult(Receipt receipt, MerkleProof merkleProof,
      VerifyReceiptAndMerkleProofResult verifyReceiptAndMerkleProofResult) {
  }
  ```

您現在已經了解 BNS Client 和 BNS Server 溝通時會發生的事件以及 Callback 所有功能。接下來我們將引導您了解 BNS Client 如何使用 receiptDao 對回條進行存取並找尋待驗證的回條，

----

- [下一頁 : ReceiptDao 功能說明](./receiptDao_zh.md)
- [上一頁 : CMD 設計說明](./cmd_zh.md)
