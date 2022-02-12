package com.itrustmachines.client.login.service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.itrustmachines.client.login.vo.LoginRequest;
import com.itrustmachines.client.util.OkHttpClientUtil;
import com.itrustmachines.client.vo.ClientInfo;
import com.itrustmachines.common.util.UrlUtil;
import com.itrustmachines.common.vo.KeyInfo;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@ToString(exclude = { "gson" })
@Slf4j
public class LoginService {
  
  private static final String API_PATH = "/account/login";
  private static final String LOGIN_MESSAGE = "I confirm that I am the owner of the following address:{address}, and I agree to login to Blockchain Notary with this wallet address. Validate timestamp:{timestamp}.";
  private static final int MAX_RETRY_TIMES = 5;
  
  private final KeyInfo keyInfo;
  
  private final String apiUrl;
  private final String bnsServerUrl;
  private final Gson gson;
  private final MediaType JSON = MediaType.get("application/json; charset=utf-8");
  private final OkHttpClient okHttpClient;
  private final int retryDelaySec;
  
  public LoginService(String bnsServerUrl, KeyInfo keyInfo, int retryDelaySec) {
    this.keyInfo = keyInfo;
    this.bnsServerUrl = bnsServerUrl;
    this.apiUrl = UrlUtil.urlWithoutSlash(bnsServerUrl) + API_PATH;
    this.okHttpClient = OkHttpClientUtil.getOkHttpClient();
    this.gson = new Gson();
    this.retryDelaySec = retryDelaySec;
    log.info("new instance={}", this);
  }
  
  @SneakyThrows
  public ClientInfo login() {
    final LoginRequest loginRequest = buildLoginRequest(keyInfo);
    
    ClientInfo res = null;
    for (int retryCount = 0; retryCount <= MAX_RETRY_TIMES; retryCount++) {
      log.debug("login() retryCount={}", retryCount);
      try {
        res = postLogin(loginRequest);
        log.debug("login() res={}", res);
        if (checkResponse(res)) {
          break;
        }
      } catch (Exception e) {
        if (Thread.currentThread()
                  .isInterrupted()) {
          throw e;
        }
        if (retryCount == MAX_RETRY_TIMES) {
          log.error("login() fail", e);
          throw e;
        }
        TimeUnit.SECONDS.sleep(retryDelaySec);
      }
    }
    
    if (res == null) {
      String errMsg = "response is null";
      log.error("login() fail, {}", errMsg);
      throw new NullPointerException(errMsg);
    }
    return res;
  }
  
  @SneakyThrows
  public ClientInfo postLogin(LoginRequest loginRequest) {
    log.info("postLogin() begin, loginRequest={}, apiUrl={}", loginRequest, apiUrl);
    Request request = new Request.Builder().url(apiUrl)
                                           .post(RequestBody.create(gson.toJson(loginRequest), JSON))
                                           .build();
    ClientInfo res;
    try (final Response response = okHttpClient.newCall(request)
                                               .execute()) {
      
      log.debug("postLogin() response={}", response);
      final String resString = Objects.requireNonNull(response.body())
                                      .string();
      res = gson.fromJson(resString, ClientInfo.class);
      log.info("postLogin() res={}", res);
      return res;
    }
  }
  
  boolean checkResponse(ClientInfo loginRes) {
    log.debug("checkResponse() loginRes={}", loginRes);
    if (loginRes == null) {
      log.warn("checkResponse() result= false, loginRes is null");
      return false;
    }
    
    log.debug("checkResponse() result=true, response ok");
    return true;
  }
  
  private LoginRequest buildLoginRequest(final @NonNull KeyInfo keyInfo) {
    log.debug("buildLoginRequest() begin, keyInfo={}", keyInfo);
    final String toSignMessage = LOGIN_MESSAGE.replace("{address}", keyInfo.getAddress())
                                              .replace("{timestamp}", "" + System.currentTimeMillis());
    final LoginRequest registerReq = LoginRequest.builder()
                                                 .address(keyInfo.getAddress())
                                                 .toSignMessage(toSignMessage)
                                                 .build()
                                                 .sign(keyInfo.getPrivateKey());
    
    log.debug("buildLoginRequest() end, registerRequest={}", registerReq);
    return registerReq;
  }
  
}
