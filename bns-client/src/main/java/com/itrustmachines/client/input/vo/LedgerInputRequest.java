package com.itrustmachines.client.input.vo;

import java.io.Serializable;

import org.apache.commons.lang3.SerializationUtils;

import com.itrustmachines.common.util.SignatureUtil;
import com.itrustmachines.common.vo.SpoSignature;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class LedgerInputRequest implements Serializable, Cloneable {
  
  private String callerAddress;
  private String timestamp;
  private String cmd;
  private String indexValue;
  private String metadata;
  private Long clearanceOrder;
  private SpoSignature sigClient;
  
  public LedgerInputRequest clone() {
    return SerializationUtils.clone(this);
  }
  
  public String toSignData() {
    final StringBuilder builder = new StringBuilder();
    builder.append(this.getCallerAddress())
           .append(this.getTimestamp())
           .append(this.getCmd())
           .append(this.getIndexValue())
           .append(this.getMetadata())
           .append(this.getClearanceOrder());
    return builder.toString();
  }
  
  public LedgerInputRequest sign(@NonNull final String privateKey) {
    LedgerInputRequest result;
    try {
      result = clone();
      result.setSigClient(SignatureUtil.signEthereumMessage(privateKey, toSignData()));
    } catch (Exception e) {
      final String errMsg = String.format("sign() error, ledgerInputRequest=%s", this);
      log.error(errMsg, e);
      throw new RuntimeException(errMsg);
    }
    return result;
  }
  
}
