package com.itrustmachines.bnsautofolderattest.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class Cmd {
  
  private AttestationType type;
  private String fileName; // required for bns front-end
  private Long lastModifiedTime;
  private String fileHash;
  private Long timestamp;
  private String description; // required for bns front-end
  
  @NonNull
  public static Cmd of(@NonNull final FileInfo fileInfo) {
    final Cmd cmd = new Cmd();
    cmd.setType(fileInfo.getType());
    cmd.setFileName(fileInfo.getFilePath()
                            .getFileName()
                            .toString());
    cmd.setLastModifiedTime(fileInfo.getLastModifiedTime()
                                    .toInstant()
                                    .toEpochMilli());
    cmd.setFileHash(fileInfo.getFileHash());
    cmd.setTimestamp(System.currentTimeMillis());
    cmd.setDescription(fileInfo.getFilePath()
                               .toAbsolutePath()
                               .toString());
    return cmd;
  }
  
}
