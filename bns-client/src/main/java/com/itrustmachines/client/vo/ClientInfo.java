package com.itrustmachines.client.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientInfo {
  
  private String address;
  
  private String role;
  
  private String email;
  
  private String name;
  
  private String country;
  
  private String company;
  
  private String jobTitle;
  
  private String website;
  
  private String emailNotification;
  
  private String bindingKey;
  
}
