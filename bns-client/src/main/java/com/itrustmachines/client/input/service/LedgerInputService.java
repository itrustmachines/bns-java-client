package com.itrustmachines.client.input.service;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.itrustmachines.client.input.vo.LedgerInputRequest;
import com.itrustmachines.client.input.vo.LedgerInputResponse;
import com.itrustmachines.client.input.vo.LedgerInputServiceParams;
import com.itrustmachines.client.service.ReceiptEventProcessor;
import com.itrustmachines.client.service.ReceiptLocatorService;
import com.itrustmachines.client.service.BnsClientReceiptService;
import com.itrustmachines.client.todo.BnsClientCallback;
import com.itrustmachines.client.verify.service.DoneClearanceOrderEventProcessor;
import com.itrustmachines.client.verify.vo.DoneClearanceOrderEvent;
import com.itrustmachines.client.vo.ReceiptEvent;
import com.itrustmachines.common.constants.StatusConstants;
import com.itrustmachines.common.util.OkHttpClientUtil;
import com.itrustmachines.common.util.UrlUtil;
import com.itrustmachines.common.vo.KeyInfo;
import com.itrustmachines.common.vo.ReceiptLocator;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

@ToString
@Slf4j
public class LedgerInputService {
  
  private static final String API_PATH = "/ledger/input";
  
  private static final int MAX_RESEND_TIMES = 5;
  
  private final KeyInfo keyInfo;
  private final BnsClientCallback callback;
  private final BnsClientReceiptService bnsClientReceiptService;
  private final ReceiptLocatorService receiptLocatorService;
  
  private final DoneClearanceOrderEventProcessor doneClearanceOrderEventProcessor;
  private final ReceiptEventProcessor receiptEventProcessor;
  
  private final String apiUrl;
  private final Gson gson;
  private final MediaType JSON = MediaType.get("application/json; charset=utf-8");
  private final OkHttpClient okHttpClient;
  private final int retryDelaySec;
  
  public LedgerInputService(@NonNull final LedgerInputServiceParams params) {
    this.keyInfo = params.getKeyInfo();
    this.apiUrl = UrlUtil.urlWithoutSlash(params.getBnsServerUrl()) + API_PATH;
    this.callback = params.getCallback();
    this.bnsClientReceiptService = params.getBnsClientReceiptService();
    this.receiptLocatorService = params.getReceiptLocatorService();
    this.doneClearanceOrderEventProcessor = params.getDoneClearanceOrderEventProcessor();
    this.receiptEventProcessor = params.getReceiptEventProcessor();
    this.retryDelaySec = params.getRetryDelaySec();
    
    this.okHttpClient = OkHttpClientUtil.getOkHttpClient();
    this.gson = new Gson();
    log.info("new instance={}", this);
  }
  
  @SneakyThrows
  public LedgerInputResponse ledgerInput(final @NonNull KeyInfo keyInfo, final @NonNull String cmdJson) {
    log.debug("ledgerInput() begin, indexValueKey={}, cmdJson={}", keyInfo.getAddress(), cmdJson);
    
    // do ledger input, resend if necessary
    ReceiptLocator locator = null;
    LedgerInputRequest ledgerInputRequest;
    LedgerInputResponse res = null;
    for (int resendCount = 0; resendCount <= MAX_RESEND_TIMES; resendCount++) {
      log.debug("ledgerInput() resendCount={}", resendCount);
      try {
        // build LedgerInput by cmdJson
        locator = receiptLocatorService.postReceiptLocator( keyInfo);
        ledgerInputRequest = buildLedgerInputRequest(locator, cmdJson);
        try {
          callback.createLedgerInputByCmd(locator, ledgerInputRequest);
        } catch (Exception e) {
          if (Thread.currentThread()
                    .isInterrupted()) {
            throw e;
          }
          log.error("ledgerInput() callback createLedgerInputByCmd error, locator={}, ledgerInputRequest={}", locator,
              ledgerInputRequest, e);
        }
        res = postLedgerInput(ledgerInputRequest);
        log.debug("ledgerInput() res={}", res);
        if (checkResponse(res)) {
          break;
        }
      } catch (Exception e) {
        if (Thread.currentThread()
                  .isInterrupted()) {
          throw e;
        }
        if (resendCount == MAX_RESEND_TIMES) {
          log.error("ledgerInput() fail", e);
          throw e;
        }
        TimeUnit.SECONDS.sleep(retryDelaySec);
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
      if (Thread.currentThread()
                .isInterrupted()) {
        throw e;
      }
      log.error("ledgerInput() callback obtainLedgerInputResponse error, locator={}, cmdJson={}, res={}", locator,
          cmdJson, res, e);
    }
    
    // if status not ok, don't handle receipt and doneCO
    if (!StatusConstants.OK.name()
                           .equalsIgnoreCase(res.getStatus())) {
      log.debug("ledgerInput() response status not ok, res={}", res);
      return res;
    }
    
    handleReceipt(res);
    handleDoneClearanceOrderList(res);
    return res;
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
  
  @SneakyThrows
  LedgerInputResponse postLedgerInput(final @NonNull LedgerInputRequest ledgerInputRequest) {
    log.debug("postLedgerInput() ledgerInputRequest={}", ledgerInputRequest);
    Request request = new Request.Builder().url(apiUrl)
                                           .post(RequestBody.create(gson.toJson(ledgerInputRequest), JSON))
                                           .build();
    LedgerInputResponse res = null;
    try (final Response response = okHttpClient.newCall(request)
                                               .execute()) {
      log.debug("postLedgerInput() response={}", response);
      final String resString = Objects.requireNonNull(response.body())
                                      .string();
      log.debug("postLedgerInput() resString={}", resString);
      res = gson.fromJson(resString, LedgerInputResponse.class);
      
      log.debug("postLedgerInput() res={}", res);
      return res;
    }
  }
  
  private boolean checkResponse(final LedgerInputResponse res) {
    log.debug("checkResponse() res={}", res);
    if (res == null) {
      log.warn("checkResponse() result=false, LedgerInputResponse is null");
      return false;
    }
    if (checkInputDescriptionNeedResend(res)) {
      log.warn("checkResponse() result=false, LedgerInputResponse description checked, need resend");
      return false;
    }
    log.debug("checkResponse() result=true, response ok");
    return true;
  }
  
  boolean checkInputDescriptionNeedResend(final @NonNull LedgerInputResponse res) {
    final String resDescription = res.getDescription();
    log.debug("checkInputDescriptionNeedResend() resDescription={}", resDescription);
    // if description equals any below, do not need to resend
    final String[] notNeedResendDes = { "OK", "CLIENT_SIGNATURE_ERROR", "AUTHENTICATION_ERROR", "CMD_ERROR",
        "TX_COUNT_ERROR" };
    boolean result = !Arrays.asList(notNeedResendDes)
                            .contains(resDescription.toUpperCase());
    log.debug("checkInputDescriptionNeedResend() result={}", result);
    return result;
  }
  
  // generate ReceiptEvent and handle by ReceiptEventProcessor
  void handleReceipt(LedgerInputResponse res) {
    // TODO should verify receipt signature first before saving
    final ReceiptEvent event = ReceiptEvent.builder()
                                           .source(ReceiptEvent.Source.LEDGER_INPUT_RESULT)
                                           .receipt(res.getReceipt())
                                           .build();
    log.debug("handleReceipt() event={}", event);
    receiptEventProcessor.handleReceiptEvent(event);
  }
  
  // generate DoneClearanceOrderEvent and process
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
  
}
