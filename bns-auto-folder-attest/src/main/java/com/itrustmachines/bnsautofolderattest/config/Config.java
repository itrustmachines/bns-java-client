package com.itrustmachines.bnsautofolderattest.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.itrustmachines.bnsautofolderattest.exception.InitializationException;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Slf4j
public class Config {
  
  public static final long DEFAULT_SCAN_INTERVAL = 5 * 60;
  
  private Path rootFolderPath;
  
  private Path verificationProofDownloadPath;
  
  private long scanDelay; // seconds
  
  private Path historyCsvPath;
  
  private Path scanHistoryCsvPath;
  
  private String jdbcUrl;
  
  private boolean enableCache;
  
  private boolean enableDownloadVerificationProof;
  
  public static Config load(@NonNull final String configFilePath) throws InitializationException {
    try {
      final Properties props = new Properties();
      final FileInputStream fileInputStream;
      fileInputStream = new FileInputStream(configFilePath);
      props.load(new InputStreamReader(fileInputStream, StandardCharsets.UTF_8));
      
      final String rootFolderPathStr = props.getProperty("rootFolderPath");
      final String verificationProofDownloadPathStr = props.getProperty("verificationProofDownloadPath");
      long scanDelay = NumberUtils.toLong(props.getProperty("scanDelay"), DEFAULT_SCAN_INTERVAL);
      final String jdbcUrl = props.getProperty("jdbcUrl");
      final String historyCsvPathStr = props.getProperty("historyCsvPath");
      final String scanHistoryCsvPathStr = props.getProperty("scanHistoryCsvPath");
      boolean enableCache = BooleanUtils.toBoolean(props.getProperty("enableCache"));
      boolean enableDownloadVerificationProof = BooleanUtils.toBoolean(
          props.getProperty("enableDownloadVerificationProof"));
      
      if (rootFolderPathStr == null) {
        final String errMsg = String.format("config \"rootFolderPath\" not found in \"%s\"", configFilePath);
        log.error("load() error, {}", errMsg);
        throw new InitializationException(errMsg);
      }
      final Path rootFolderPath = Paths.get(rootFolderPathStr)
                                       .normalize();
      if (Files.exists(rootFolderPath) && !Files.isDirectory(rootFolderPath)) {
        final String errMsg = String.format("config \"rootFolderPath\": \"%s\" is not directory", rootFolderPathStr);
        log.error("load() error, {}", errMsg);
        throw new InitializationException(errMsg);
      } else if (!Files.exists(rootFolderPath)) {
        Files.createDirectories(rootFolderPath);
      }
      
      if (!rootFolderPath.isAbsolute()) {
        final String errMsg = String.format("\"rootFolderPath\": \"%s\" should be absolute path", rootFolderPathStr);
        log.error("load() error, {}", errMsg);
        throw new InitializationException(errMsg);
      }
      final Path verificationProofDownloadPath = Paths.get(verificationProofDownloadPathStr)
                                                      .normalize();
      if (enableDownloadVerificationProof) {
        if (Files.exists(verificationProofDownloadPath) && !Files.isDirectory(verificationProofDownloadPath)) {
          final String errMsg = String.format("config \"verificationProofDownloadPath\": \"%s\" is not directory",
              verificationProofDownloadPathStr);
          log.error("load() error, {}", errMsg);
          throw new InitializationException(errMsg);
        } else if (!Files.exists(verificationProofDownloadPath)) {
          Files.createDirectories(verificationProofDownloadPath);
        }
        if (!verificationProofDownloadPath.isAbsolute()) {
          final String errMsg = String.format("\"verificationProofDownloadPath\": \"%s\" should be absolute path",
              verificationProofDownloadPathStr);
          log.error("load() error, {}", errMsg);
          throw new InitializationException(errMsg);
        }
        if (verificationProofDownloadPath.startsWith(rootFolderPath)) {
          final String errMsg = String.format(
              "\"verificationProofDownloadPath\": \"%s\" cannot inside \"rootFolderPath\": \"%s\"",
              verificationProofDownloadPathStr, rootFolderPathStr);
          log.error("load() error, {}", errMsg);
          throw new InitializationException(errMsg);
        }
      }
      if (scanDelay < 0) {
        final String errMsg = "config \"scanDelay\" can not less then zero";
        log.error("load() error, {}", errMsg);
        throw new InitializationException(errMsg);
      }
      if (historyCsvPathStr == null) {
        final String errMsg = String.format("config \"historyCsvPath\" not fount in \"%s\"", configFilePath);
        log.error("load() error, {}", errMsg);
        throw new InitializationException(errMsg);
      }
      final Path historyCsvPath = Paths.get(historyCsvPathStr);
      if (!FileSystems.getDefault()
                      .getPathMatcher("glob:*.csv")
                      .matches(historyCsvPath)) {
        final String errMsg = String.format("\"historyCsvPath\": \"%s\" not a csv file", historyCsvPathStr);
        log.error("load() error, {}", errMsg);
        throw new InitializationException(errMsg);
      }
      if (scanHistoryCsvPathStr == null) {
        final String errMsg = String.format("config \"scanHistoryCsvPath_\" not fount in \"%s\"", configFilePath);
        log.error("load() error, {}", errMsg);
        throw new InitializationException(errMsg);
      }
      final Path scanHistoryCsvPath = Paths.get(scanHistoryCsvPathStr);
      if (!FileSystems.getDefault()
                      .getPathMatcher("glob:*.csv")
                      .matches(scanHistoryCsvPath)) {
        final String errMsg = String.format("\"scanHistoryCsvPath_\": \"%s\" not a csv file", scanHistoryCsvPathStr);
        log.error("load() error, {}", errMsg);
        throw new InitializationException(errMsg);
      }
      if (historyCsvPath.endsWith(scanHistoryCsvPath)) {
        final String errMsg = "can not use same path for \"historyCsvPath\" and \"scanHistoryCsvPath_\"";
        log.error("load() error, {}", errMsg);
        throw new InitializationException(errMsg);
      }
      if (jdbcUrl == null) {
        final String errMsg = String.format("config \"jdbcUrl\" not fount in \"%s\"", configFilePath);
        log.error("load() error, {}", errMsg);
        throw new InitializationException(errMsg);
      }
      
      final Config result = Config.builder()
                                  .rootFolderPath(rootFolderPath)
                                  .verificationProofDownloadPath(verificationProofDownloadPath)
                                  .scanDelay(scanDelay)
                                  .historyCsvPath(historyCsvPath)
                                  .scanHistoryCsvPath(scanHistoryCsvPath)
                                  .jdbcUrl(jdbcUrl)
                                  .enableCache(enableCache)
                                  .enableDownloadVerificationProof(enableDownloadVerificationProof)
                                  .build();
      log.info("load[{}] config={}", configFilePath, result);
      return result;
    } catch (IOException e) {
      throw new InitializationException(e);
    }
  }
  
}
