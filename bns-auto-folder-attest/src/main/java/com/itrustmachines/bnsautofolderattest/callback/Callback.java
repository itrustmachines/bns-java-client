package com.itrustmachines.bnsautofolderattest.callback;

import com.itrustmachines.bnsautofolderattest.vo.AttestationRecord;
import com.itrustmachines.bnsautofolderattest.vo.ScanResult;

import lombok.NonNull;

public interface Callback {
  
  void onScanResult(@NonNull ScanResult scanResult);
  
  void onAttested(@NonNull AttestationRecord attestationRecord);
  
  void onAttestFail(@NonNull AttestationRecord attestationRecord);
  
  void onVerified(@NonNull AttestationRecord attestationRecord);
  
  void onVerifyFail(@NonNull AttestationRecord attestationRecord);
  
  void onSaveProof(@NonNull AttestationRecord attestationRecord);
  
  void onSaveFail(@NonNull AttestationRecord attestationRecord);
  
}
