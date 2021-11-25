package com.itrustmachines.client.service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.itrustmachines.client.vo.ReceiptLocatorRequest;
import com.itrustmachines.common.constants.StatusConstants;
import com.itrustmachines.common.util.OkHttpClientUtil;
import com.itrustmachines.common.util.UrlUtil;
import com.itrustmachines.common.vo.KeyInfo;
import com.itrustmachines.common.vo.ReceiptLocator;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

@ToString
@Slf4j
public class ReceiptLocatorService {
  
  /** API: /ledger/clearanceOrderAndSn/{indexValueKey} */
  private static final String API_PATH = "/ledger/clearanceOrderAndSn";
  private static final int MAX_RETRY_TIMES = 5;
  
  private final String apiUrl;
  private final OkHttpClient okHttpClient;
  private final Gson gson;
  private final int retryDelaySec;

  private final MediaType JSON = MediaType.get("application/json; charset=utf-8");
  
  public ReceiptLocatorService(String bnsServerUrl, int retryDelaySec) {
    this.apiUrl = UrlUtil.urlWithoutSlash(bnsServerUrl) + API_PATH;
    this.okHttpClient = OkHttpClientUtil.getOkHttpClient();
    this.gson = new Gson();
    this.retryDelaySec = retryDelaySec;
    log.info("new instance={}", this);
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  static class ReceiptLocatorResponse {
    
    String status;
    String description;
    Long clearanceOrder;
    Long sn;
    
  }
  
  @SneakyThrows
  public ReceiptLocator postReceiptLocator( final @NonNull KeyInfo keyInfo ) {
    log.debug("obtainReceiptLocator() start");

    ReceiptLocatorRequest receiptLocatorRequest = buildReceiptLocatorRequest( keyInfo );
    ReceiptLocatorResponse res = null;
    for (int retryCount = 0; retryCount <= MAX_RETRY_TIMES; retryCount++) {
      log.debug("obtainReceiptLocator() retryCount={}", retryCount);
      try {

        res = getReceiptLocatorResponse( receiptLocatorRequest );
        if (checkResponse(res)) {
          break;
        }
      } catch (Exception e) {
        if (Thread.currentThread()
                  .isInterrupted()) {
          throw e;
        }
        if (retryCount == MAX_RETRY_TIMES) {
          log.error("obtainReceiptLocator() fail", e);
          throw e;
        }
        TimeUnit.SECONDS.sleep(retryDelaySec);
      }
    }
    
    if (res == null) {
      String errMsg = "response is null";
      log.error("obtainReceiptLocator() fail, {}", errMsg);
      throw new NullPointerException(errMsg);
    }
    
    final ReceiptLocator result = ReceiptLocator.builder()
                                                .clearanceOrder(res.getClearanceOrder())
                                                .indexValue(keyInfo.getAddress() + "_R" + res.getSn())
                                                .build();
    log.debug("obtainReceiptLocator() result={}", result);
    return result;
  }
  
  boolean checkResponse(ReceiptLocatorResponse receiptLocatorRes) {
    log.debug("checkResponse() receiptLocatorRes={}", receiptLocatorRes);
    if (receiptLocatorRes == null) {
      log.warn("checkResponse() result=false,receiptLocatorRes is null");
      return false;
    }
    if (!StatusConstants.OK.name()
                           .equalsIgnoreCase(receiptLocatorRes.getStatus())) {
      log.warn("checkResponse() result=false, receiptLocatorRes status not ok");
      return false;
    }
    log.debug("checkResponse() result=true, receiptLocatorRes status is ok");
    return true;
  }
  
  @SneakyThrows
  private ReceiptLocatorResponse getReceiptLocatorResponse(@NonNull ReceiptLocatorRequest receiptLocatorRequest) {

    log.debug("getReceiptLocatorResponse() requestUrl={}", apiUrl);

    final Request request = new Request.Builder().url(apiUrl)
            .post(RequestBody.create(gson.toJson(receiptLocatorRequest), JSON))
            .build();

    try (final Response response = okHttpClient.newCall(request)
                                               .execute()) {
      final String resString = Objects.requireNonNull(response.body())
                                      .string();
      final ReceiptLocatorResponse res = gson.fromJson(resString, ReceiptLocatorResponse.class);
      log.debug("getReceiptLocatorResponse() res={}", res);
      return res;
    }
  }

  public ReceiptLocatorRequest buildReceiptLocatorRequest(final @NonNull KeyInfo keyInfo ) {
    log.debug("buildReceiptLocatorRequest() begin, keyInfo={}", keyInfo);

    final String toSignMessage = "clearanceOrderAndSn";
    final ReceiptLocatorRequest receiptLocatorRequest = ReceiptLocatorRequest.builder()
            .address(keyInfo.getAddress())
            .indexValueKey(keyInfo.getAddress())
            .toSignMessage(toSignMessage)
            .build()
            .sign(keyInfo.getPrivateKey());

    log.debug("buildReceiptLocatorRequest() end, receiptLocatorRequest={}", receiptLocatorRequest);
    return receiptLocatorRequest;
  }
  
}
