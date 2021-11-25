package com.itrustmachines.client.register.service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.itrustmachines.client.register.vo.BnsServerInfoRequest;
import com.itrustmachines.client.register.vo.RegisterRequest;
import com.itrustmachines.client.vo.BnsServerInfo;
import com.itrustmachines.common.constants.StatusConstants;
import com.itrustmachines.common.util.OkHttpClientUtil;
import com.itrustmachines.common.util.UrlUtil;

import com.itrustmachines.common.vo.KeyInfo;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

@ToString
@Slf4j
public class BnsServerInfoService {

  private static final String API_PATH = "/ledger/serverInfo";
  private static final int MAX_RETRY_TIMES = 5;
  private final MediaType JSON = MediaType.get("application/json; charset=utf-8");
  private final String apiUrl;
  private final OkHttpClient okHttpClient;
  private final Gson gson;
  private final int retryDelaySec;

  public BnsServerInfoService(final @NonNull String bnsServerUrl, int retryDelaySec) {
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
  public static class BnsServerInfoResponse {

    String serverWalletAddress;
    String contractAddress;
  }

  @SneakyThrows
  public BnsServerInfo postBnsServerInfo( KeyInfo keyInfo ) {
    log.debug("obtainBnsServerInfo() start");

    BnsServerInfoRequest bnsServerInfoRequest = buildBnsServerInfoRequest( keyInfo );
    BnsServerInfoResponse res = null;

    for (int retryCount = 0; retryCount <= MAX_RETRY_TIMES; retryCount++) {
      log.debug("obtainBnsServerInfo() retryCount={}", retryCount);
      try {
        res = getBnsServerInfo( bnsServerInfoRequest );
        if (checkResponse(res)) {
          break;
        }
      } catch (Exception e) {
        if (Thread.currentThread()
                .isInterrupted()) {
          throw e;
        }
        if (retryCount == MAX_RETRY_TIMES) {
          log.error("obtainBnsServerInfo() fail", e);
          throw e;
        }
        TimeUnit.SECONDS.sleep(retryDelaySec);
      }
    }

    if (res == null) {
      String errMsg = "response is null";
      log.error("obtainSpoServerInfo() fail, {}", errMsg);
      throw new NullPointerException(errMsg);
    }

    final BnsServerInfo result = BnsServerInfo.builder()
            .serverWalletAddress(res.getServerWalletAddress())
            .contractAddress(res.getContractAddress())
            .build();
    log.debug("obtainBnsServerInfo() result={}", result);
    return result;
  }

  @SneakyThrows
  private BnsServerInfoResponse getBnsServerInfo( BnsServerInfoRequest bnsServerInfoRequest) {

    log.debug("getBnsServerInfo() start bnsServerInfoRequest={}", bnsServerInfoRequest);
    final Request request = new Request.Builder().url(apiUrl)
            .post(RequestBody.create(gson.toJson(bnsServerInfoRequest), JSON))
            .build();

    BnsServerInfoResponse res = null;
    try (final Response response = okHttpClient.newCall(request)
            .execute()) {
      log.debug("getBnsServerInfo() response={}", response);
      final String resString = Objects.requireNonNull(response.body()).string();
      log.debug("getBnsServerInfo() resString={}", resString);
      res = gson.fromJson(resString, BnsServerInfoResponse.class);
      log.debug("postRegister() res={}", res);
      return res;
    }
  }

  boolean checkResponse(final BnsServerInfoResponse bnsServerInfoRes) {

    log.debug("checkResponse() bnsServerInfoRes={}", bnsServerInfoRes);
    if (bnsServerInfoRes == null) {
      log.warn("checkResponse() result=false, bnsServerInfoRes is null");
      return false;
    }

    if (bnsServerInfoRes.serverWalletAddress == null ){
      log.warn("checkResponse() result=false, bnsServerInfoRes is null");
      return false;
    }

    if (bnsServerInfoRes.contractAddress == null){
      log.warn("checkResponse() result=false, bnsServerInfoRes is null");
      return false;
    }

    log.debug("checkResponse() result=true, bnsServerInfoRes status is ok");
    return true;
  }

  public BnsServerInfoRequest buildBnsServerInfoRequest(final @NonNull KeyInfo keyInfo ) {
    log.debug("buildRegisterRequest() begin, keyInfo={}", keyInfo);

    final String toSignMessage = "serverInfo";
    final BnsServerInfoRequest bnsServerInfoRequest = BnsServerInfoRequest.builder()
            .address(keyInfo.getAddress())
            .toSignMessage(toSignMessage)
            .build()
            .sign(keyInfo.getPrivateKey());

    log.debug("buildBnsServerInfoRequest() end, bnsServerInfoRequest={}", bnsServerInfoRequest);
    return bnsServerInfoRequest;
  }
}
