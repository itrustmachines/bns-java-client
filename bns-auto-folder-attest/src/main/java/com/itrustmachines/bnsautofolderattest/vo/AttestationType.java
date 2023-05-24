package com.itrustmachines.bnsautofolderattest.vo;

public enum AttestationType {
  
  ADDED, //
  MODIFIED, //
  ATTESTED, //
  UNKNOWN, //
  ;
  
  public static boolean isNeedAttestation(final AttestationType type) {
    switch (type) {
      case ADDED:
      case MODIFIED:
        return true;
      case ATTESTED:
      case UNKNOWN:
      default:
        return false;
    }
  }
}
