package com.itrustmachines.client.register.service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.itrustmachines.client.register.vo.RegisterRequest;
import com.itrustmachines.client.todo.BnsClientCallback;
import com.itrustmachines.client.util.OkHttpClientUtil;
import com.itrustmachines.common.util.UrlUtil;
import com.itrustmachines.common.vo.KeyInfo;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
public class RegisterService {
  
  private static final String API_PATH = "/account/register";
  private static final String CHECK_REGISTER_PATH = "/account/register/check/";
  private static final int MAX_RETRY_TIMES = 5;
  
  private final BnsClientCallback bnsClientCallback;
  private final KeyInfo keyInfo;
  
  private final String apiUrl;
  private final String bnsServerUrl;
  private final String email;
  private final Gson gson;
  private final MediaType JSON = MediaType.get("application/json; charset=utf-8");
  private final OkHttpClient okHttpClient;
  private final int retryDelaySec;
  
  public RegisterService(String bnsServerUrl, BnsClientCallback bnsClientCallback, KeyInfo keyInfo, int retryDelaySec,
      String email) {
    this.bnsClientCallback = bnsClientCallback;
    this.keyInfo = keyInfo;
    this.bnsServerUrl = bnsServerUrl;
    this.email = email;
    this.apiUrl = UrlUtil.urlWithoutSlash(bnsServerUrl) + API_PATH;
    this.okHttpClient = OkHttpClientUtil.getOkHttpClient();
    this.gson = new Gson();
    this.retryDelaySec = retryDelaySec;
    log.info("new instance={}", this);
  }
  
  @SneakyThrows
  public boolean register() {
    final RegisterRequest registerReq = buildRegisterRequest(keyInfo, email);
    
    String res = null;
    for (int retryCount = 0; retryCount <= MAX_RETRY_TIMES; retryCount++) {
      log.debug("register() retryCount={}", retryCount);
      try {
        res = postRegister(registerReq);
        log.debug("register() res={}", res);
        if (checkResponse(res)) {
          break;
        }
      } catch (Exception e) {
        if (Thread.currentThread()
                  .isInterrupted()) {
          throw e;
        }
        if (retryCount == MAX_RETRY_TIMES) {
          log.error("register() fail", e);
          throw e;
        }
        TimeUnit.SECONDS.sleep(retryDelaySec);
      }
    }
    
    if (res == null) {
      String errMsg = "response is null";
      log.error("register() fail, {}", errMsg);
      throw new NullPointerException(errMsg);
    }
    
    try {
      bnsClientCallback.register(registerReq, true);
    } catch (Exception e) {
      if (Thread.currentThread()
                .isInterrupted()) {
        throw e;
      }
      log.error("register() callback register error, registerReq={}, res={}", registerReq, res, e);
    }
    
    final boolean result = checkRegisterResult(res);
    log.debug("register() result={}", result);
    return result;
  }
  
  @SneakyThrows
  public String postRegister(RegisterRequest registerReq) {
    log.debug("postRegister() begin, registerReq={}, apiUrl={}", registerReq, apiUrl);
    Request request = new Request.Builder().url(apiUrl)
                                           .post(RequestBody.create(gson.toJson(registerReq), JSON))
                                           .build();
    String res;
    try (final Response response = okHttpClient.newCall(request)
                                               .execute()) {
      log.debug("postRegister() response={}", response);
      final String resString = Objects.requireNonNull(response.body())
                                      .string();
      log.debug("postRegister() resString={}", resString);
      res = gson.fromJson(resString, String.class);
      log.debug("postRegister() res={}", res);
      return res;
    }
  }
  
  boolean checkResponse(String registerRes) {
    log.debug("checkResponse() registerRes={}", registerRes);
    if (registerRes == null) {
      log.warn("checkResponse() result= false, registerRes is null");
      return false;
    }
    
    log.debug("checkResponse() result=true, response ok");
    return true;
  }
  
  boolean checkRegisterResult(String res) {
    log.debug("checkRegisterResult() begin, res={}", res);
    boolean result = false;
    
    if (res.compareToIgnoreCase("true") == 0) {
      result = true;
    }
    
    log.debug("checkRegisterResult() end, result={}", result);
    return result;
  }
  
  private RegisterRequest buildRegisterRequest(final @NonNull KeyInfo keyInfo, final @NonNull String email) {
    log.debug("buildRegisterRequest() begin, keyInfo={}", keyInfo);
    
    final String toSignMessage = keyInfo.getAddress();
    final RegisterRequest registerReq = RegisterRequest.builder()
                                                       .address(keyInfo.getAddress())
                                                       .email(email)
                                                       .toSignMessage(toSignMessage)
                                                       .build()
                                                       .sign(keyInfo.getPrivateKey());
    
    log.debug("buildRegisterRequest() end, registerRequest={}", registerReq);
    return registerReq;
  }
  
  @SneakyThrows
  private Boolean getRegister(@NonNull final String callerAddress) {
    log.debug("getRegister() begin, callerAddress={}", callerAddress);
    final String url = UrlUtil.urlWithoutSlash(bnsServerUrl) + CHECK_REGISTER_PATH + callerAddress;
    
    Request request = new Request.Builder().url(url)
                                           .get()
                                           .build();
    Boolean res;
    try (final Response response = okHttpClient.newCall(request)
                                               .execute()) {
      log.debug("getRegister() response={}", response);
      final String resString = Objects.requireNonNull(response.body())
                                      .string();
      res = gson.fromJson(resString, Boolean.class);
      log.debug("getRegister() res={}", res);
      return res;
    }
  }
  
  @SneakyThrows
  public boolean checkRegister() {
    
    Boolean result = null;
    
    for (int retryCount = 0; retryCount <= MAX_RETRY_TIMES; retryCount++) {
      log.debug("checkRegister() retryCount={}", retryCount);
      try {
        result = getRegister(keyInfo.getAddress());
        log.debug("checkRegister() result={}", result);
        if (result != null) {
          break;
        }
      } catch (Exception e) {
        if (Thread.currentThread()
                  .isInterrupted()) {
          throw e;
        }
        if (retryCount == MAX_RETRY_TIMES) {
          log.error("checkRegister() fail", e);
          throw e;
        }
        TimeUnit.SECONDS.sleep(retryDelaySec);
      }
    }
    
    log.debug("checkRegister() result={}", result);
    return result;
  }
}
