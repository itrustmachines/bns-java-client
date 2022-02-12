package com.itrustmachines.bnsautofolderattest.bns.util;

import com.itrustmachines.bnsautofolderattest.bns.entity.SigClientEntity;
import com.itrustmachines.bnsautofolderattest.bns.entity.SigServerEntity;
import com.itrustmachines.common.vo.SpoSignature;

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
