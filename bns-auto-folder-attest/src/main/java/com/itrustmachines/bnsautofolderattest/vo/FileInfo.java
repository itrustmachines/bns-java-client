package com.itrustmachines.bnsautofolderattest.vo;

import java.nio.file.Path;
import java.time.ZonedDateTime;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class FileInfo {
  
  private AttestationType type = AttestationType.UNKNOWN;
  private AttestationStatus status = AttestationStatus.UNKNOWN;
  private Path filePath;
  private Path relativeFilePath;
  private ZonedDateTime lastModifiedTime;
  private String fileHash;
  private AttestationRecord previousRecord;
  
  public boolean isModified() {
    return isModified(previousRecord);
  }
  
  public boolean isModified(@NonNull final AttestationRecord attestationRecord) {
    final boolean lastModifiedTimeNotEqual = !attestationRecord.getLastModifiedTime()
                                                               .isEqual(lastModifiedTime);
    if (lastModifiedTimeNotEqual) {
      return true;
    }
    return !StringUtils.equalsIgnoreCase(attestationRecord.getFileHash(), fileHash);
  }
  
}
