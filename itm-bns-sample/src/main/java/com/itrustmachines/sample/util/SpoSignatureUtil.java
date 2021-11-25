package com.itrustmachines.sample.util;

import com.itrustmachines.common.vo.SpoSignature;
import com.itrustmachines.sample.entity.SigClientEntity;
import com.itrustmachines.sample.entity.SigServerEntity;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SpoSignatureUtil {
  
  public SpoSignature toDomain(@NonNull final SigClientEntity entity) {
    return SpoSignature.builder()
                       .r(entity.getR())
                       .s(entity.getS())
                       .v(entity.getV())
                       .build();
  }
  
  public SpoSignature toDomain(@NonNull final SigServerEntity entity) {
    return SpoSignature.builder()
                       .r(entity.getR())
                       .s(entity.getS())
                       .v(entity.getV())
                       .build();
  }
  
  public SigClientEntity toSigClientEntity(@NonNull final SpoSignature spoSignature) {
    return SigClientEntity.builder()
                          .r(spoSignature.getR())
                          .s(spoSignature.getS())
                          .v(spoSignature.getV())
                          .build();
  }
  
  public SigServerEntity toSigServerEntity(@NonNull final SpoSignature spoSignature) {
    return SigServerEntity.builder()
                          .r(spoSignature.getR())
                          .s(spoSignature.getS())
                          .v(spoSignature.getV())
                          .build();
  }
  
}
