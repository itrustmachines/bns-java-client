package com.itrustmachines.bnsautofolderattest.vo;

import lombok.NonNull;

public interface CsvWritable {
  
  @NonNull
  String getHeader();
  
  @NonNull
  String toCsvString();
  
}
