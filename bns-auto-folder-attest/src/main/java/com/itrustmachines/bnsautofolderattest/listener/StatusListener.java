package com.itrustmachines.bnsautofolderattest.listener;

import lombok.NonNull;

public interface StatusListener {
  
  void onStatus(@NonNull String status);
  
}
