package com.itrustmachines.bnsautofolderattest.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itrustmachines.bnsautofolderattest.bns.service.BnsClientReceiptDaoImpl;
import com.itrustmachines.bnsautofolderattest.callback.Callback;
import com.itrustmachines.bnsautofolderattest.config.Config;
import com.itrustmachines.bnsautofolderattest.exception.CallbackException;
import com.itrustmachines.bnsautofolderattest.exception.InitializationException;
import com.itrustmachines.bnsautofolderattest.exception.ScanException;
import com.itrustmachines.bnsautofolderattest.vo.*;
import com.itrustmachines.client.BnsClient;
import com.itrustmachines.client.account.vo.RegisterRequest;
import com.itrustmachines.client.account.vo.RegisterToBindingRequest;
import com.itrustmachines.client.config.BnsClientConfig;
import com.itrustmachines.client.exception.BnsClientException;
import com.itrustmachines.client.input.vo.LedgerInputRequest;
import com.itrustmachines.client.input.vo.LedgerInputResponse;
import com.itrustmachines.client.todo.BnsClientCallback;
import com.itrustmachines.client.verify.vo.DoneClearanceOrderEvent;
import com.itrustmachines.client.vo.ClientInfo;
import com.itrustmachines.client.vo.ReceiptEvent;
import com.itrustmachines.common.util.HashUtils;
import com.itrustmachines.common.vo.MerkleProof;
import com.itrustmachines.common.vo.Receipt;
import com.itrustmachines.common.vo.ReceiptLocator;
import com.itrustmachines.verification.vo.VerificationProof;
import com.itrustmachines.verification.vo.VerifyReceiptAndMerkleProofResult;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@ToString(exclude = { "gson", "bnsClient" })
@Slf4j
public class AttestService implements BnsClientCallback {
  
  private final Config config;
  @NonNull
  private final Path rootFolderPath;
  @NonNull
  private final AttestationRecordService attestationRecordService;
  @NonNull
  private final Callback callback;
  @Getter(AccessLevel.PUBLIC)
  @NonNull
  private final BnsClient bnsClient;
  @NonNull
  private final Gson gson;
  
  public AttestService(@NonNull final Config config, BnsClientConfig bnsClientConfig)
      throws InitializationException, SQLException {
    this.config = config;
    this.rootFolderPath = config.getRootFolderPath();
    this.attestationRecordService = new AttestationRecordService(config);
    this.callback = new CallbackImpl(config.getHistoryCsvPath(), config.getScanHistoryCsvPath());
    try {
      this.bnsClient = BnsClient.init(bnsClientConfig, this, new BnsClientReceiptDaoImpl(config.getJdbcUrl()));
    } catch (BnsClientException | SQLException e) {
      throw new InitializationException(e);
    }
    this.gson = new GsonBuilder().disableHtmlEscaping()
                                 .create();
    
    log.info("new instance={}", this);
  }
  
  public void process() throws CallbackException, SQLException, ScanException {
    log.debug("process() start");
    
    final List<FileInfo> toAttestFileList = scan();
    
    if (!toAttestFileList.isEmpty()) {
      attestAll(toAttestFileList);
    }
    
    log.debug("process() end");
  }
  
  @NonNull
  public List<FileInfo> scan() throws CallbackException, SQLException, ScanException {
    log.debug("scan() start, rootFolderPath={}", rootFolderPath);
    final ScanResult scanResult = new ScanResult();
    scanResult.setStartTime(ZonedDateTime.now());
    
    final List<Path> filePathList;
    try (final Stream<Path> pathStream = Files.walk(rootFolderPath)) {
      filePathList = pathStream.filter(path -> !Files.isDirectory(path))
                               .collect(Collectors.toList());
    } catch (IOException e) {
      throw new ScanException(e);
    }
    
    final List<FileInfo> fileInfoList = new ArrayList<>();
    for (final Path p : filePathList) {
      final FileInfo fileInfo = getFileInfo(p);
      fileInfoList.add(fileInfo);
    }
    log.debug("scan() fileInfoList.size={}", fileInfoList.size());
    
    computeScanResult(fileInfoList, scanResult);
    scanResult.setEndTime(ZonedDateTime.now());
    callback.onScanResult(scanResult);
    
    final List<FileInfo> toAttestFileInfoList = fileInfoList.stream()
                                                            .filter(fileInfo -> AttestationType.isNeedAttestation(
                                                                fileInfo.getType()))
                                                            .collect(Collectors.toList());
    log.debug("scan() toAttestFileInfoList.size={}", toAttestFileInfoList.size());
    return toAttestFileInfoList;
  }
  
  private static void computeScanResult(@NonNull final List<FileInfo> fileInfoList,
      @NonNull final ScanResult scanResult) throws ScanException {
    int totalCount = fileInfoList.size();
    long totalBytes = 0;
    long addedCount = 0;
    long modifiedCount = 0;
    long attestedCount = 0;
    long unknownCount = 0;
    for (FileInfo fileInfo : fileInfoList) {
      try {
        totalBytes += Files.size(fileInfo.getFilePath());
      } catch (IOException e) {
        throw new ScanException(e);
      }
      switch (fileInfo.getType()) {
        case ADDED:
          addedCount++;
          break;
        case MODIFIED:
          modifiedCount++;
          break;
        case ATTESTED:
          attestedCount++;
          break;
        case UNKNOWN:
        default:
          unknownCount++;
          break;
      }
    }
    
    scanResult.setTotalCount(totalCount);
    scanResult.setTotalBytes(totalBytes);
    scanResult.setAddedCount(addedCount);
    scanResult.setModifiedCount(modifiedCount);
    scanResult.setAttestedCount(attestedCount);
    log.info("computeScanResult() end, total={}, totalBytes={}, added={}, modified={}, attested={}, unknown={}",
        totalCount, totalBytes, addedCount, modifiedCount, attestedCount, unknownCount);
  }
  
  public void attestAll(@NonNull final List<FileInfo> toAttestFileInfoList) throws CallbackException, SQLException {
    log.debug("attestAll() start, toAttestFileInfoList.size={}", toAttestFileInfoList.size());
    long attestedCount = 0;
    long attestFailCount = 0;
    long unknownCount = 0;
    for (FileInfo fileInfo : toAttestFileInfoList) {
      final AttestationRecord attestationRecord = attest(fileInfo);
      switch (attestationRecord.getStatus()) {
        case ATTESTED:
          attestationRecordService.save(attestationRecord);
          callback.onAttested(attestationRecord);
          attestedCount++;
          break;
        case FAIL:
          attestationRecordService.save(attestationRecord);
          callback.onAttestFail(attestationRecord);
          attestFailCount++;
          break;
        case UNKNOWN:
          attestationRecordService.save(attestationRecord);
          callback.onAttestFail(attestationRecord);
          unknownCount++;
          break;
      }
    }
    
    log.info("attestAll() end, attested={}, attestFail={}, unknown={}", attestedCount, attestFailCount, unknownCount);
  }
  
  @NonNull
  private FileInfo getFileInfo(@NonNull final Path filePath) throws ScanException, SQLException {
    log.debug("getFileInfo() start, filePath={}", filePath);
    final FileInfo fileInfo = new FileInfo();
    fileInfo.setFilePath(filePath);
    fileInfo.setRelativeFilePath(rootFolderPath.relativize(filePath));
    try {
      fileInfo.setLastModifiedTime(Files.getLastModifiedTime(filePath)
                                        .toInstant()
                                        .atZone(ZoneId.systemDefault())
                                        .truncatedTo(ChronoUnit.MILLIS));
    } catch (IOException e) {
      throw new ScanException(e);
    }
    fileInfo.setFileHash(HashUtils.sha256(filePath.toFile()));
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
    log.debug("getFileInfo() end, fileInfo={}", fileInfo);
    return fileInfo;
  }
  
  @NonNull
  private AttestationRecord attest(@NonNull final FileInfo fileInfo) {
    log.debug("attest() start, fileInfo={}", fileInfo);
    final Cmd cmd = Cmd.of(fileInfo);
    final AttestationRecord attestationRecord = AttestationRecord.of(fileInfo, cmd);
    try {
      final LedgerInputResponse ledgerInputResponse = bnsClient.ledgerInput(gson.toJson(cmd));
      attestationRecord.updateByReceipt(ledgerInputResponse.getReceipt());
    } catch (BnsClientException e) {
      log.error("attest() error", e);
      attestationRecord.setStatus(AttestationStatus.FAIL);
    }
    log.debug("attest() end, attestationRecord={}", attestationRecord);
    return attestationRecord;
  }
  
  @Override
  public void register(RegisterRequest registerRequest, ClientInfo clientInfo) {
    
  }
  
  @Override
  public void registerToBinding(RegisterToBindingRequest registerRequest, ClientInfo clientInfo) {
    
  }
  
  @Override
  public void createLedgerInputByCmd(ReceiptLocator receiptLocator, LedgerInputRequest ledgerInputRequest) {
    
  }
  
  @Override
  public void obtainLedgerInputResponse(ReceiptLocator locator, String cmdJson,
      LedgerInputResponse ledgerInputResponse) {
    
  }
  
  @Override
  public void obtainReceiptEvent(ReceiptEvent receiptEvent) {
    
  }
  
  @Override
  public void obtainDoneClearanceOrderEvent(DoneClearanceOrderEvent doneClearanceOrderEvent) {
    
  }
  
  @Override
  public void obtainMerkleProof(ReceiptLocator receiptLocator, MerkleProof merkleProof) {
    
  }
  
  @SneakyThrows({ CallbackException.class, SQLException.class })
  @Override
  public void getVerifyReceiptResult(Receipt receipt, MerkleProof merkleProof,
      VerifyReceiptAndMerkleProofResult verifyReceiptAndMerkleProofResult) {
    final AttestationRecord attestationRecord = attestationRecordService.findLastByCOAndIVAndStatus(
        receipt.getClearanceOrder(), receipt.getIndexValue(), AttestationStatus.ATTESTED);
    if (attestationRecord != null) {
      if (attestationRecord.getPreviousRecord() != null) {
        attestationRecord.setPreviousRecord(attestationRecordService.findById(attestationRecord.getPreviousRecord()
                                                                                               .getId()));
      }
      handleVerifyResult(verifyReceiptAndMerkleProofResult, attestationRecord);
      if (verifyReceiptAndMerkleProofResult.isPass() && config.isEnableDownloadVerificationProof()) {
        handleSaveProof(attestationRecord);
      }
    } else {
      log.error("onVerified() attestationRecord not found");
    }
  }
  
  private void handleVerifyResult(@NonNull final VerifyReceiptAndMerkleProofResult verifyReceiptAndMerkleProofResult,
      @NonNull final AttestationRecord attestationRecord) throws CallbackException, SQLException {
    attestationRecord.setStatus(
        verifyReceiptAndMerkleProofResult.isPass() ? AttestationStatus.VERIFIED : AttestationStatus.VERIFY_FAIL);
    attestationRecordService.save(attestationRecord);
    if (verifyReceiptAndMerkleProofResult.isPass()) {
      callback.onVerified(attestationRecord);
    } else {
      callback.onVerifyFail(attestationRecord);
    }
  }
  
  private void handleSaveProof(@NonNull final AttestationRecord attestationRecord)
      throws CallbackException, SQLException {
    try {
      final VerificationProof verificationProof = bnsClient.getVerificationProof(attestationRecord.getClearanceOrder(),
          attestationRecord.getIndexValue());
      String proofName;
      Path proofPath;
      int i = 0;
      do {
        proofName = String.format("%s_%d_%s%s.itm", attestationRecord.getFilePath()
                                                                     .getFileName()
                                                                     .toString(),
            attestationRecord.getClearanceOrder(), attestationRecord.getIndexValue(), i == 0 ? "" : " (" + i + ")");
        proofPath = Paths.get(config.getVerificationProofDownloadPath()
                                    .toString(),
            Optional.ofNullable(attestationRecord.getRelativeFilePath()
                                                 .getParent())
                    .map(Path::toString)
                    .orElse(""),
            proofName);
        i++;
      } while (Files.exists(proofPath));
      Files.createDirectories(proofPath.getParent());
      Files.write(proofPath, gson.toJson(verificationProof)
                                 .getBytes(StandardCharsets.UTF_8));
      attestationRecord.setStatus(AttestationStatus.SAVE_PROOF);
      attestationRecord.setProofPath(proofPath);
      attestationRecordService.save(attestationRecord);
      callback.onSaveProof(attestationRecord);
    } catch (BnsClientException | IOException e) {
      log.error("getVerifyReceiptResult() error", e);
      attestationRecord.setStatus(AttestationStatus.SAVE_FAIL);
      attestationRecordService.save(attestationRecord);
      callback.onSaveFail(attestationRecord);
    }
  }
  
}
