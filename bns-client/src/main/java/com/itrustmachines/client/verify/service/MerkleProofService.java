package com.itrustmachines.client.verify.service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.itrustmachines.client.todo.BnsClientCallback;
import com.itrustmachines.client.util.OkHttpClientUtil;
import com.itrustmachines.common.util.UrlUtil;
import com.itrustmachines.common.vo.MerkleProof;
import com.itrustmachines.common.vo.ReceiptLocator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@ToString
@Slf4j
public class MerkleProofService {
  
  private static final String API_PATH = "/verify/merkleProof/{clearanceOrder}/{indexValueReq}";
  private static final int MAX_RETRY_TIMES = 5;
  
  private final MediaType JSON = MediaType.get("application/json; charset=utf-8");
  
  private final String apiUrl;
  private final BnsClientCallback callback;
  private final int retryDelaySec;
  
  private final Gson gson;
  private final OkHttpClient okHttpClient;
  
  public MerkleProofService(@NonNull final String bnsServerUrl, @NonNull final BnsClientCallback callback,
      final int retryDelaySec) {
    this.apiUrl = UrlUtil.urlWithoutSlash(bnsServerUrl) + API_PATH;
    this.callback = callback;
    this.retryDelaySec = retryDelaySec;
    
    this.gson = new Gson();
    this.okHttpClient = OkHttpClientUtil.getOkHttpClient();
    log.info("new instance={}", this);
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class MerkleProofResponse {
    
    String status;
    String description;
    MerkleProof merkleProof;
    
  }
  
  @SneakyThrows
  public MerkleProof postMerkleProof(@NonNull final ReceiptLocator receiptLocator) {
    
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
        if (Thread.currentThread()
                  .isInterrupted()) {
          throw e;
        }
        if (retryCount == MAX_RETRY_TIMES) {
          log.error("obtainMerkleProof() fail", e);
          throw e;
        }
        TimeUnit.SECONDS.sleep(retryDelaySec);
      }
    }
    
    if (res == null) {
      log.warn("obtainMerkleProof() response is null");
      try {
        callback.obtainMerkleProof(receiptLocator, null);
      } catch (Exception e) {
        if (Thread.currentThread()
                  .isInterrupted()) {
          throw e;
        }
        log.error("obtainMerkleProof() callback obtainMerkleProof error, receiptLocator={}, merkleProof=null",
            receiptLocator, e);
      }
      return null;
    }
    
    final MerkleProof result = res.getMerkleProof();
    try {
      callback.obtainMerkleProof(receiptLocator, result);
    } catch (Exception e) {
      if (Thread.currentThread()
                .isInterrupted()) {
        throw e;
      }
      log.error("obtainMerkleProof() callback obtainMerkleProof error, receiptLocator={}, merkleProof={}",
          receiptLocator, result);
    }
    log.debug("obtainMerkleProof() result={}", result);
    return result;
  }
  
  @SneakyThrows
  MerkleProofResponse getMerkleProofResponse(@NonNull final ReceiptLocator receiptLocator) {
    
    log.debug("getMerkleProofResponse() requestUrl={}", apiUrl);
    
    final Request request = new Request.Builder().url(
        apiUrl.replace("{clearanceOrder}", "" + receiptLocator.getClearanceOrder())
              .replace("{indexValueReq}", receiptLocator.getIndexValue()))
                                                 .get()
                                                 .build();
    
    try (final Response res = okHttpClient.newCall(request)
                                          .execute()) {
      log.debug("getMerkleProofResponse() res={}", res);
      final String resString = Objects.requireNonNull(res.body())
                                      .string();
      final MerkleProofResponse merkleProofResponse = gson.fromJson(resString, MerkleProofResponse.class);
      log.debug("getMerkleProofResponse() merkleProofResponse={}", merkleProofResponse);
      return merkleProofResponse;
    }
  }
  
  boolean checkResponse(final MerkleProofResponse merkleProofRes) {
    log.debug("checkResponse() merkleProofRes={}", merkleProofRes);
    if (merkleProofRes == null) {
      log.warn("checkResponse() result=false, merkleProofRes is null");
      return false;
    }
    log.debug("checkResponse() result=true, merkleProofRes is not null");
    return true;
  }
  
}
