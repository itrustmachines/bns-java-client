package com.itrustmachines.bnsautofolderattest.vo;

import java.time.ZonedDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class ScanResult implements CsvWritable {
  
  public ZonedDateTime startTime;
  
  public long totalCount;
  
  public long totalBytes;
  
  public long addedCount;
  
  public long modifiedCount;
  
  public long attestedCount;
  
  public ZonedDateTime endTime;
  
  @NonNull
  public String getHeader() {
    return "Start Time" + "," // 1
        + "Total Count" + "," // 2
        + "Total Bytes" + "," // 3
        + "Added Count" + "," // 4
        + "Modified Count" + "," // 5
        + "Attested Count" + "," // 6
        + "End Time" + "," // 7
        + "\n";
  }
  
  @Override
  @NonNull
  public String toCsvString() {
    return startTime + "," // 1
        + totalCount + "," // 2
        + totalBytes + "," // 3
        + addedCount + "," // 4
        + modifiedCount + "," // 5
        + attestedCount + "," // 6
        + endTime + "," // 7
        + "\n";
  }
}
