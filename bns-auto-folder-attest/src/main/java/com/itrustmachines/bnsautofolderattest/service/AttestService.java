package com.itrustmachines.bnsautofolderattest.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itrustmachines.bnsautofolderattest.callback.Callback;
import com.itrustmachines.bnsautofolderattest.config.Config;
import com.itrustmachines.bnsautofolderattest.vo.AttestationRecord;
import com.itrustmachines.bnsautofolderattest.vo.AttestationStatus;
import com.itrustmachines.bnsautofolderattest.vo.AttestationType;
import com.itrustmachines.bnsautofolderattest.vo.Cmd;
import com.itrustmachines.bnsautofolderattest.vo.FileInfo;
import com.itrustmachines.bnsautofolderattest.vo.ScanResult;
import com.itrustmachines.client.BnsClient;
import com.itrustmachines.client.input.vo.LedgerInputResponse;
import com.itrustmachines.common.constants.StatusConstantsString;
import com.itrustmachines.common.util.HashUtils;

import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@ToString(exclude = { "gson" })
@Slf4j
public class AttestService {
  
  private final Config config;
  private final Path rootFolderPath;
  private final AttestationRecordService attestationRecordService;
  private final BnsClient bnsClient;
  private final Callback callback;
  private final Gson gson;
  
  public AttestService(@NonNull final Config config, @NonNull final AttestationRecordService attestationRecordService,
      @NonNull final BnsClient bnsClient, @NonNull final Callback callback) {
    this.config = config;
    this.rootFolderPath = config.getRootFolderPath();
    this.attestationRecordService = attestationRecordService;
    this.bnsClient = bnsClient;
    this.callback = callback;
    this.gson = new GsonBuilder().disableHtmlEscaping()
                                 .create();
    
    log.info("new instance={}", this);
  }
  
  public void process() {
    log.debug("process() start");
    
    final List<FileInfo> toAttestFileList = scan();
    
    if (!toAttestFileList.isEmpty()) {
      attestAll(toAttestFileList);
    }
    
    log.debug("process() end");
  }
  
  public List<FileInfo> scan() {
    log.debug("scan() start, rootFolderPath={}", rootFolderPath);
    final List<FileInfo> toAttestFileInfoList = new ArrayList<>();
    final ZonedDateTime startTime = ZonedDateTime.now();
    final List<Path> filePathList;
    try {
      filePathList = Files.walk(rootFolderPath)
                          .filter(path -> !Files.isDirectory(path))
                          .collect(Collectors.toList());
    } catch (IOException e) {
      log.error("scan() folder scan error", e);
      return new ArrayList<>();
    }
    
    final List<FileInfo> fileInfoList = filePathList.stream()
                                                    .map(this::getFileInfo)
                                                    .filter(Objects::nonNull)
                                                    .map(this::checkAddedAndModified)
                                                    .collect(Collectors.toList());
    log.debug("scan() fileInfoList.size={}", fileInfoList.size());
    int totalCount = fileInfoList.size();
    long totalBytes = fileInfoList.stream()
                                  .map(FileInfo::getFilePath)
                                  .map(Path::toFile)
                                  .map(File::length)
                                  .reduce(0L, Long::sum);
    final long addedCount = fileInfoList.stream()
                                        .filter(fileInfo -> AttestationType.ADDED == fileInfo.getType())
                                        .peek(toAttestFileInfoList::add)
                                        .count();
    final long modifiedCount = fileInfoList.stream()
                                           .filter(fileInfo -> AttestationType.MODIFIED == fileInfo.getType())
                                           .peek(toAttestFileInfoList::add)
                                           .count();
    final long attestedCount = fileInfoList.stream()
                                           .filter(fileInfo -> AttestationType.ATTESTED == fileInfo.getType())
                                           .count();
    final long unknownCount = fileInfoList.stream()
                                          .filter(fileInfo -> AttestationType.UNKNOWN == fileInfo.getType())
                                          .count();
    log.info("scan() end, total={}, totalBytes={}, added={}, modified={}, attested={}, unknown={}", totalCount,
        totalBytes, addedCount, modifiedCount, attestedCount, unknownCount);
    try {
      callback.onScanResult(ScanResult.builder()
                                      .startTime(startTime)
                                      .totalCount(totalCount)
                                      .totalBytes(totalBytes)
                                      .addedCount(addedCount)
                                      .modifiedCount(modifiedCount)
                                      .attestedCount(attestedCount)
                                      .endTime(ZonedDateTime.now())
                                      .build());
    } catch (Exception e) {
      log.warn("onAttested() callback error", e);
    }
    return toAttestFileInfoList;
  }
  
  public void attestAll(@NonNull final List<FileInfo> toAttestFileInfoList) {
    log.debug("attestAll() start, toAttestFileInfoList.size={}", toAttestFileInfoList.size());
    long attestedCount = 0;
    long attestFailCount = 0;
    long unknownCount = 0;
    for (FileInfo fileInfo : toAttestFileInfoList) {
      final AttestationRecord attestationRecord = attest(fileInfo);
      switch (attestationRecord.getStatus()) {
        case ATTESTED:
          onAttested(attestationRecord);
          attestedCount++;
          break;
        case FAIL:
          onAttestFail(attestationRecord);
          attestFailCount++;
          break;
        case UNKNOWN:
          onAttestFail(attestationRecord);
          unknownCount++;
          break;
      }
    }
    
    log.info("attestAll() end, attested={}, attestFail={}, unknown={}", attestedCount, attestFailCount, unknownCount);
  }
  
  @Nullable
  private FileInfo getFileInfo(@NonNull final Path filePath) {
    log.debug("getFileInfo() start, filePath={}", filePath);
    final Path relativeFilePath = rootFolderPath.relativize(filePath);
    final ZonedDateTime lastModifiedTime;
    try {
      lastModifiedTime = Files.getLastModifiedTime(filePath)
                              .toInstant()
                              .atZone(ZoneId.systemDefault())
                              .truncatedTo(ChronoUnit.MILLIS);
    } catch (IOException e) {
      log.error("getFileInfo() get lastModifiedTime error, filePath={}", filePath, e);
      return null;
    }
    final String fileHash = HashUtils.sha256(filePath.toFile());
    
    final FileInfo fileInfo = FileInfo.builder()
                                      .type(AttestationType.UNKNOWN)
                                      .status(AttestationStatus.UNKNOWN)
                                      .filePath(filePath)
                                      .relativeFilePath(relativeFilePath)
                                      .lastModifiedTime(lastModifiedTime)
                                      .fileHash(fileHash)
                                      .build();
    log.debug("getFileInfo() end, fileInfo={}", fileInfo);
    return fileInfo;
  }
  
  @NonNull
  private FileInfo checkAddedAndModified(@NonNull final FileInfo fileInfo) {
    log.debug("checkAddedAndModified() start, fileInfo={}", fileInfo);
    final AttestationRecord previousRecord = attestationRecordService.findLastAttestedByRelativePath(
        fileInfo.getRelativeFilePath());
    if (previousRecord == null) {
      fileInfo.setType(AttestationType.ADDED);
    } else {
      fileInfo.setPreviousRecord(previousRecord);
      if (fileInfo.isModified()) {
        fileInfo.setType(AttestationType.MODIFIED);
      } else {
        fileInfo.setType(AttestationType.ATTESTED);
        fileInfo.setStatus(AttestationStatus.ATTESTED);
      }
    }
    log.debug("checkAddedAndModified() end, fileInfo={}", fileInfo);
    return fileInfo;
  }
  
  private AttestationRecord attest(@NonNull final FileInfo fileInfo) {
    log.debug("attest() start, fileInfo={}", fileInfo);
    final AttestationRecord attestationRecord = AttestationRecord.builder()
                                                                 .type(fileInfo.getType())
                                                                 .status(fileInfo.getStatus())
                                                                 .filePath(fileInfo.getFilePath())
                                                                 .relativeFilePath(fileInfo.getRelativeFilePath())
                                                                 .lastModifiedTime(fileInfo.getLastModifiedTime())
                                                                 .fileHash(fileInfo.getFileHash())
                                                                 .previousRecord(fileInfo.getPreviousRecord())
                                                                 .build();
    try {
      final ZonedDateTime attestTime = ZonedDateTime.now();
      attestationRecord.setAttestTime(attestTime);
      final Cmd cmd = Cmd.builder()
                         .type(attestationRecord.getType())
                         .fileName(attestationRecord.getFilePath()
                                                    .getFileName()
                                                    .toString())
                         .lastModifiedTime(attestationRecord.getLastModifiedTimeLong())
                         .fileHash(attestationRecord.getFileHash())
                         .timestamp(attestationRecord.getAttestTimeLong())
                         .description("")
                         .build();
      final LedgerInputResponse ledgerInputResponse = bnsClient.ledgerInput(gson.toJson(cmd));
      if (StatusConstantsString.OK.equalsIgnoreCase(ledgerInputResponse.getStatus())) {
        attestationRecord.setStatus(AttestationStatus.ATTESTED);
        attestationRecord.setAddress(ledgerInputResponse.getReceipt()
                                                        .getCallerAddress());
        attestationRecord.setClearanceOrder(ledgerInputResponse.getReceipt()
                                                               .getClearanceOrder());
        attestationRecord.setIndexValue(ledgerInputResponse.getReceipt()
                                                           .getIndexValue());
      } else {
        attestationRecord.setStatus(AttestationStatus.FAIL);
      }
    } catch (Exception e) {
      log.error("attest() error", e);
      attestationRecord.setStatus(AttestationStatus.FAIL);
    }
    log.debug("attest() end, attestationRecord={}", attestationRecord);
    return attestationRecord;
  }
  
  private void onAttested(@NonNull final AttestationRecord attestationRecord) {
    log.debug("onAttested() attestationRecord={}", attestationRecord);
    attestationRecordService.save(attestationRecord);
    try {
      callback.onAttested(attestationRecord);
    } catch (Exception e) {
      log.warn("onAttested() callback error", e);
    }
  }
  
  private void onAttestFail(@NonNull final AttestationRecord attestationRecord) {
    log.debug("onAttestFail() attestationRecord={}", attestationRecord);
    attestationRecordService.save(attestationRecord);
    try {
      callback.onAttestFail(attestationRecord);
    } catch (Exception e) {
      log.warn("onAttestFail() callback error", e);
    }
  }
  
}
