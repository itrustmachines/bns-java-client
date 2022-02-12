package com.itrustmachines.bnsautofolderattest.config;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
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
  
  private boolean disableCache;
  
  private boolean disableDownloadVerificationProof;
  
  @SneakyThrows
  public static Config load(@NonNull final String configFilePath) {
    final Properties props = new Properties();
    final FileInputStream fileInputStream = new FileInputStream(configFilePath);
    props.load(new InputStreamReader(fileInputStream, StandardCharsets.UTF_8));
    
    final String rootFolderPath = props.getProperty("rootFolderPath");
    final String verificationProofDownloadPath = props.getProperty("verificationProofDownloadPath");
    long scanDelay = NumberUtils.toLong(props.getProperty("scanDelay"), DEFAULT_SCAN_INTERVAL);
    final String jdbcUrl = props.getProperty("jdbcUrl");
    final String historyCsvPath = props.getProperty("historyCsvPath");
    final String scanHistoryCsvPath = props.getProperty("scanHistoryCsvPath");
    boolean disableDownloadVerificationProof = BooleanUtils.toBoolean(
        props.getProperty("disableDownloadVerificationProof"));
    boolean disableCache = BooleanUtils.toBoolean(props.getProperty("disableCache"));
    
    if (rootFolderPath == null) {
      final String errMsg = String.format("config \"rootFolderPath\" not found in \"%s\"", configFilePath);
      log.error("load() error, {}", errMsg);
      throw new RuntimeException(errMsg);
    }
    if (Files.exists(Paths.get(rootFolderPath)) && !Files.isDirectory(Paths.get(rootFolderPath))) {
      final String errMsg = String.format("config \"rootFolderPath\": \"%s\" is not directory", rootFolderPath);
      log.error("load() error, {}", errMsg);
      throw new RuntimeException(errMsg);
    } else if (!Files.exists(Paths.get(rootFolderPath))) {
      Files.createDirectories(Paths.get(rootFolderPath));
    }
    
    if (!Paths.get(rootFolderPath)
              .isAbsolute()) {
      final String errMsg = String.format("\"rootFolderPath\": \"%s\" should be absolute path", rootFolderPath);
      log.error("load() error, {}", errMsg);
      throw new RuntimeException(errMsg);
    }
    if (!disableDownloadVerificationProof) {
      if (verificationProofDownloadPath == null) {
        final String errMsg = String.format("config \"verificationProofDownloadPath\" not found in \"%s\"",
            configFilePath);
        log.error("load() error, {}", errMsg);
        throw new RuntimeException(errMsg);
      }
      if (Files.exists(Paths.get(verificationProofDownloadPath))
          && !Files.isDirectory(Paths.get(verificationProofDownloadPath))) {
        final String errMsg = String.format("config \"verificationProofDownloadPath\": \"%s\" is not directory",
            verificationProofDownloadPath);
        log.error("load() error, {}", errMsg);
        throw new RuntimeException(errMsg);
      } else if (!Files.exists(Paths.get(verificationProofDownloadPath))) {
        Files.createDirectories(Paths.get(verificationProofDownloadPath));
      }
      if (!Paths.get(verificationProofDownloadPath)
                .isAbsolute()) {
        final String errMsg = String.format("\"verificationProofDownloadPath\": \"%s\" should be absolute path",
            verificationProofDownloadPath);
        log.error("load() error, {}", errMsg);
        throw new RuntimeException(errMsg);
      }
      if (verificationProofDownloadPath.startsWith(rootFolderPath)) {
        final String errMsg = String.format(
            "\"verificationProofDownloadPath\": \"%s\" cannot inside \"rootFolderPath\": \"%s\"",
            verificationProofDownloadPath, rootFolderPath);
        log.error("load() error, {}", errMsg);
        throw new RuntimeException(errMsg);
      }
    }
    if (scanDelay < 0) {
      final String errMsg = "config \"scanDelay\" can not less then zero";
      log.error("load() error, {}", errMsg);
      throw new RuntimeException(errMsg);
    }
    if (historyCsvPath == null) {
      final String errMsg = String.format("config \"historyCsvPath\" not fount in \"%s\"", configFilePath);
      log.error("load() error, {}", errMsg);
      throw new RuntimeException(errMsg);
    }
    if (!FileSystems.getDefault()
                    .getPathMatcher("glob:*.csv")
                    .matches(Paths.get(historyCsvPath))) {
      final String errMsg = String.format("\"historyCsvPath\": \"%s\" not a csv file", historyCsvPath);
      log.error("load() error, {}", errMsg);
      throw new RuntimeException(errMsg);
    }
    if (scanHistoryCsvPath == null) {
      final String errMsg = String.format("config \"scanHistoryCsvPath\" not fount in \"%s\"", configFilePath);
      log.error("load() error, {}", errMsg);
      throw new RuntimeException(errMsg);
    }
    if (!FileSystems.getDefault()
                    .getPathMatcher("glob:*.csv")
                    .matches(Paths.get(scanHistoryCsvPath))) {
      final String errMsg = String.format("\"scanHistoryCsvPath\": \"%s\" not a csv file", scanHistoryCsvPath);
      log.error("load() error, {}", errMsg);
      throw new RuntimeException(errMsg);
    }
    if (Paths.get(historyCsvPath)
             .endsWith(Paths.get(scanHistoryCsvPath))) {
      final String errMsg = "can not use same path for \"historyCsvPath\" and \"scanHistoryCsvPath\"";
      log.error("load() error, {}", errMsg);
      throw new RuntimeException(errMsg);
    }
    if (jdbcUrl == null) {
      final String errMsg = String.format("config \"jdbcUrl\" not fount in \"%s\"", configFilePath);
      log.error("load() error, {}", errMsg);
      throw new RuntimeException(errMsg);
    }
    
    final Config result = Config.builder()
                                .rootFolderPath(Paths.get(rootFolderPath))
                                .verificationProofDownloadPath(Paths.get(verificationProofDownloadPath))
                                .scanDelay(scanDelay)
                                .historyCsvPath(Paths.get(historyCsvPath))
                                .scanHistoryCsvPath(Paths.get(scanHistoryCsvPath))
                                .jdbcUrl(jdbcUrl)
                                .disableDownloadVerificationProof(disableDownloadVerificationProof)
                                .disableCache(disableCache)
                                .build();
    log.info("load[{}] config={}", configFilePath, result);
    return result;
  }
  
}
