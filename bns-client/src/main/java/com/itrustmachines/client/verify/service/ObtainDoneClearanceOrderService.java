package com.itrustmachines.client.verify.service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.itrustmachines.client.vo.DoneClearanceOrderRequest;
import com.itrustmachines.client.vo.ReceiptLocatorRequest;
import com.itrustmachines.common.util.OkHttpClientUtil;
import com.itrustmachines.common.util.UrlUtil;

import com.itrustmachines.common.vo.KeyInfo;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

@ToString
@Slf4j
public class ObtainDoneClearanceOrderService {
  
  private static final String API_PATH = "/ledger/doneClearanceOrder";
  private static final int MAX_RETRY_TIMES = 5;
  
  private final String apiUrl;
  private final int retryDelaySec;
  
  private final OkHttpClient okHttpClient;
  private final Gson gson;

  private final MediaType JSON = MediaType.get("application/json; charset=utf-8");
  
  public ObtainDoneClearanceOrderService(String bnsServerUrl, int retryDelaySec) {
    this.apiUrl = UrlUtil.urlWithoutSlash(bnsServerUrl) + API_PATH;
    this.retryDelaySec = retryDelaySec;
    
    this.okHttpClient = OkHttpClientUtil.getOkHttpClient();
    this.gson = new Gson();
    log.info("new instance={}", this);
  }
  
  @SneakyThrows
  public long postDoneClearanceOrder( @NonNull KeyInfo keyInfo) {
    log.debug("obtainDoneClearanceOrder() begin");
    long doneClearanceOrder = -1;
    DoneClearanceOrderRequest doneClearanceOrderRequest = buildDoneClearanceOrderRequest( keyInfo );
    for (int retryCount = 0; retryCount <= MAX_RETRY_TIMES; retryCount++) {
      try {
        doneClearanceOrder = getDoneClearanceOrder( doneClearanceOrderRequest );
        log.debug("obtainDoneClearanceOrder() doneClearanceOrder={}", doneClearanceOrder);
        if (doneClearanceOrder >= 0) {
          break;
        }
      } catch (Exception e) {
        if (Thread.currentThread()
                  .isInterrupted()) {
          throw e;
        }
        log.debug("obtainDoneClearanceOrder() retryCount={}", retryCount);
        if (retryCount == MAX_RETRY_TIMES) {
          log.error("obtainDoneClearanceOrder() fail", e);
          throw e;
        }
        TimeUnit.SECONDS.sleep(retryDelaySec);
      }
    }
    log.debug("obtainDoneClearanceOrder() end, doneClearanceOrder={}", doneClearanceOrder);
    return doneClearanceOrder;
  }
  
  @SneakyThrows
  private long getDoneClearanceOrder( DoneClearanceOrderRequest doneClearanceOrderRequest ) {

    final Request request = new Request.Builder().url(apiUrl)
            .post(RequestBody.create(gson.toJson(doneClearanceOrderRequest), JSON))
            .build();

    try (final Response res = okHttpClient.newCall(request)
                                          .execute()) {
      log.debug("getDoneClearanceOrder() res={}", res);
      final long doneClearanceOrder = Long.parseLong(Objects.requireNonNull(res.body())
                                                            .string());
      log.debug("getDoneClearanceOrder() doneClearanceOrder={}", doneClearanceOrder);
      return doneClearanceOrder;
    }
  }

  public DoneClearanceOrderRequest buildDoneClearanceOrderRequest(final @NonNull KeyInfo keyInfo ) {
    log.debug("buildDoneClearanceOrderRequest() begin, keyInfo={}", keyInfo);

    final String toSignMessage = "doneClearanceOrder";
    final DoneClearanceOrderRequest doneClearanceOrderRequest = DoneClearanceOrderRequest.builder()
            .address(keyInfo.getAddress())
            .toSignMessage(toSignMessage)
            .build()
            .sign(keyInfo.getPrivateKey());

    log.debug("buildDoneClearanceOrderRequest() end, doneClearanceOrderRequest={}", doneClearanceOrderRequest);
    return doneClearanceOrderRequest;
  }
}
