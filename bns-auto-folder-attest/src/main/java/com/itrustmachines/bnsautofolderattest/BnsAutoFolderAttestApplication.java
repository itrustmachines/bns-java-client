package com.itrustmachines.bnsautofolderattest;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JOptionPane;

import com.itrustmachines.bnsautofolderattest.ui.MainWindow;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BnsAutoFolderAttestApplication {
  
  public static final String[] PROPERTIES_PATH_LIST = new String[] { "./sample.properties",
      "./src/main/resources/sample.properties", "./bns-auto-folder-attest/src/main/resources/sample.properties" };
  
  public static final Path ITM_ENCRYPTED_PRIVATE_KEY = Paths.get(".itm.encrypted.private.key");
  
  public static void main(String[] args) {
    try {
      new MainWindow(PROPERTIES_PATH_LIST, ITM_ENCRYPTED_PRIVATE_KEY);
    } catch (Exception e) {
      log.error("main()", e);
      JOptionPane.showMessageDialog(null, e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
      System.exit(1);
    }
  }
}