package com.itrustmachines.bnsautofolderattest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.web3j.crypto.Credentials;

import com.itrustmachines.bnsautofolderattest.bns.service.BnsClientCallbackImpl;
import com.itrustmachines.bnsautofolderattest.bns.service.BnsClientReceiptDaoImpl;
import com.itrustmachines.bnsautofolderattest.bns.service.VerificationProofService;
import com.itrustmachines.bnsautofolderattest.config.Config;
import com.itrustmachines.bnsautofolderattest.service.AttestService;
import com.itrustmachines.bnsautofolderattest.service.AttestationRecordService;
import com.itrustmachines.bnsautofolderattest.service.CallbackImpl;
import com.itrustmachines.bnsautofolderattest.util.AESUtil;
import com.itrustmachines.bnsautofolderattest.util.FileUtil;
import com.itrustmachines.client.BnsClient;
import com.itrustmachines.client.config.BnsClientConfig;
import com.itrustmachines.common.util.HashUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BnsAutoFolderAttestApplication {
  
  // 設定Properties file名稱
  public static String SAMPLE_PROPERTIES = "sample.properties";
  
  public static String ITM_ENCRYPTED_PRIVATE_KEY = ".itm.encrypted.private.key";
  
  public static final String[] PROP_PATH_LIST = new String[] { "./", "./src/main/resources/",
      "./bns-auto-folder-attest/src/main/resources/" };
  
  public static void main(String[] args) {
    final Config config;
    final BnsClient bnsClient;
    final AttestService attestService;
    try {
      final String privateKey = loadPrivateKey();
      
      final String configPath = FileUtil.findFile(SAMPLE_PROPERTIES, PROP_PATH_LIST);
      log.info("main() configPath={}", configPath);
      
      config = Config.load(configPath);
      final BnsClientConfig bnsClientConfig = BnsClientConfig.load(configPath);
      bnsClientConfig.setPrivateKey(privateKey);
      log.info("main() config={}", config);
      log.info("main() bnsClientConfig={}", bnsClientConfig);
      
      final CallbackImpl callback = new CallbackImpl(config.getHistoryCsvPath(), config.getScanHistoryCsvPath());
      final AttestationRecordService attestationRecordService = new AttestationRecordService(config);
      bnsClient = BnsClient.init(bnsClientConfig, new BnsClientCallbackImpl(config, callback, attestationRecordService,
          new VerificationProofService(bnsClientConfig)), new BnsClientReceiptDaoImpl(config.getJdbcUrl()));
      attestService = new AttestService(config, attestationRecordService, bnsClient, callback);
    } catch (Exception e) {
      log.error("main() error", e);
      System.exit(1);
      return;
    }
    
    // auto restart until interrupted
    while (!Thread.currentThread()
                  .isInterrupted()) {
      
      try {
        // process and delay seconds
        while (!Thread.currentThread()
                      .isInterrupted()) {
          
          attestService.process();
          bnsClient.verifyNow();
          
          TimeUnit.SECONDS.sleep(config.getScanDelay());
        }
      } catch (InterruptedException e) {
        log.info("main() interrupted");
        break;
      } catch (Exception e) {
        log.error("main() error, currentPath={}", System.getProperty("user.dir"), e);
        log.info("main() restart after 60 seconds");
        try {
          TimeUnit.SECONDS.sleep(60);
        } catch (InterruptedException ex) {
          log.info("main() interrupted");
          break;
        }
      }
    }
    
    log.info("main() end");
    System.exit(0);
  }
  
  static String loadPrivateKey() throws IOException, NoSuchPaddingException, IllegalBlockSizeException,
      NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
    final Path encryptedPrivateKeyPath = Paths.get(ITM_ENCRYPTED_PRIVATE_KEY);
    if (Files.exists(encryptedPrivateKeyPath)) {
      Scanner sc = new Scanner(System.in);
      log.info("Enter Pin Code: ");
      final String pinCode = sc.next();
      final String pinCodeHash = HashUtils.sha256(pinCode)
                                          .toLowerCase(Locale.ROOT);
      final List<String> fileContent = Files.readAllLines(encryptedPrivateKeyPath, StandardCharsets.UTF_8);
      if (!pinCodeHash.equalsIgnoreCase(fileContent.get(0))) {
        throw new RuntimeException("Invalid Pin Code");
      }
      return AESUtil.decrypt(fileContent.get(1), pinCode);
    } else {
      Scanner sc = new Scanner(System.in);
      String privateKey = "";
      while (!Thread.currentThread()
                    .isInterrupted()) {
        log.info("Enter Private Key: ");
        privateKey = sc.next();
        try {
          Credentials.create(privateKey);
          break;
        } catch (Exception e) {
          log.error("Invalid Private Key format");
        }
      }
      String pinCode = "";
      while (pinCode.length() < 8) {
        log.info("Enter Pin Code: ");
        pinCode = sc.next();
        if (pinCode.length() < 8) {
          log.info("Pin Code is not strong enough (minimum length: 8)");
        }
      }
      log.info("Repeat Pin Code: ");
      final String repeatPinCode = sc.next();
      if (!pinCode.equals(repeatPinCode)) {
        throw new RuntimeException("Pin Code not match");
      }
      final String pinCodeHash = HashUtils.sha256(pinCode)
                                          .toLowerCase(Locale.ROOT);
      final List<String> fileContent = List.of(pinCodeHash, AESUtil.encrypt(privateKey, pinCode));
      Files.write(encryptedPrivateKeyPath, fileContent, StandardCharsets.UTF_8);
      return privateKey;
    }
    
  }
  
}