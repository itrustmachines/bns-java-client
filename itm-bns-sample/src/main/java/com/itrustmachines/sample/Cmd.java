package com.itrustmachines.sample;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Cmd內容使用者可自定義, 相關規範請參照使用流程 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Cmd {

  // 使用Sample callback傳送資料至Dashboard
  // 必須包含deviceId及timestamp兩項資訊
  String deviceId;
  Long timestamp;
  Double watt;
  
}
