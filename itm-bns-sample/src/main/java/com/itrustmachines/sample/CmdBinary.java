package com.itrustmachines.sample;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CmdBinary {

    String deviceId;
    Long timestamp;
    String fileName;
    String fileHash;
}
