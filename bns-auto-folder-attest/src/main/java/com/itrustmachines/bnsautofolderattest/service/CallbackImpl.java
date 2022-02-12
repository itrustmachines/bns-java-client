package com.itrustmachines.bnsautofolderattest.service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.itrustmachines.bnsautofolderattest.callback.Callback;
import com.itrustmachines.bnsautofolderattest.vo.AttestationRecord;
import com.itrustmachines.bnsautofolderattest.vo.AttestationStatus;
import com.itrustmachines.bnsautofolderattest.vo.AttestationType;
import com.itrustmachines.bnsautofolderattest.vo.ScanResult;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CallbackImpl implements Callback {
  
  private final Path historyCsvPath;
  private final Path scanHistoryCsvPath;
  
  public CallbackImpl(@Nullable final Path historyCsvPath, @Nullable final Path scanHistoryCsvPath) {
    this.historyCsvPath = historyCsvPath;
    this.scanHistoryCsvPath = scanHistoryCsvPath;
    checkFile();
    
    log.info("new instance={}", this);
  }
  
  @SneakyThrows
  public void checkFile() {
    if (Files.notExists(historyCsvPath)) {
      Files.write(historyCsvPath, new byte[] { (byte) 0xef, (byte) 0xbb, (byte) 0xbf });
      Files.write(historyCsvPath, String.join(",", getHistoryHeader())
                                        .getBytes(StandardCharsets.UTF_8),
          StandardOpenOption.APPEND);
    }
    if (Files.notExists(scanHistoryCsvPath)) {
      Files.write(scanHistoryCsvPath, new byte[] { (byte) 0xef, (byte) 0xbb, (byte) 0xbf });
      Files.write(scanHistoryCsvPath, String.join(",", getScanHistoryHeader())
                                            .getBytes(StandardCharsets.UTF_8),
          StandardOpenOption.APPEND);
    }
  }
  
  @Override
  public void onScanResult(@NonNull final ScanResult scanResult) {
    log.info("onScanResult() scanResult={}", scanResult);
    writeScanHistory(scanResult);
  }
  
  @SneakyThrows
  @Override
  public void onAttested(@NonNull final AttestationRecord attestationRecord) {
    log.info("onAttested() attestationRecord={}", attestationRecord);
    writeHistory(attestationRecord);
  }
  
  @SneakyThrows
  @Override
  public void onAttestFail(@NonNull final AttestationRecord attestationRecord) {
    log.error("onAttestFail() attestationRecord={}", attestationRecord);
    writeHistory(attestationRecord);
  }
  
  @SneakyThrows
  @Override
  public void onVerified(@NonNull final AttestationRecord attestationRecord) {
    log.error("onVerified() attestationRecord={}", attestationRecord);
    writeHistory(attestationRecord);
  }
  
  @Override
  public void onVerifyFail(@NonNull AttestationRecord attestationRecord) {
    log.error("onVerifyFail() attestationRecord={}", attestationRecord);
    writeHistory(attestationRecord);
  }
  
  @Override
  public void onSaveProof(@NonNull AttestationRecord attestationRecord) {
    log.error("onSaveProof() attestationRecord={}", attestationRecord);
    writeHistory(attestationRecord);
  }
  
  @Override
  public void onSaveFail(@NonNull AttestationRecord attestationRecord) {
    log.error("onSaveFail() attestationRecord={}", attestationRecord);
    writeHistory(attestationRecord);
  }
  
  @NotNull
  private static String getScanHistoryHeader() {
    return "Start Time,Total Count,Total Bytes,Added Count,Modified Count,Attested Count,End Time\n";
  }
  
  @SneakyThrows
  private void writeScanHistory(@NotNull final ScanResult scanResult) {
    StringBuilder sb = new StringBuilder();
    sb.append(scanResult.getStartTime());
    sb.append(",");
    sb.append(scanResult.getTotalCount());
    sb.append(",");
    sb.append(scanResult.getTotalBytes());
    sb.append(",");
    sb.append(scanResult.getAddedCount());
    sb.append(",");
    sb.append(scanResult.getModifiedCount());
    sb.append(",");
    sb.append(scanResult.getAttestedCount());
    sb.append(",");
    sb.append(scanResult.getEndTime());
    sb.append("\n");
    checkFile();
    Files.write(scanHistoryCsvPath, sb.toString()
                                      .getBytes(StandardCharsets.UTF_8),
        StandardOpenOption.APPEND);
  }
  
  @NotNull
  private static String getHistoryHeader() {
    return "Attestation Type,Attestation Status,Relative File Path,Last Modified Time,File Hash,Address,Clearance Order,Index Value,Proof Path,Previous Modified Time,Previous File Hash\n";
  }
  
  @SneakyThrows
  private void writeHistory(@NotNull final AttestationRecord attestationRecord) {
    StringBuilder sb = new StringBuilder();
    sb.append(attestationRecord.getType());
    sb.append(",");
    sb.append(attestationRecord.getStatus());
    sb.append(",");
    sb.append(attestationRecord.getRelativeFilePath());
    sb.append(",");
    sb.append(attestationRecord.getLastModifiedTime());
    sb.append(",");
    sb.append(attestationRecord.getFileHash());
    sb.append(",");
    sb.append(attestationRecord.getAddress());
    sb.append(",");
    sb.append(attestationRecord.getClearanceOrder());
    sb.append(",");
    sb.append(attestationRecord.getIndexValue());
    sb.append(",");
    if (AttestationStatus.SAVE_PROOF == attestationRecord.getStatus()) {
      sb.append(attestationRecord.getProofPath());
    }
    sb.append(",");
    if (AttestationType.MODIFIED == attestationRecord.getType()) {
      sb.append(attestationRecord.getPreviousRecord()
                                 .getLastModifiedTime());
    }
    sb.append(",");
    if (AttestationType.MODIFIED == attestationRecord.getType()) {
      sb.append(attestationRecord.getPreviousRecord()
                                 .getFileHash());
    }
    sb.append("\n");
    checkFile();
    Files.write(historyCsvPath, sb.toString()
                                  .getBytes(StandardCharsets.UTF_8),
        StandardOpenOption.APPEND);
  }
  
}
