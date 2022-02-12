package com.itrustmachines.bnsautofolderattest.vo;

import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScanResult {
  
  public ZonedDateTime startTime;
  
  public long totalCount;
  
  public long totalBytes;
  
  public long addedCount;
  
  public long modifiedCount;
  
  public long attestedCount;
  
  public ZonedDateTime endTime;
  
}
