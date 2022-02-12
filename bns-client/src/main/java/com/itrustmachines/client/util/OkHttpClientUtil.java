package com.itrustmachines.client.util;

import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.jetbrains.annotations.NotNull;

import lombok.Synchronized;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

@UtilityClass
@Slf4j
public class OkHttpClientUtil {
  
  public static OkHttpClient client;
  public static OkHttpClient authenticateClient;
  
  @Synchronized
  public OkHttpClient getOkHttpClient() {
    if (client != null) {
      log.info("getOkHttpClient() reuse result={}", client);
      return client;
    }
    client = ignoreCertificate().build();
    log.info("getOkHttpClient() build result={}", client);
    return client;
  }
  
  @Synchronized
  public OkHttpClient getOkHttpClient(String userName, String password) {
    if (authenticateClient != null) {
      log.info("getOkHttpClient() reuse result={}", authenticateClient);
      return authenticateClient;
    }
    authenticateClient = authenticate(userName, password).build();
    log.info("getOkHttpClient() result={}", authenticateClient);
    return authenticateClient;
  }
  
  private OkHttpClient.Builder ignoreCertificate() {
    OkHttpClient.Builder builder = new OkHttpClient.Builder();
    builder = configureToIgnoreCertificate(builder);
    builder = withBnsRequirements(builder);
    builder = builder.readTimeout(30, TimeUnit.SECONDS);
    return builder;
  }
  
  // ignore ssl and need authenticate
  private OkHttpClient.Builder authenticate(String userName, String password) {
    OkHttpClient.Builder builder = ignoreCertificate();
    builder = builder.authenticator((route, response) -> {
      String credential = Credentials.basic(userName, password);
      return response.request()
                     .newBuilder()
                     .header("Authorization", credential)
                     .build();
    });
    builder = withBnsRequirements(builder);
    return builder;
  }
  
  private OkHttpClient.Builder withBnsRequirements(OkHttpClient.Builder builder) {
    builder = builder.cookieJar(new CookieJar() {
      private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();
      
      @Override
      public void saveFromResponse(@NotNull HttpUrl url, @NotNull List<Cookie> cookies) {
        cookieStore.put(url.host(), cookies);
      }
      
      @NotNull
      @Override
      public List<Cookie> loadForRequest(@NotNull HttpUrl url) {
        List<Cookie> cookies = cookieStore.get(url.host());
        return cookies != null ? cookies : new ArrayList<>();
      }
    });
    builder = builder.addInterceptor(chain -> {
      Request newRequest = chain.request()
                                .newBuilder()
                                .addHeader("device-type", "sdk")
                                .build();
      return chain.proceed(newRequest);
    });
    return builder;
  }
  
  private OkHttpClient.Builder configureToIgnoreCertificate(OkHttpClient.Builder builder) {
    log.info("Ignore Ssl Certificate");
    try {
      // Create a trust manager that does not validate certificate chains
      final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
        @Override
        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
            throws CertificateException {
        }
        
        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
            throws CertificateException {
        }
        
        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
          return new java.security.cert.X509Certificate[] {};
        }
      } };
      // Install the all-trusting trust manager
      final SSLContext sslContext = SSLContext.getInstance("SSL");
      sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
      // Create an ssl socket factory with our all-trusting manager
      final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
      builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
      builder.hostnameVerifier(new HostnameVerifier() {
        
        @Override
        public boolean verify(String hostname, SSLSession session) {
          return true;
        }
      });
    } catch (Exception e) {
      log.error("Exception while configuring IgnoreSslCertificate", e);
    }
    return builder;
  }
  
}
