## Build the Callback Applications

### About the Callback

The Callbacks send the events between SPO Client and SPO Server to the ITM Dashboard or your system. We define 8 events that you can callback. We will introduce these Callbacks in the following document. The document include two parts. The first part [(Basic)](#basic) will introduce two basic callbacks and send the events to the ITM Dashboard. The second part [(Advanced)](#advanced) will introduce all Callbacks and let you integrate the Callbacks into your system, such as server, database, and other services.

### Prerequisites

- Complete quickstarts document
- Complete build the CMD document

### Events

![](../image/callback_number.png)

1. `register` : When initialize the SPO Client, SPO Client will send `registerRequest` to SPO Server and receive `registerResult` from SPO Server. Developers can implement the code in `register` method to callback the informations in `registerRequest` and `registerResult`.

2. `createLedgerInputByCmd` :

   - After successfully initializing the SPO Client, SPO Client will store CMD and other data in `ledgerInputRequest` and do **ledgerInput** to send `ledgerInputRequest` to the SPO Server. Developers can implement the code in `createLedgerInputByCmd` method to callback the information in `ledgerInputRequest`.

   - If developes want to use verification server to verify the file which introduced in CMD document, SPO Client will do **binaryLedgerInput** to send `ledgerInputRequest` to SPO Server. Developers can implement the code in `createLedgerInputByCmd` method to callback the informations in `ledgerInputRequest`.

3. `obtainLedgerInputResponse` : SPO Client will receive `ledgerInputResponse` from SPO Server after sending `ledgerInputRequest`. Developers can implement the code in `obtainLedgerInputResponse` method to callback the informations in `ledgerInputResponse`.

4. `obtainBinaryLedgerInputResponse` : If developers use `binaryLedgerInput` method, SPO Client will do binaryLedgerInput to send `ledgerInputRequest` to SPO server and receive `binaryLedgerInputResponse` from SPO Server. Developes can implement the code in `obtainBinaryLedgerInputResponse` method to callback the informations in `binaryLedgerInputResponse`.

5. `obtainReceiptEvent` : The `receipt` is contained in `ledgerInputResponse` / `binaryLedgerInputResponse`. Developers can implement the code in `obtainReceiptEvent` method to callback the informations in `receipt`.

6. `obtainDoneClearanceOrderEvent` : The `doneClearanceOrder` is contained in `ledgerInputResponse` / `binaryLedgerInputResponse`. SPO Client will use `doneClearanceOrder` to find out which receipts need to be verified. Developers can implement the code in `obtainDoneClearanceOrderEvent` method to callback the informations in `doneClearanceOrder`.

7. `obtainMerkleProof` : Before verifying the receipt, SPO Client will request the `merkleProof` of the receipts to be verified from the Server. The Merkle Proof is evidence of receipt verification. SPO Client will use Merkle proof to verify the receipt whether receipt is in the TP-merkle tree. Developers can implement the code in `obtainMerkleProof` method to callback the informations in `merkleProof`.

8. `getVerifyReceiptResult` : After receiving the Merkle Proof. SPO Client will start to verify the receipt and store the result to `verifyReceiptResult`. Developers can implement the code in `getVerifyReceiptResult` method to callback the informations in `verifyReceiptResult`.

### Basic

We are going to use ITM Dashboard as a example to demonstrate the two basic callbacks `ledger_input_response_callback` and `verify_receipt_result_callback`.

we recommand that you can reference the code and document at the same time so that you can understand the callback applications more easily.

![](../image/callback_easy.png)

#### obtainLedgerInputResponse

- To display every `ledgerInputResponse` informations on ITM Dashboard, we copy `clearanceOrder` and `indexValue` from `ledgerInputResponse` and paste to `receiptLocator`. These two variables is very important, we can use `clearanceOrder` and `indexValue` to calculate the location of receipt .

- Second, we call `postDeviceData` method to extract `status` from `ledgerInputResponse` then store `receiptLocator`, `status`, and `cmd` in JSON data type variable, `req`.

- Third, `postDeviceData` POST `req` to the ITM Dashboard via `device-data-input-api`

- For further API informations, please refer to [ITM Dashboard API](https://azure-prod-rinkeby.itm.monster:8443/swagger-ui/)

- For API URL setting, please refer to [DashboardService.java](../src/main/java/com/itrustmachines/sample/DashboardService.java)

  ```java
  public class DashboardService {
  
  public static final String DEVICE_DATA_PATH = "/device/data";
  public static final String LOG_VERIFY_RECEIPT_RESULT = "/log/verifyReceiptResult";
  public static final String LOG_VERIFY_RECEIPT_RESULTS = "/log/verifyReceiptResults";
  public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
  
  private final Gson gson;
  private final String dashboardUrl;
  private final OkHttpClient okHttpClient;
  
  public DashboardService(String dashboardUrl) {
    this.gson = new Gson();
    this.dashboardUrl = UrlUtil.urlWithoutSlash(dashboardUrl);
    this.okHttpClient = new OkHttpClient();
  }
  ```

- For the code of `obtainLedgerInputResponse`, please refer to [CallbackSample.java](../src/main/java/com/itrustmachines/sample/CallbackSample.java)
  
  ```java
  public void obtainLedgerInputResponse(ReceiptLocator locator, String cmdJson,
      LedgerInputResponse ledgerInputResponse) {

    final DashboardService.DeviceDataResponse response;
    if (StatusConstants.OK.name()
                          .equalsIgnoreCase(ledgerInputResponse.getStatus())) {
      final Receipt receipt = ledgerInputResponse.getReceipt();
      final ReceiptLocator receiptLocator = ReceiptLocator.builder()
                                                          .indexValue(receipt.getIndexValue())
                                                          .clearanceOrder(receipt.getClearanceOrder())
                                                          .build();
      final Cmd cmd = new Gson().fromJson(receipt.getCmd(), Cmd.class);
      response = dashboardService.postDeviceData(receiptLocator, ledgerInputResponse.getStatus(), cmd);
    } else {
      final Cmd cmd = new Gson().fromJson(cmdJson, Cmd.class);
      response = dashboardService.postDeviceData(locator, ledgerInputResponse.getStatus(), cmd);
    }
    log.info("obtainLedgerInputResponse() dashboard response={}", response);
  }
  ```

- For the code of `postDeviceData`, please refer to [DashboardService.java](../src/main/java/com/itrustmachines/sample/DashboardService.java)
  
  ```java
  public DeviceDataResponse postDeviceData(@NonNull final ReceiptLocator locator, @NonNull final String status,
      @NonNull final Object cmd) {
    
    log.debug("postDeviceData() begin, locator={}, status={}, cmd={}", locator, status, cmd);
    final DeviceDataRequest req = DeviceDataRequest.builder()
                                                   .clearanceOrder(locator.getClearanceOrder())
                                                   .indexValue(locator.getIndexValue())
                                                   .status(status)
                                                   .cmd(cmd)
                                                   .build();
    
    final String url = dashboardUrl + DEVICE_DATA_PATH;
    final Request request = new Request.Builder().url(url)
                                                 .post(RequestBody.create(gson.toJson(req), JSON))
                                                 .build();
    
    DeviceDataResponse res;
    try (Response response = okHttpClient.newCall(request)
                                         .execute()) {
      final String resString = Objects.requireNonNull(response.body())
                                      .string();
      res = gson.fromJson(resString, DeviceDataResponse.class);
      
      log.debug("postDeviceData() end, res={}", res);
      return res;
    }
  }
  ```

#### getVerifyReceiptResult

- First, `getVerifyReceiptResult` call `postVerifyReceiptAndMerkleProofResult` method to callback the informations in the `verifyReceiptAndMerkleProofResult`

- Second, convert `verifyReceiptAndMerkleProofResult` to JSON data type variable, `request`

- Third, POST `request` to ITM Dashboard via `verify-log-api`.

- For further API informations, please refer to [ITM Dashboard API](https://azure-prod-rinkeby.itm.monster:8443/swagger-ui/)

- For API URL setting, please refer to [DashboardService.java](../src/main/java/com/itrustmachines/sample/DashboardService.java)

  ```java
  public class DashboardService {
  
  public static final String DEVICE_DATA_PATH = "/device/data";
  public static final String LOG_VERIFY_RECEIPT_RESULT = "/log/verifyReceiptResult";
  public static final String LOG_VERIFY_RECEIPT_RESULTS = "/log/verifyReceiptResults";
  public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
  
  private final Gson gson;
  private final String dashboardUrl;
  private final OkHttpClient okHttpClient;
  
  public DashboardService(String dashboardUrl) {
    this.gson = new Gson();
    this.dashboardUrl = UrlUtil.urlWithoutSlash(dashboardUrl);
    this.okHttpClient = new OkHttpClient();
  }
  ```

- For the code of `getVerifyReceiptResult`, please refer to [CallbackSample.java](../src/main/java/com/itrustmachines/sample/CallbackSample.java)
  
  ```java
  public void getVerifyReceiptResult(Receipt receipt, MerkleProof merkleProof,
      VerifyReceiptAndMerkleProofResult verifyReceiptAndMerkleProofResult) {

    final DashboardService.VerifyReceiptResultResponse response = dashboardService.postVerifyReceiptAndMerkleProofResult(
        verifyReceiptAndMerkleProofResult);
    log.info("getVerifyReceiptResult() dashboard response={}", response);
  }
  ```

- For the code of `postVerifyReceiptAndMerkleProofResult`, please refer to [dashboard_service.c](../example/spo-client-example/dashboard_service.c)
  
  ```java
  public VerifyReceiptResultResponse postVerifyReceiptAndMerkleProofResult(
      @NonNull final VerifyReceiptAndMerkleProofResult verifyResult) {
    log.debug("postVerifyReceiptAndMerkleProofResult() begin, verifyResult={}", verifyResult);
    
    final String url = dashboardUrl + LOG_VERIFY_RECEIPT_RESULT;
    
    final Request request = new Request.Builder().url(url)
                                                 .post(RequestBody.create(gson.toJson(verifyResult), JSON))
                                                 .build();
    
    VerifyReceiptResultResponse res;
    try (final Response response = okHttpClient.newCall(request)
                                               .execute()) {
      final String resString = Objects.requireNonNull(response.body())
                                      .string();
      res = gson.fromJson(resString, VerifyReceiptResultResponse.class);
      log.debug("postVerifyReceiptAndMerkleProofResult() end, res={}", res);
      return res;
    }
  }
  ```

### Advanced

![](../image/callback_advanced.png)

#### register

**When initialize the SPO Client, SPO Client will send `registerRequest` to SPO Server and receive `registerResult` from SPO Server. Developers can implement the code in `register` callback method to callback the informations in `registerRequest` and `registerResult`.**

- First, SPO Client call `register` method to start to register with SPO Server

- Second, `register` method call `buildRegisterRequest` method to build `registerRequest` with `callerAddress` and `publicKey` then sign `registerRequest` with your `privateKey` to ensure the security

- Third, `register` method call `postRegister` method to POST `registerRequest` via API and receive the response from SPO Server

- After receiving and checking the response, SPO Client will call `register` callback method to callback the informations in `registerRequest` and `registerResult`

- For further API informations, please refer to [SPO Server API](https://azure-prod-rinkeby.itm.monster:4430/swagger-ui/)

- For API URL setting, please refer to [RegisterService.java](../../spo-client/src/main/java/com/itrustmachines/client/register/service/RegisterService.java)

  ```java
  private static final String API_PATH = "/ledger/register";
  ```

- For the code of initializing, please refer to [SpoClient.java](../../spo-client/src/main/java/com/itrustmachines/client/BnsClient.java)
  
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

- For the code of `register` method, please refer to [RegisterService.java](../../spo-client/src/main/java/com/itrustmachines/client/register/service/RegisterService.java)
  
  ```java
  public boolean register() {
    final RegisterRequest registerReq = buildRegisterRequest(keyInfo);
    
    RegisterResponse res = null;
    for (int retryCount = 0; retryCount <= MAX_RETRY_TIMES; retryCount++) {
      log.debug("register() retryCount={}", retryCount);
      try {
        res = postRegister(registerReq);
        log.debug("register() res={}", res);
        if (checkResponse(res)) {
          break;
        }
      } catch (Exception e) {
        // ...
      }
    }
    
    if (res == null) {
      String errMsg = "response is null";
      log.error("register() fail, {}", errMsg);
      throw new NullPointerException(errMsg);
    }
    
    try {
      spoClientCallback.register(registerReq, res.getStatus());
    } catch (Exception e) {
      // ...
      log.error("register() callback register error, registerReq={}, status={}", registerReq, res.getStatus(), e);
    }
    
    final boolean result = checkRegisterResult(res);
    log.debug("register() result={}", result);
    return result;
  }
  ```

- For the code of `register` callback method, please refer to [CallbackSample.java](../src/main/java/com/itrustmachines/sample/CallbackSample.java)

  ```java
  public void register(RegisterRequest registerRequest, String registerResult) {
  }
  ```

#### createLedgerInputByCmd

**After successfully initializing the SPO Client, SPO Client will store CMD and other data in `ledgerInputRequest` and do ledgerInput / binaryLedgerInput to send `ledgerInputRequest` to the SPO Server. Developers can implement the code in `createLedgerInputByCmd` method to callback the information in `ledgerInputRequest`.**

- SPO Client will call `ledgerInput` / `binaryLedgerInput` method to do ledgerInput / binaryLedgerInput.

- Before doing ledgerInput / binaryLedgerInput, `ledgerInput` / `binaryLedgerInput` method will call `buildLedgerInputRequest` method to build `ledgerInputRequest` with `cmdJSON` and sign the `ledgerInputRequest` with `privateKey`

- After building `ledgerInputRequest`, SPO Client will POST `ledgerInputRequest` to SPO Server via API and receive the response from SPO Server

- For further API informations, please refer to [SPO Server API](https://azure-prod-rinkeby.itm.monster:4430/swagger-ui/)

- For API URL setting, please refer to [LedgerInputService.java](../../spo-client/src/main/java/com/itrustmachines/client/input/service/LedgerInputService.java)
  
  ```java
  private static final String API_PATH = "/ledger/input";
  ```

- For the code of `SPO Client` method, please refer to [SpoClient.java](../../spo-client/src/main/java/com/itrustmachines/client/BnsClient.java)
  
  ```java
  public LedgerInputResponse ledgerInput(@NonNull final String indexValueKey, @NonNull final String cmdJson) {
    return ledgerInputService.ledgerInput(indexValueKey, cmdJson);
  }
  
  public LedgerInputResponse ledgerInput(@NonNull final String cmdJson) {
    return ledgerInputService.ledgerInput(config.getIndexValueKey(), cmdJson);
  }
  
  public BinaryLedgerInputResponse binaryLedgerInput(@NonNull final String indexValueKey, @NonNull final String cmdJson,
      @NonNull final Path binaryPath) {
    return binaryLedgerInputService.binaryLedgerInput(indexValueKey, cmdJson, binaryPath);
  }
  
  public BinaryLedgerInputResponse binaryLedgerInput(@NonNull final String cmdJson, @NonNull final Path binaryPath) {
    return binaryLedgerInputService.binaryLedgerInput(config.getIndexValueKey(), cmdJson, binaryPath);
  }
  ```

- For the code of `ledgerInput` method, please refer to [LedgerInputService.java](../../spo-client/src/main/java/com/itrustmachines/client/input/service/LedgerInputService.java)
  
  ```java
  public LedgerInputResponse ledgerInput(final @NonNull String indexValueKey, final @NonNull String cmdJson) {
    log.debug("ledgerInput() begin, indexValueKey={}, cmdJson={}", indexValueKey, cmdJson);
    
    // do ledger input, resend if necessary
    ReceiptLocator locator = null;
    LedgerInputRequest ledgerInputRequest;
    LedgerInputResponse res = null;
    for (int resendCount = 0; resendCount <= MAX_RESEND_TIMES; resendCount++) {
      log.debug("ledgerInput() resendCount={}", resendCount);
      try {
        // build LedgerInput by cmdJson
        locator = receiptLocatorService.obtainReceiptLocator(indexValueKey);
        ledgerInputRequest = buildLedgerInputRequest(locator, cmdJson);
        try {
          callback.createLedgerInputByCmd(locator, ledgerInputRequest);
        } catch (Exception e) {
          // ...
        }
        res = postLedgerInput(ledgerInputRequest);
        log.debug("ledgerInput() res={}", res);
        if (checkResponse(res)) {
          break;
        }
      } catch (Exception e) {
        // ...
        TimeUnit.SECONDS.sleep(retryDelaySec);
      }
    }
    // ...
  }

  LedgerInputRequest buildLedgerInputRequest(final @NonNull ReceiptLocator locator, final @NonNull String cmdJson) {
    final LedgerInputRequest result = LedgerInputRequest.builder()
                                                        .callerAddress(keyInfo.getAddress())
                                                        .timestamp("" + System.currentTimeMillis())
                                                        .cmd(cmdJson)
                                                        .indexValue(locator.getIndexValue())
                                                        .metadata("")
                                                        .clearanceOrder(locator.getClearanceOrder())
                                                        .build()
                                                        .sign(keyInfo.getPrivateKey());
    log.debug("buildLedgerInputRequest() result={}", result);
    return result;
  }
  ```

- For the code of `binaryLedgerInput` method, please refer to [binaryLedgerInputService.java](../../spo-client/src/main/java/com/itrustmachines/client/input/service/BinaryLedgerInputService.java)
  
  ```java
  public BinaryLedgerInputResponse binaryLedgerInput(@NonNull final String indexValueKey, @NonNull final String cmdJson,
      @NonNull final Path binaryPath) {
    log.debug("binaryLedgerInput() begin, indexValueKey={}, cmdJson={}, binaryPath={}", indexValueKey, cmdJson,
        binaryPath);
    
    // do ledger input, resend if necessary
    ReceiptLocator locator = null;
    LedgerInputRequest ledgerInputRequest;
    BinaryLedgerInputResponse res = null;
    for (int resendCount = 0; resendCount <= MAX_RESEND_TIMES; resendCount++) {
      log.debug("binaryLedgerInput() resendCount={}", resendCount);
      try {
        // build ledgerInput by cmdJson
        locator = receiptLocatorService.obtainReceiptLocator(indexValueKey);
        ledgerInputRequest = buildLedgerInputRequest(locator, cmdJson);
        try {
          callback.createLedgerInputByCmd(locator, ledgerInputRequest);
        } catch (Exception e) {
          // ...
        }
        res = postBinaryLedgerInput(ledgerInputRequest, binaryPath);
        log.debug("binaryLedgerInput() res={}", res);
        if (checkResponse(res)) {
          break;
        }
      } catch (Exception e) {
        // ...
        TimeUnit.SECONDS.sleep(retryDelaySec);
      }
    }
    // ...
  }
  ```

- For the code of `createLedgerInputByCmd` callback method, please refer to [CallbackSample.java](../src/main/java/com/itrustmachines/sample/CallbackSample.java)

  ```java
  public void createLedgerInputByCmd(ReceiptLocator receiptLocator, LedgerInputRequest ledgerInputRequest) {
  }
  ```

#### obtainLedgerInputResponse

**SPO Client will receive `ledgerInputResponse` from SPO Server after sending `ledgerInputRequest`. Developers can implement the code in `obtainLedgerInputResponse` method to callback the informations in `ledgerInputResponse`.**

- After receiving the response from SPO Server, SPO Client will call `checkResponse` method to examine the response contents. If there is no problem, then `ledgerInput` method will call `obtainLedgerInputResponse` method to callback the informations in `ledgerInputResponse`

- For the code of `ledgerInput` method, please refer to [LedgerInputService.java](../../spo-client/src/main/java/com/itrustmachines/client/input/service/LedgerInputService.java)

  ```java
  public LedgerInputResponse ledgerInput(final @NonNull String indexValueKey, final @NonNull String cmdJson) {
    log.debug("ledgerInput() begin, indexValueKey={}, cmdJson={}", indexValueKey, cmdJson);
    
    // do ledger input, resend if necessary
    ReceiptLocator locator = null;
    LedgerInputRequest ledgerInputRequest;
    LedgerInputResponse res = null;
    for (int resendCount = 0; resendCount <= MAX_RESEND_TIMES; resendCount++) {
      log.debug("ledgerInput() resendCount={}", resendCount);
      try {
        // build LedgerInput by cmdJson
        // ...
        try {
          callback.createLedgerInputByCmd(locator, ledgerInputRequest);
        } catch (Exception e) {
          // ...
        }
        res = postLedgerInput(ledgerInputRequest);
        log.debug("ledgerInput() res={}", res);
        if (checkResponse(res)) {
          break;
        }
      } catch (Exception e) {
        // ...
      }
    }
    if (res == null) {
      String errMsg = "response is null";
      log.error("ledgerInput() fail, {}", errMsg);
      throw new NullPointerException(errMsg);
    }
    
    try {
      callback.obtainLedgerInputResponse(locator, cmdJson, res);
    } catch (Exception e) {
      // ...
    }
    // ...
    handleReceipt(res);
    handleDoneClearanceOrderList(res);
    return res;
  }
  ```

- For the code of `obtainLedgerInputResponse` callback method, please refer to [CallbackSample.java](../src/main/java/com/itrustmachines/sample/CallbackSample.java)

  ```java
  public void obtainLedgerInputResponse(ReceiptLocator locator, String cmdJson,
      LedgerInputResponse ledgerInputResponse) {
      }
  ```

#### obtainBinaryLedgerInputResponse

**If developers use binaryLedgerInput method, SPO Client will do binaryLedgerInput to send `ledgerInputRequest` to SPO server and receive `binaryLedgerInputResponse` from SPO Server then call this callback method. Developes can implement the code in `obtainBinaryLedgerInputResponse` method to callback the informations in `binaryLedgerInputResponse`.**

- After receiving the response from SPO Server, SPO Client will call `checkResponse` method to examine the response contents. If there is no problem, then `binaryLedgerInput` method will call `obtainBinaryLedgerInputResponse` method to callback the informations in `binaryLedgerInputResponse`

- If you want to send the event to ITM Dashboard, please refer to [ITM Dashboard API](https://azure-prod-rinkeby.itm.monster:8443/swagger-ui/) for API informations.

- For the code of `binaryLedgerInput` method, please refer to [binaryLedgerInputService.java](../../spo-client/src/main/java/com/itrustmachines/client/input/service/BinaryLedgerInputService.java)

  ```java
  public BinaryLedgerInputResponse binaryLedgerInput(@NonNull final String indexValueKey, @NonNull final String cmdJson,
      @NonNull final Path binaryPath) {
    log.debug("binaryLedgerInput() begin, indexValueKey={}, cmdJson={}, binaryPath={}", indexValueKey, cmdJson,
        binaryPath);
    
    // do ledger input, resend if necessary
    ReceiptLocator locator = null;
    LedgerInputRequest ledgerInputRequest;
    BinaryLedgerInputResponse res = null;
    for (int resendCount = 0; resendCount <= MAX_RESEND_TIMES; resendCount++) {
      log.debug("binaryLedgerInput() resendCount={}", resendCount);
      try {
        // build ledgerInput by cmdJson
        // ...
        try {
          callback.createLedgerInputByCmd(locator, ledgerInputRequest);
        } catch (Exception e) {
          // ...
        }
        res = postBinaryLedgerInput(ledgerInputRequest, binaryPath);
        log.debug("binaryLedgerInput() res={}", res);
        if (checkResponse(res)) {
          break;
        }
      } catch (Exception e) {
        // ...
      }
    }
    
    if (res == null) {
      String errMsg = "response is null";
      log.error("binaryLedgerInput() fail, {}", errMsg);
      throw new NullPointerException(errMsg);
    }
    
    try {
      callback.obtainBinaryLedgerInputResponse(locator, cmdJson, res);
    } catch (Exception e) {
      // ...
    }
    // ...
    
    handleReceipt(res);
    handleDoneClearanceOrderList(res);
    return res;
  }
  ```

- For the code of `obtainBinaryLedgerInputResponse` callback method, please refer to [CallbackSample.java](../src/main/java/com/itrustmachines/sample/CallbackSample.java)

  ```java
  public void obtainBinaryLedgerInputResponse(ReceiptLocator locator, String cmdJson,
      BinaryLedgerInputResponse ledgerInputResponse) {
  }
  ```

#### obtainReceiptEvent

**The `receipt` is contained in `ledgerInputResponse` / `binaryLedgerInputResponse`. Developers can implement the code in `obtainReceiptEvent` method to callback the informations in `receipt`.**

- After calling `obtainLedgerInputResponse` / `obtainBinaryLedgerInputResponse`, `ledgerInput` / `binaryLedgerInput` method will call `handleReceipt` method to extract the `receipt` from `ledgerInputResponse` / `binaryLedgerInputResponse` and call `handleReceiptEvent` method to handle the receipt

- For the code of `handleReceipt` method, please refer to [LedgerInputService.java](../../spo-client/src/main/java/com/itrustmachines/client/input/service/LedgerInputService.java)
  
  ```java
  public LedgerInputResponse ledgerInput(final @NonNull String indexValueKey, final @NonNull String cmdJson) {
    log.debug("ledgerInput() begin, indexValueKey={}, cmdJson={}", indexValueKey, cmdJson);
    
    // do ledger input, resend if necessary
    ReceiptLocator locator = null;
    LedgerInputRequest ledgerInputRequest;
    LedgerInputResponse res = null;
    // ...
    handleReceipt(res);
    handleDoneClearanceOrderList(res);
    return res;
  }
  
  void handleReceipt(LedgerInputResponse res) {
    // TODO should verify receipt signature first before saving
    final ReceiptEvent event = ReceiptEvent.builder()
                                           .source(ReceiptEvent.Source.LEDGER_INPUT_RESULT)
                                           .receipt(res.getReceipt())
                                           .build();
    log.debug("handleReceipt() event={}", event);
    receiptEventProcessor.handleReceiptEvent(event);
  }
  ```

- For the code of `handleReceiptEvent` method, please refer to [ReceiptEventProcessor.java](../../spo-client/src/main/java/com/itrustmachines/client/service/ReceiptEventProcessor.java)
  
  ```java
  public void handleReceiptEvent(final @NonNull ReceiptEvent event) {
    log.debug("handleReceiptEvent() begin, event={}", event);
    try {
      callback.obtainReceiptEvent(event);
    } catch (Exception e) {
      if (Thread.currentThread()
                .isInterrupted()) {
        throw e;
      }
      log.error("handleReceiptEvent() callback obtainReceiptEvent error, event={}", event, e);
    }
    final boolean isReceiptSaved = receiptService.save(event.getReceipt());
    log.debug("handleReceiptEvent() end, isReceiptSaved={}", isReceiptSaved);
  }
  ```

- For the code of `obtainReceiptEvent` callback method, please refer to [CallbackSample.java](../src/main/java/com/itrustmachines/sample/CallbackSample.java)

  ```java
  public void obtainReceiptEvent(ReceiptEvent receiptEvent) {
  }
  ```

- In order to search data conveniently, we recommand that you can callback the `indexValue` and `clearanceOrder`

#### obtainDoneClearanceOrderEvent

**The `doneClearanceOrder` is contained in `ledgerInputResponse` / `binaryLedgerInputResponse`. SPO Client will use `doneClearanceOrder` to find out which receipts need to be verified. Developers can implement the code in `obtainDoneClearanceOrderEvent` method to callback the informations in `doneClearanceOrder`.**

- After calling `obtainLedgerInputResponse` / `obtainBinaryLedgerInputResponse`, `ledgerInput` / `binaryLedgerInput` method will call `handleDoneClearanceOrderList` method to extract the `doneClearanceOrder` from `ledgerInputResponse` / `binaryLedgerInputResponse` and call `process` method to process the `doneClearanceOrder`

- For the code of `handleDoneClearanceOrderList` method, please refer to [LedgerInputService.java](../../spo-client/src/main/java/com/itrustmachines/client/input/service/LedgerInputService.java)
  
  ```java
  public LedgerInputResponse ledgerInput(final @NonNull String indexValueKey, final @NonNull String cmdJson) {
    log.debug("ledgerInput() begin, indexValueKey={}, cmdJson={}", indexValueKey, cmdJson);
    
    // do ledger input, resend if necessary
    ReceiptLocator locator = null;
    LedgerInputRequest ledgerInputRequest;
    LedgerInputResponse res = null;
    // ...
    handleReceipt(res);
    handleDoneClearanceOrderList(res);
    return res;
  }
  
  void handleDoneClearanceOrderList(LedgerInputResponse res) {
    if (Objects.nonNull(res.getDoneClearanceOrderList()) && res.getDoneClearanceOrderList()
                                                               .size() > 0) {
      final Long doneCO = res.getDoneClearanceOrderList()
                             .get(0);
      log.debug("handleDoneClearanceOrderList() response doneCO={}", doneCO);
      DoneClearanceOrderEvent event = DoneClearanceOrderEvent.builder()
                                                             .source(DoneClearanceOrderEvent.Source.LEDGER_INPUT_RESULT)
                                                             .doneClearanceOrder(doneCO)
                                                             .build();
      log.debug("handleDoneClearanceOrderList() event={}", event);
      doneClearanceOrderEventProcessor.process(event);
    }
  }
  ```

- For the code of `process` method, please refer to [DoneClearanceOrderEventProcessor.java](../../spo-client/src/main/java/com/itrustmachines/client/verify/service/DoneClearanceOrderEventProcessor.java)
  
  ```java
  public void process(@NonNull final DoneClearanceOrderEvent event) {
    log.debug("process() start, event={}", event);
    try {
      callback.obtainDoneClearanceOrderEvent(event);
    } catch (Exception e) {
      if (Thread.currentThread()
                .isInterrupted()) {
        throw e;
      }
      log.error("process() callback obtainDoneClearanceOrderEvent error, event={}", event, e);
    }
    
    if (doneClearanceOrder < event.getDoneClearanceOrder()) {
      doneClearanceOrder = event.getDoneClearanceOrder();
    }
  }
  ```

- For the code of `obtainDoneClearanceOrderEvent` callback method, please refer to [CallbackSample.java](../src/main/java/com/itrustmachines/sample/CallbackSample.java)
  
  ```java
  public void obtainDoneClearanceOrderEvent(DoneClearanceOrderEvent doneClearanceOrderEvent) {
  }
  ```

#### obtainMerkleProof

**Before verifying the receipt, SPO Client will request the `merkleProof` of the receipts to be verified from the Server. The Merkle Proof is evidence of receipt verification. SPO Client will use Merkle proof to verify the receipt whether receipt is in the TP-merkle tree. Developers can implement the code in `obtainMerkleProof` method to callback the informations in `merkleProof`.**

- Because of executor service, SPO Client will run `donceClearanceOrder` and `verifyReceipt` in asynchronous mode

- `verifyReceipt` method will call `obtainMerkleProof` method request the merkle proof from SPO Server

- `obtainMerkleProof` call `getMerkleProofResponse` to GET the response from SPO server via API

- After receiving the response from SPO Server, `obtainMerkleProof` will call `checkResponse` to examine the response. If there is no problem, then `obtainMerkleProof` will call `obtainMerkleProof` method to callback the informations in `merkleProof`
  
- For further API informations, please refer to [SPO Server API](https://azure-prod-rinkeby.itm.monster:4430/swagger-ui/)

- For API URL setting, please refer to [MerkleProofService.java](../../spo-client/src/main/java/com/itrustmachines/client/verify/service/MerkleProofService.java)
  
  ```java
  private static final String API_PATH = "/ledger/verify/merkleProof/";
  ```

- For the code of `verifyReceipt`, please refer to [DoneClearanceOrderEventProcessor.java](../../spo-client/src/main/java/com/itrustmachines/client/verify/service/DoneClearanceOrderEventProcessor.java)
  
  ```java
  private void verifyReceipts() {
    log.debug("verifyReceipts() start");
    while (!isCloseCalled && !Thread.currentThread()
                                    .isInterrupted()) {
      try {
        final Map<Long, Set<String>> needVerifyReceiptLocatorMap = receiptService.getNeedVerifyReceiptLocatorMap(
            doneClearanceOrder);
        
        int delayCount = 0;
        for (long co : needVerifyReceiptLocatorMap.keySet()) {
          Set<String> indexValues = needVerifyReceiptLocatorMap.get(co);
          for (String iv : indexValues) {
            final ReceiptLocator locator = ReceiptLocator.builder()
                                                         .clearanceOrder(co)
                                                         .indexValue(iv)
                                                         .build();
            log.debug("process() locator={}", locator);
            Receipt receipt = receiptService.findByLocator(locator);
            
            MerkleProof merkleProof = null;
            VerifyReceiptAndMerkleProofResult verifyResult;
            try {
              merkleProof = merkleProofService.obtainMerkleProof(locator);
              final ClearanceRecord clearanceRecord = contractService.obtainClearanceRecord(
                  merkleProof.getClearanceOrder());
              verifyResult = verifyService.verify(receipt, merkleProof, serverWalletAddress, clearanceRecord);
            } catch (Exception e) {
              // ...
            }
            // ...
          }
        }
      } catch (final Exception e) {
        // ...
      }
      TimeUnit.SECONDS.sleep(3L);
    }
    log.debug("verifyReceipts() end");
  }
  ```

- For the code of `obtainMerkleProof`, please refer to [MerkleProofService.java](../../spo-client/src/main/java/com/itrustmachines/client/verify/service/MerkleProofService.java)
  
  ```java
  public MerkleProof obtainMerkleProof(@NonNull final ReceiptLocator receiptLocator) {
    log.debug("obtainMerkleProof() begin, receiptLocator={}", receiptLocator);
    MerkleProofResponse res = null;
    for (int retryCount = 0; retryCount <= MAX_RETRY_TIMES; retryCount++) {
      log.debug("obtainMerkleProof() retryCount={}", retryCount);
      
      try {
        res = getMerkleProofResponse(receiptLocator);
        log.debug("obtainMerkleProof() res={}", res);
        if (checkResponse(res)) {
          break;
        }
      } catch (Exception e) {
        // ...
      }
    }
    
    if (res == null) {
      log.warn("obtainMerkleProof() response is null");
      try {
        callback.obtainMerkleProof(receiptLocator, null);
      } catch (Exception e) {
        // ...
      }
      return null;
    }
    
    final MerkleProof result = res.getMerkleProof();
    try {
      callback.obtainMerkleProof(receiptLocator, result);
    } catch (Exception e) {
      // ...
    }
    log.debug("obtainMerkleProof() result={}", result);
    return result;
  }
  ```

#### getVerifyReceiptResult

**After receiving the Merkle Proof. SPO Client will start to verify the receipt and store the result to `verifyReceiptResult`. Developers can implement the code in `getVerifyReceiptResult` method to callback the informations in `verifyReceiptResult`.**

- `verifyReceipts` method call `verify` method to verify the receipts. `verify` method will call `verifyReceiptSignature`, `verifyMerkleProofSignature`, `verifyClearanceOrder`, `verifyPbPair`, `verifyMerkleProofSlice`, `verifyRootHash` to verify the receipt and store verification result in `verifyResult`

- After verifying the receipt, `verifyReceipts` will call `getVerifyReceiptResult` method to callback the informations in `getVerifyReceiptResult`

- For the code of `verifyReceipts` method, please refer to [DoneClearanceOrderEventProcessor.java](../../spo-client/src/main/java/com/itrustmachines/client/verify/service/DoneClearanceOrderEventProcessor.java)
  
  ```java
  private void verifyReceipts() {
    log.debug("verifyReceipts() start");
    while (!isCloseCalled && !Thread.currentThread()
                                    .isInterrupted()) {
      try {
        final Map<Long, Set<String>> needVerifyReceiptLocatorMap = receiptService.getNeedVerifyReceiptLocatorMap(
            doneClearanceOrder);
        
        int delayCount = 0;
        for (long co : needVerifyReceiptLocatorMap.keySet()) {
          Set<String> indexValues = needVerifyReceiptLocatorMap.get(co);
          for (String iv : indexValues) {
            // ...
            Receipt receipt = receiptService.findByLocator(locator);
            
            MerkleProof merkleProof = null;
            VerifyReceiptAndMerkleProofResult verifyResult;
            try {
              merkleProof = merkleProofService.obtainMerkleProof(locator);
              final ClearanceRecord clearanceRecord = contractService.obtainClearanceRecord(
                  merkleProof.getClearanceOrder());
              verifyResult = verifyService.verify(receipt, merkleProof, serverWalletAddress, clearanceRecord);
            } catch (Exception e) {
              // ...
            }
            
            try {
              callback.getVerifyReceiptResult(receipt, merkleProof, verifyResult);
            } catch (Exception e) {
              // ...
            }
            receiptService.delete(receipt);
            
            if (++delayCount >= verifyBatchSize) {
              TimeUnit.SECONDS.sleep(verifyDelaySec);
              delayCount = 0;
            }
          }
        }
      } catch (final Exception e) {
        // ...
      }
      TimeUnit.SECONDS.sleep(3L);
    }
    log.debug("verifyReceipts() end");
  }
  ```

- For the code of `verify` method, please refer to [VerifyReceiptAndMerkleProofService.java](../../spo-common-verification/src/main/java/com/itrustmachines/verification/service/VerifyReceiptAndMerkleProofService.java)
  
  ```java
  public VerifyReceiptAndMerkleProofResult verify(@NonNull final Receipt receipt,
      @NonNull final MerkleProof merkleProof, @NonNull final String serverWalletAddress,
      final ClearanceRecord clearanceRecord) {
    log.debug("verify() start, receipt={}, merkleProof={}, clearanceRecord={}", receipt, merkleProof, clearanceRecord);
    
    boolean isMerkleProofSignatureOk;
    boolean isReceiptSignatureOk;
    boolean isClearanceOrderCorrect;
    boolean isPbPairOk;
    boolean isSliceOk;
    boolean isRootHashCorrect;
    final String rootHash = SliceValidationUtil.getRootHashString(merkleProof.getSlice());
    
    final long timestamp = System.currentTimeMillis();
    
    final VerifyReceiptAndMerkleProofResult result = VerifyReceiptAndMerkleProofResult.builder()
                                                                                      // ...
                                                                                      .build();
    log.debug("verify() initiate verify result={}", result);
    
    // verify receipt signature
    isReceiptSignatureOk = verifyReceiptSignature(receipt, serverWalletAddress);
    
    if (!isReceiptSignatureOk) {
      log.debug("verify() result={}", result);
      return result;
    } else {
      result.setReceiptSignatureOk(true);
    }
    
    // verify merkleProof signature
    isMerkleProofSignatureOk = verifyMerkleProofSignature(merkleProof, serverWalletAddress);
    if (!isMerkleProofSignatureOk) {
      log.debug("verify() result={}", result);
      return result;
    } else {
      result.setMerkleproofSignatureOk(true);
    }
    
    // verify clearanceOrder
    isClearanceOrderCorrect = verifyClearanceOrder(receipt.getClearanceOrder(), merkleProof.getClearanceOrder(),
        clearanceRecord.getClearanceOrder());
    if (!isClearanceOrderCorrect) {
      log.debug("verify() result={}", result);
      return result;
    } else {
      result.setClearanceOrderOk(true);
    }
    
    // verify PbPair
    isPbPairOk = verifyPbPair(receipt, merkleProof);
    if (!isPbPairOk) {
      log.debug("verify() result={}", result);
      return result;
    } else {
      result.setPbPairOk(true);
    }
    
    // verify slice
    isSliceOk = verifyMerkleProofSlice(merkleProof);
    if (!isSliceOk) {
      log.debug("verify() result={}", result);
      return result;
    } else {
      result.setSliceOk(true);
    }
    
    // verify if clearanceRecord rootHash and merkle proof slice rootHash match
    isRootHashCorrect = verifyRootHash(merkleProof, clearanceRecord);
    if (!isRootHashCorrect) {
      log.debug("verify() result={}", result);
      return result;
    } else {
      result.setClearanceRecordRootHashOk(true);
    }
    
    // overall result
    result.setPass(true);
    result.setStatus(StatusConstantsString.OK);
    result.setProofExistStatus(ProofExistStatus.PASS);
    result.setDescription(StatusConstantsString.OK);
    log.debug("verify() result={}", result);
    return result;
  }
  ```

- For the code of `getVerifyReceiptResult` method, please refer to [CallbackSample.java](../src/main/java/com/itrustmachines/sample/CallbackSample.java)
  
  ```java
  public void getVerifyReceiptResult(Receipt receipt, MerkleProof merkleProof,
      VerifyReceiptAndMerkleProofResult verifyReceiptAndMerkleProofResult) {

    final DashboardService.VerifyReceiptResultResponse response = dashboardService.postVerifyReceiptAndMerkleProofResult(
        verifyReceiptAndMerkleProofResult);
    log.info("getVerifyReceiptResult() dashboard response={}", response);
  }
  ```

----
Build the Callback Applications document is now complete. Next, learn how to build the ReceiptDao Applications

## Next Steps

Next Page : [Build the ReceiptDao Applications](./receiptDao_en.md)
Last Page : [Build the CMD](./cmd_en.md)