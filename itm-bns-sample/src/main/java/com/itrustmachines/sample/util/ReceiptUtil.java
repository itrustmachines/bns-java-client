package com.itrustmachines.sample.util;

import com.itrustmachines.common.vo.Receipt;
import com.itrustmachines.sample.entity.ReceiptEntity;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ReceiptUtil {
  
  public Receipt toDomain(final @NonNull ReceiptEntity entity) {
    return Receipt.builder()
                  .callerAddress(entity.getCallerAddress())
                  .timestamp(entity.getTimestamp())
                  .cmd(entity.getCmd())
                  .indexValue(entity.getIndexValue())
                  .metadata(entity.getMetadata())
                  .clearanceOrder(entity.getClearanceOrder())
                  .timestampSPO(entity.getTimestampSPO())
                  .result(entity.getResult())
                  .sigClient(SpoSignatureUtil.toDomain(entity.getSigClient()))
                  .sigServer(SpoSignatureUtil.toDomain(entity.getSigServer()))
                  .build();
  }
  
  public ReceiptEntity toEntity(@NonNull final Receipt domain) {
    return ReceiptEntity.builder()
                        .callerAddress(domain.getCallerAddress())
                        .timestamp(domain.getTimestamp())
                        .cmd(domain.getCmd())
                        .indexValue(domain.getIndexValue())
                        .metadata(domain.getMetadata())
                        .clearanceOrder(domain.getClearanceOrder())
                        .timestampSPO(domain.getTimestampSPO())
                        .result(domain.getResult())
                        .sigClient(SpoSignatureUtil.toSigClientEntity(domain.getSigClient()))
                        .sigServer(SpoSignatureUtil.toSigServerEntity(domain.getSigServer()))
                        .build();
  }
}
