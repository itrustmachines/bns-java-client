package com.itrustmachines.bnsautofolderattest.vo;

public enum AttestationStatus {
  
  ATTESTED, //
  FAIL, //
  VERIFIED, //
  VERIFY_FAIL, //
  SAVE_PROOF, //
  SAVE_FAIL, //
  UNKNOWN, //
  ; //
  
  public boolean isAttested() {
    switch (this) {
      case ATTESTED:
      case VERIFIED:
      case SAVE_PROOF:
      case SAVE_FAIL:
      case VERIFY_FAIL:
        return true;
    }
    return false;
  }
  
}
