package com.itrustmachines.bnsautofolderattest.vo;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.j256.ormlite.field.DatabaseField;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttestationRecord {
  
  public static final String ID_KEY = "id";
  public static final String RELATIVE_FILE_PATH_KEY = "relativeFilePath";
  public static final String STATUS_KEY = "status";
  public static final String ATTEST_TIME_KEY = "attestTime";
  public static final String CLEARANCE_ORDER_KEY = "clearanceOrder";
  public static final String INDEX_VALUE_KEY = "indexValue";
  public static final String VERIFY_STATUS_KEY = "verifyStatus";
  
  @DatabaseField(generatedId = true, columnName = ID_KEY)
  private Long id;
  
  @DatabaseField(columnName = "type", canBeNull = false, unknownEnumName = "UNKNOWN")
  private AttestationType type;
  
  @DatabaseField(columnName = STATUS_KEY, canBeNull = false, unknownEnumName = "UNKNOWN")
  private AttestationStatus status;
  
  private Path filePath;
  
  private Path relativeFilePath;
  
  private ZonedDateTime lastModifiedTime;
  
  @DatabaseField(columnName = "fileHash", canBeNull = false)
  private String fileHash;
  
  @ToString.Exclude
  @DatabaseField(columnName = "previousRecord", foreign = true)
  private AttestationRecord previousRecord;
  
  @DatabaseField(columnName = "address", canBeNull = false)
  private String address;
  
  private ZonedDateTime attestTime;
  
  @DatabaseField(columnName = CLEARANCE_ORDER_KEY, canBeNull = false)
  private Long clearanceOrder;
  
  @DatabaseField(columnName = INDEX_VALUE_KEY, canBeNull = false)
  private String indexValue;
  
  private Path proofPath;
  
  // Additional column for db
  @DatabaseField(columnName = "filePath", canBeNull = false, useGetSet = true)
  private String filePathStr;
  
  public String getFilePathStr() {
    if (filePath == null) {
      return null;
    }
    return filePath.toString();
  }
  
  public void setFilePathStr(String filePathStr) {
    if (filePathStr == null) {
      return;
    }
    filePath = Paths.get(filePathStr);
  }
  
  @DatabaseField(columnName = RELATIVE_FILE_PATH_KEY, canBeNull = false, useGetSet = true)
  private String relativeFilePathStr;
  
  public String getRelativeFilePathStr() {
    if (relativeFilePath == null) {
      return null;
    }
    return relativeFilePath.toString();
  }
  
  public void setRelativeFilePathStr(String relativeFilePathStr) {
    if (relativeFilePathStr == null) {
      return;
    }
    relativeFilePath = Paths.get(relativeFilePathStr);
  }
  
  @DatabaseField(columnName = "lastModifiedTime", canBeNull = false, useGetSet = true)
  private Long lastModifiedTimeLong;
  
  public Long getLastModifiedTimeLong() {
    if (lastModifiedTime == null) {
      return null;
    }
    return lastModifiedTime.toInstant()
                           .toEpochMilli();
  }
  
  public void setLastModifiedTimeLong(Long lastModifiedTimeLong) {
    if (lastModifiedTimeLong == null) {
      return;
    }
    lastModifiedTime = Instant.ofEpochMilli(lastModifiedTimeLong)
                              .atZone(ZoneId.systemDefault());
  }
  
  @DatabaseField(columnName = ATTEST_TIME_KEY, canBeNull = false, useGetSet = true)
  private Long attestTimeLong;
  
  public Long getAttestTimeLong() {
    if (attestTime == null) {
      return null;
    }
    return attestTime.toInstant()
                     .toEpochMilli();
  }
  
  public void setAttestTimeLong(Long attestTimeLong) {
    if (attestTimeLong == null) {
      return;
    }
    attestTime = Instant.ofEpochMilli(attestTimeLong)
                        .atZone(ZoneId.systemDefault());
  }
  
  @DatabaseField(columnName = "proofPath", useGetSet = true)
  private String proofPathStr;
  
  public String getProofPathStr() {
    if (proofPath == null) {
      return null;
    }
    return proofPath.toString();
  }
  
  public void setProofPathStr(String filePathStr) {
    if (filePathStr == null) {
      return;
    }
    proofPath = Paths.get(filePathStr);
  }
  
}
