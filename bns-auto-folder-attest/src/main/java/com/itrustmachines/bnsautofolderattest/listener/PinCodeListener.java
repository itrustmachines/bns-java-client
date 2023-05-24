package com.itrustmachines.bnsautofolderattest.listener;

import com.itrustmachines.bnsautofolderattest.exception.InputException;

import lombok.NonNull;

public interface PinCodeListener {
  
  void onPinCode(@NonNull String pinCode) throws InputException;
  
}
