package com.itrustmachines.bnsautofolderattest.callback;

import com.itrustmachines.bnsautofolderattest.exception.CallbackException;
import com.itrustmachines.bnsautofolderattest.vo.AttestationRecord;
import com.itrustmachines.bnsautofolderattest.vo.ScanResult;

import lombok.NonNull;

public interface Callback {
  
  void onScanResult(@NonNull ScanResult scanResult) throws CallbackException;
  
  void onAttested(@NonNull AttestationRecord attestationRecord) throws CallbackException;
  
  void onAttestFail(@NonNull AttestationRecord attestationRecord) throws CallbackException;
  
  void onVerified(@NonNull AttestationRecord attestationRecord) throws CallbackException;
  
  void onVerifyFail(@NonNull AttestationRecord attestationRecord) throws CallbackException;
  
  void onSaveProof(@NonNull AttestationRecord attestationRecord) throws CallbackException;
  
  void onSaveFail(@NonNull AttestationRecord attestationRecord) throws CallbackException;
  
}
