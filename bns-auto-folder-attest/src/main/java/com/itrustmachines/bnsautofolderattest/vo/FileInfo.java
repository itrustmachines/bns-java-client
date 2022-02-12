package com.itrustmachines.bnsautofolderattest.vo;

import java.nio.file.Path;
import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileInfo {
  
  private AttestationType type;
  private AttestationStatus status;
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
    final boolean fileHashNotEqual = !attestationRecord.getFileHash()
                                                       .equalsIgnoreCase(fileHash);
    if (fileHashNotEqual) {
      return true;
    }
    return false;
  }
  
}
