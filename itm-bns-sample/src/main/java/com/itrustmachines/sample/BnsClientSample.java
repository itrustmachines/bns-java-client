package com.itrustmachines.sample;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.*;

import com.google.gson.Gson;
import com.itrustmachines.client.BnsClient;
import com.itrustmachines.client.config.BnsClientConfig;
import com.itrustmachines.client.exception.BnsClientException;
import com.itrustmachines.client.input.vo.LedgerInputResponse;
import com.itrustmachines.client.todo.BnsClientCallback;
import com.itrustmachines.client.todo.BnsClientReceiptDao;
import com.itrustmachines.common.util.HashUtils;
import com.itrustmachines.common.util.KeyInfoUtil;
import com.itrustmachines.common.vo.KeyInfo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BnsClientSample {
  
  // 設定Properties file名稱
  public static final String SAMPLE_PROPERTIES = "sample.properties";
  
  // 建立sqlite database連線位址
  public static final String JDBC_URL = "jdbc:sqlite:BnsDevice.db";
  
  // 設定每次存證資料時間間隔
  public static final int LEDGER_INPUT_DELAY_SECOND = 10;
  
  public static final String[] PROP_PATH_LIST = new String[] { "./", "./src/main/resources/",
      "./itm-bns-java-client/itm-bns-sample/src/main/resources/", "./itm-bns-sample/src/main/resources/" };
  
  public static void main(String[] args) throws IOException, SQLException, BnsClientException {
    
    String filePath;
    final Options options = new Options();
    final Option filePathOption = Option.builder()
                                        .argName("filePath")
                                        .longOpt("file")
                                        .hasArg()
                                        .desc("input attestation file path")
                                        .optionalArg(true)
                                        .build();
    
    options.addOption(filePathOption);
    final CommandLineParser parser = new DefaultParser();
    
    try {
      final CommandLine line = parser.parse(options, args);
      filePath = line.getOptionValue("file");
      log.debug("filePath={}", filePath);
    } catch (ParseException e) {
      log.error("error", e);
      final HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("BNS-Java-Client", options);
      throw new IOException("No such file.");
    }
    
    log.info("working directory:{}", new File(".").getAbsolutePath());
    
    final String configPath = FileUtil.findFile(SAMPLE_PROPERTIES, PROP_PATH_LIST);
    
    // 讀取properties檔案以設定Config內容, 請參照程式流程說明(1.)
    final BnsClientConfig config = BnsClientConfig.load(configPath);
    log.info("BnsClientConfig={}", config);
    
    // 使用者須實作SpoClientCallback方法, 請參照程式流程說明(2.)
    final BnsClientCallback callback = new CallbackSample();
    
    // 使用者須實作SpoClientReceiptDao方法, 請參照程式流程說明(3.)
    final BnsClientReceiptDao receiptDao = new ReceiptDaoSample(JDBC_URL);
    
    // 建立SpoClient服務, 請參照程式流程說明(4.)
    final BnsClient bnsClient = BnsClient.init(config, callback, receiptDao);
    bnsClient.login();
    
    while (!Thread.currentThread()
                  .isInterrupted()) {
      
      // 產生RawData, 請參照程式流程說明(5.)
      final String privateKey = bnsClient.getConfig()
                                         .getPrivateKey();
      final KeyInfo keyInfo = KeyInfoUtil.buildKeyInfo(privateKey);
      final String deviceId = keyInfo.getAddress();
      final long timestamp = System.currentTimeMillis();
      LedgerInputResponse ledgerInputResult;
      String cmdJson;
      
      if (filePath == null) {
        
        final double watt = 15.00 + (Math.random() * 100 % 100 / 100);
        final Cmd cmd = Cmd.builder()
                           .text("" + watt)
                           .description("watt")
                           .deviceId(deviceId)
                           .timestamp(timestamp)
                           .watt(watt)
                           .build();
        log.info("cmd create={}", cmd);
        cmdJson = new Gson().toJson(cmd);
        ledgerInputResult = bnsClient.ledgerInput(cmdJson);
      } else {
        
        final Path imgFile = Paths.get(filePath);
        final String fileName = imgFile.getFileName()
                                       .toString();
        final String binaryFileHash = HashUtils.byte2hex(HashUtils.sha256(Files.newInputStream(imgFile)));
        final CmdBinary cmdBinary = CmdBinary.builder()
                                             .deviceId(deviceId)
                                             .timestamp(timestamp)
                                             .fileName(fileName)
                                             .fileHash(binaryFileHash)
                                             .build();
        log.info("cmdBinary create={}", cmdBinary);
        cmdJson = new Gson().toJson(cmdBinary);
        ledgerInputResult = bnsClient.ledgerInput(cmdJson);
      }
      log.info("ledger input result={}", ledgerInputResult);
      
      try {
        TimeUnit.SECONDS.sleep(LEDGER_INPUT_DELAY_SECOND);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    
    // 將SpoClient服務關閉, 請參照程式流程說明(7.)
    bnsClient.stopAsyncVerifyReceipts();
  }
  
}
