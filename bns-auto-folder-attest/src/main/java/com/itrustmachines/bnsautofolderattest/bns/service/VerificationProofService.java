package com.itrustmachines.bnsautofolderattest.bns.service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.itrustmachines.bnsautofolderattest.vo.AttestationRecord;
import com.itrustmachines.bnsautofolderattest.vo.VerificationProofResponse;
import com.itrustmachines.client.config.BnsClientConfig;
import com.itrustmachines.client.util.OkHttpClientUtil;
import com.itrustmachines.common.util.UrlUtil;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@ToString(exclude = { "gson" })
@Slf4j
public class VerificationProofService {
  
  private final BnsClientConfig bnsClientConfig;
  private static final String VERIFICATION_PROOF_DOWNLOAD_PATH = "/verify/verificationProof/{clearanceOrder}/{indexValue}";
  private final String verificationProofDownloadUrl;
  
  private final int MAX_RETRY_TIMES = 5;
  private final int retryDelaySec;
  
  private final OkHttpClient okHttpClient;
  private final Gson gson;
  
  private final MediaType JSON = MediaType.get("application/json; charset=utf-8");
  
  public VerificationProofService(@NonNull final BnsClientConfig bnsClientConfig) {
    this.bnsClientConfig = bnsClientConfig;
    this.verificationProofDownloadUrl = UrlUtil.urlWithoutSlash(bnsClientConfig.getBnsServerUrl())
        + VERIFICATION_PROOF_DOWNLOAD_PATH;
    this.retryDelaySec = bnsClientConfig.getRetryDelaySec();
    this.okHttpClient = OkHttpClientUtil.getOkHttpClient();
    this.gson = new Gson();
    log.info("new instance={}", this);
  }
  
  @SneakyThrows
  private VerificationProofResponse sendGetProofRequest(@NonNull final Long clearanceOrder,
      @NonNull final String indexValue) {
    log.debug("sendGetProofRequest() start, clearanceOrder={}, indexValue={}", clearanceOrder, indexValue);
    final Request request = new Request.Builder().url(
        verificationProofDownloadUrl.replace("{clearanceOrder}", "" + clearanceOrder)
                                    .replace("{indexValue}", indexValue))
                                                 .get()
                                                 .build();
    
    try (final Response res = okHttpClient.newCall(request)
                                          .execute()) {
      log.debug("sendGetProofRequest() res={}", res);
      final VerificationProofResponse verificationProofResponse = gson.fromJson(Objects.requireNonNull(res.body())
                                                                                       .string(),
          VerificationProofResponse.class);
      log.debug("sendGetProofRequest() verificationProofResponse={}", verificationProofResponse);
      return verificationProofResponse;
    }
  }
  
  @SneakyThrows
  public VerificationProofResponse getVerificationProof(@NonNull final AttestationRecord attestationRecord) {
    log.debug("getVerificationProof() start, attestationRecord={}", attestationRecord);
    VerificationProofResponse verificationProofResponse = null;
    for (int retryCount = 0; retryCount <= MAX_RETRY_TIMES; retryCount++) {
      try {
        verificationProofResponse = sendGetProofRequest(attestationRecord.getClearanceOrder(),
            attestationRecord.getIndexValue());
      } catch (Exception e) {
        if (Thread.currentThread()
                  .isInterrupted()) {
          throw e;
        }
        log.debug("getVerificationProof() retryCount={}", retryCount);
        if (retryCount == MAX_RETRY_TIMES) {
          log.error("getVerificationProof() fail", e);
          throw e;
        }
        TimeUnit.SECONDS.sleep(retryDelaySec);
      }
    }
    log.debug("getVerificationProof() end, verificationProofResponse={}", verificationProofResponse);
    return verificationProofResponse;
  }
  
}
