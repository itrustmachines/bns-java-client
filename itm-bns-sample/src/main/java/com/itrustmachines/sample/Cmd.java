package com.itrustmachines.sample;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Cmd {

  String deviceId;
  Long timestamp;
  Double watt;
  
}
