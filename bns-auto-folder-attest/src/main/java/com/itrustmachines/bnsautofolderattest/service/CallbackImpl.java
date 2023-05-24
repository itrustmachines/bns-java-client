package com.itrustmachines.bnsautofolderattest.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.itrustmachines.bnsautofolderattest.callback.Callback;
import com.itrustmachines.bnsautofolderattest.exception.CallbackException;
import com.itrustmachines.bnsautofolderattest.exception.InitializationException;
import com.itrustmachines.bnsautofolderattest.vo.AttestationRecord;
import com.itrustmachines.bnsautofolderattest.vo.CsvWritable;
import com.itrustmachines.bnsautofolderattest.vo.ScanResult;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CallbackImpl implements Callback {
  
  private final Path historyCsvPath;
  private final Path scanHistoryCsvPath;
  
  public CallbackImpl(@Nullable final Path historyCsvPath, @Nullable final Path scanHistoryCsvPath) {
    this.historyCsvPath = historyCsvPath;
    this.scanHistoryCsvPath = scanHistoryCsvPath;
    
    log.info("new instance={}", this);
  }
  
  public void checkFile(@NonNull final Path csvPath, @NonNull final CsvWritable csvWritable)
      throws InitializationException {
    try {
      if (Files.notExists(csvPath)) {
        Files.write(csvPath, new byte[] { (byte) 0xef, (byte) 0xbb, (byte) 0xbf });
        Files.write(csvPath, String.join(",", csvWritable.getHeader())
                                   .getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.APPEND);
      }
    } catch (IOException e) {
      throw new InitializationException(e);
    }
  }
  
  private void writeCsv(@NonNull final Path csvPath, @NotNull final CsvWritable csvWritable)
      throws InitializationException, IOException {
    checkFile(csvPath, csvWritable);
    final String row = csvWritable.toCsvString();
    Files.write(csvPath, row.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
  }
  
  @Override
  public void onScanResult(@NonNull final ScanResult scanResult) throws CallbackException {
    log.info("onScanResult() scanResult={}", scanResult);
    try {
      writeCsv(scanHistoryCsvPath, scanResult);
    } catch (InitializationException | IOException e) {
      throw new CallbackException(e);
    }
  }
  
  @Override
  public void onAttested(@NonNull final AttestationRecord attestationRecord) throws CallbackException {
    log.info("onAttested() attestationRecord={}", attestationRecord);
    try {
      writeCsv(historyCsvPath, attestationRecord);
    } catch (InitializationException | IOException e) {
      throw new CallbackException(e);
    }
  }
  
  @Override
  public void onAttestFail(@NonNull final AttestationRecord attestationRecord) throws CallbackException {
    log.error("onAttestFail() attestationRecord={}", attestationRecord);
    try {
      writeCsv(historyCsvPath, attestationRecord);
    } catch (InitializationException | IOException e) {
      throw new CallbackException(e);
    }
  }
  
  @Override
  public void onVerified(@NonNull final AttestationRecord attestationRecord) throws CallbackException {
    log.error("onVerified() attestationRecord={}", attestationRecord);
    try {
      writeCsv(historyCsvPath, attestationRecord);
    } catch (InitializationException | IOException e) {
      throw new CallbackException(e);
    }
  }
  
  @Override
  public void onVerifyFail(@NonNull final AttestationRecord attestationRecord) throws CallbackException {
    log.error("onVerifyFail() attestationRecord={}", attestationRecord);
    try {
      writeCsv(historyCsvPath, attestationRecord);
    } catch (InitializationException | IOException e) {
      throw new CallbackException(e);
    }
  }
  
  @Override
  public void onSaveProof(@NonNull final AttestationRecord attestationRecord) throws CallbackException {
    log.error("onSaveProof() attestationRecord={}", attestationRecord);
    try {
      writeCsv(historyCsvPath, attestationRecord);
    } catch (InitializationException | IOException e) {
      throw new CallbackException(e);
    }
  }
  
  @Override
  public void onSaveFail(@NonNull final AttestationRecord attestationRecord) throws CallbackException {
    log.error("onSaveFail() attestationRecord={}", attestationRecord);
    try {
      writeCsv(historyCsvPath, attestationRecord);
    } catch (InitializationException | IOException e) {
      throw new CallbackException(e);
    }
  }
  
}
