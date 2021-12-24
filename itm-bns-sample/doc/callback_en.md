## Build the Callback Applications

### About the Callback

In this document, we will teach you how to develop the Callbacks method to help you intergrate our BNS SDK with your applicaitons or systems. BNS Java Client will call APIs to interact with BNS server. We provide the 7 Callback methods to return each APIs request or response. We will introuduce these Callback methods in following documents.

Before starting to go throught the documents. you can refer to [BNS API Doc](https://azure-dev-membership.itm.monster/api) to learn basic API informations.

### Prerequisites

- Complete quickstarts document
- Complete build the CMD document

### Callback methods

1. `register` : When initialize the BNS Client, BNS Client will call `Confirm wallet address API` to check whether your MetaMask wallet address has been successfully registered with in ITM BNS. Developers can implement the code in `register` method to return the `registerRequest` and `registerResult`.

2. `createLedgerInputByCmd` : After successfully initialize the BNS Client, BNS Client will build `ledgerInputRequest` with `CMD` and other informations. Developers can implement the code in `createLedgerInputByCmd` method to callback the `ledgerInputRequest`.

3. `obtainLedgerInputResponse` : BNS Client will receive `ledgerInputResponse` from BNS Server after sending the `ledgerInput`. Developers can implement the code in `obtainLedgerInputResponse` method to callback the `ledgerInputResponse`.

4. `obtainReceiptEvent` : The `receipt` is contained in `ledgerInputResponse`. BNS will use receipt to verify the verification proof. Developers can implement the code in `obtainReceiptEvent` method to callback the `receipt`.

5. `obtainDoneClearanceOrderEvent` : The `doneClearanceOrder` is contained in `ledgerInputResponse`. BNS Client will use `doneClearanceOrder` to find out which receipts need to be verified. Developers can implement the code in `obtainDoneClearanceOrderEvent` method to callback the `doneClearanceOrder`.

6. `obtainMerkleProof` : Before verifying the receipt, BNS Client will call `Get Merkle proof API` to request the `merkleProof` of those to be verified receipts from the BNS Server. The Merkle Proof is evidence of receipt verification. BNS Client will use Merkle proof to verify the receipt whether the receipt is in the TP-merkle tree. Developers can implement the code in `obtainMerkleProof` method to callback the `merkleProof`.

7. `getVerifyReceiptResult` : After receiving the Merkle Proof. BNS Client will start to verify the receipt then store the verification result into `verifyReceiptResult`. Developers can implement the code in `getVerifyReceiptResult` method to callback the `verifyReceiptResult`.

### register

**When initialize the BNS Client, BNS Client will call `Confirm wallet address API` to check whether your wallet address has been successfully registered with in ITM BNS. Developers can implement the code in `register` method to return the `registerRequest` and `registerResult`.**

- [RegisterRequest.java](../../bns-client/src/main/java/com/itrustmachines/client/register/vo/RegisterRequest.java)

  ```java
  public class RegisterRequest implements Serializable, Cloneable {
    private String address;         // MetaMask wallet address
    private String email;           // The email you sign up for BNS service
    private String toSignMessage;   // address
    private SpoSignature sig;       // Use SEC256K1 algorithm to get signature of toSignMessage
  }
  ```

- [CallbackSample.java](../src/main/java/com/itrustmachines/sample/CallbackSample.java)

  ```java
  public void register(RegisterRequest registerRequest, Bool registerResult) {
  }
  ```

### createLedgerInputByCmd

**After successful initialization the BNS Client, BNS Client will build `ledgerInputRequest` with `CMD` and other information. Developers can implement the code in `createLedgerInputByCmd` method to callback the `ledgerInputRequest`.**

- [LedgerInputRequest.java](../../bns-client/src/main/java/com/itrustmachines/client/input/vo/LedgerInputRequest.java)
  
  ```java
  public class LedgerInputRequest implements Serializable, Cloneable {
    private String callerAddress;   // MetaMask wallet address
    private String timestamp;       // The time when you POST ledgerInputRequest
    private String cmd;             // Attestation data
    private String indexValue;      // IndexValueLey + 'R' + SN，indexValueKey is your MetaMask wallet address
    private String metadata;        // Description of attestation data 
    private Long clearanceOrder;    // current clearanceOrder of BNS Client
    private SpoSignature sigClient; // Signature of your ledgerInputRequest
  }
  ```

- [CallbackSample.java](../src/main/java/com/itrustmachines/sample/CallbackSample.java)
  We recommend you to store `indexValue` and `clearanceOrder` in `receiptLocator` so that you can search your records conveniently.

  ```java
  public void createLedgerInputByCmd(ReceiptLocator receiptLocator, LedgerInputRequest ledgerInputRequest) {
  }
  ```

### obtainLedgerInputResponse

**BNS Client will receive `ledgerInputResponse` from BNS Server after sending the `ledgerInput`. Developers can implement the code in `obtainLedgerInputResponse` method to callback the `ledgerInputResponse`.**

- [LedgerInputResponse.java](../../bns-client/src/main/java/com/itrustmachines/client/input/vo/LedgerInputResponse.java)
  
  ```java
  public class LedgerInputResponse {
    private String status;                      // The status of your ledgerInputRequest
    private String description;                 // The description of this ledgerInputRequest
    private Receipt receipt;                    // Receipt
    private List<Long> doneClearanceOrderList;  // Current BNS Server done clearanceOrder
  }
  ```

- [CallbackSample.java](../src/main/java/com/itrustmachines/sample/CallbackSample.java)
  We recommend you to store `indexValue` and `clearanceOrder` in `receiptLocator` so that you can search your records conveniently.
  
  ```java
  public void obtainLedgerInputResponse(ReceiptLocator locator, String cmdJson,
      LedgerInputResponse ledgerInputResponse) {
  }
  ```

### obtainReceiptEvent

**The `receipt` is contained in `ledgerInputResponse`. Developers can implement the code in `obtainReceiptEvent` method to callback the `receipt`.**

- [Receipt.java](../../spo-common-domain-objects/src/main/java/com/itrustmachines/common/vo/Receipt.java)
  
  ```java
  public class Receipt implements Serializable, Cloneable {
    private String callerAddress;       // MetaMask wallet address
    private Long timestamp;             // The time when you POST ledgerInputRequest
    private String cmd;                 // Attestation data
    private String indexValue;          // IndexValueLey + 'R' + SN，indexValueKey is your MetaMask wallet address
    private String metadata;            // Description of attestation data
    private Long clearanceOrder;        // current clearanceOrder of BNS Client
    private SpoSignature sigClient;     // Signature of your ledgerInputRequest
    private Long timestampSPO;          // The time when BNS Server receive your ledgerInputRequest
    private String result;              // The result of ledgerInput
    private SpoSignature sigServer;     // The signature from BNS
  }
  ```

- [CallbackSample.java](../src/main/java/com/itrustmachines/sample/CallbackSample.java)
  
  We recommend you to store `indexValue` and `clearanceOrder` in `receiptLocator` so that you can search your records conveniently.
  
  ```java
  public void obtainReceiptEvent(ReceiptEvent receiptEvent) {
  }
  ```

### obtainDoneClearanceOrderEvent

**The `doneClearanceOrder` is contained in `ledgerInputResponse` . BNS Client will use `doneClearanceOrder` to find out which receipts need to be verified. Developers can implement the code in `obtainDoneClearanceOrderEvent` method to callback the `doneClearanceOrder`.**

- [DoneClearanceOrderEvent.java](../../bns-client/src/main/java/com/itrustmachines/client/verify/vo/DoneClearanceOrderEvent.java)

  ```java
  public class DoneClearanceOrderEvent {
    private long doneClearanceOrder;        // current BNS Server done clearanceOrder
  }
  ```

- [CallbackSample.java](../src/main/java/com/itrustmachines/sample/CallbackSample.java)

  ```java
  public void obtainDoneClearanceOrderEvent(DoneClearanceOrderEvent doneClearanceOrderEvent) {
  }
  ```

### obtainMerkleProof

**Before verifying the receipt, BNS Client will call `Get Merkle proof API` to request the `merkleProof` of those to be verified receipts from the BNS Server. The Merkle Proof is evidence of receipt verification. BNS Client will use Merkle proof to verify the receipt whether receipt is in the TP-merkle tree. Developers can implement the code in `obtainMerkleProof` method to callback the `merkleProof`**

- [MerkleProof.java](../../spo-common-domain-objects/src/main/java/com/itrustmachines/common/vo/MerkleProof.java)

  ```java
  public class MerkleProof implements Serializable {
    private String slice;               // The slice of TP-Merkle Tree
    private List<PBPairValue> pbPair;   // The information in the leaf node of TP-Merkle Tree
    private Long clearanceOrder;        // The clearanceOrder of this to be verified receipt
    private SpoSignature sigServer;     // The signature from BNS Server
  }
  ```

- [CallbackSample.java](../src/main/java/com/itrustmachines/sample/CallbackSample.java)

  ```java
  public void obtainMerkleProof(ReceiptLocator receiptLocator, MerkleProof merkleProof) {
  }
  ```

### getVerifyReceiptResult

**After receiving the Merkle Proof. BNS Client will start to verify the receipt and store the result to `verifyReceiptResult`. Developers can implement the code in `getVerifyReceiptResult` method to callback the `verifyReceiptResult`.**

- [VerifyReceiptAndMerkleProofResult.java](../../spo-common-verification/src/main/java/com/itrustmachines/verification/vo/VerifyReceiptAndMerkleProofResult.java)

  ```java
  public class VerifyReceiptAndMerkleProofResult {
    private Long clearanceOrder;                // The clearanceOrder field in ledgerInputRequest body.
    private String indexValue;                  // The indexValue field in ledgerInputRequest body.
    private ExistenceType existenceType;        // Whether the attestation in proof.
    private boolean pass;                       // The result of verification.
    private String status;                      // The status of request.
    private ProofExistStatus proofExistStatus;  // The exist status of proof.
    private String txHash;                      // The hash value of transaction.
    private Long timestamp;                     // The time when verify receipt
    private Long ledgerInputTimestamp;          // The timestamp in ledgerInputRequest.
    private Long receiptTimestamp;              // The timestamp in receipt.
    private String merkleProofRootHash;         // The root hash of merkle proof.
    private String contractRootHash;            // The hash value of attestation data on blockchain.
    private String description;                 // The description of request.
    private String cmd;                         // Attestation data.
    private String descriptionReport;           // The report of description
    private String verifyPbPairsReport;         // The log when verify PbPair
    private String verifySliceReport;           // The log when verify slice
    private boolean merkleproofSignatureOk;     // The verify result of merkle proof
    private boolean receiptSignatureOk;         // The verify result of receipt signature
    private boolean clearanceOrderOk;           // The verify result of clearanceOrder
    private boolean pbPairOk;                   // The verify result of pbPair
    private boolean sliceOk;                    // The verify result of slice
    private boolean clearanceRecordRootHashOk;  // The verify result of clearanceRecord root hash
    private VerifyNotExistProofStatus verifyNotExistProofResult; // The result of not exist proof verification
  }
  ```

- [CallbackSample.java](../src/main/java/com/itrustmachines/sample/CallbackSample.java)

  ```java
  public void getVerifyReceiptResult(Receipt receipt, MerkleProof merkleProof,
      VerifyReceiptAndMerkleProofResult verifyReceiptAndMerkleProofResult) {
  }
  ```

----
Build the Callback Applications document is now complete. Next, learn how to build the ReceiptDao Applications

## Next Steps

Next Page : [Build the ReceiptDao Applications](./receiptDao_en.md)  
Last Page : [Build the CMD](./cmd_en.md)