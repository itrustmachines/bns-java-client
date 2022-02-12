package com.itrustmachines.client.verify.service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.itrustmachines.client.util.OkHttpClientUtil;
import com.itrustmachines.common.util.UrlUtil;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@ToString
@Slf4j
public class ObtainDoneClearanceOrderService {
  
  private static final String API_PATH = "/doneClearanceOrder";
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
  public long postDoneClearanceOrder() {
    log.debug("obtainDoneClearanceOrder() begin");
    long doneClearanceOrder = -1;
    for (int retryCount = 0; retryCount <= MAX_RETRY_TIMES; retryCount++) {
      try {
        doneClearanceOrder = getDoneClearanceOrder();
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
  private long getDoneClearanceOrder() {
    
    final Request request = new Request.Builder().url(apiUrl)
                                                 .get()
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
  
}
