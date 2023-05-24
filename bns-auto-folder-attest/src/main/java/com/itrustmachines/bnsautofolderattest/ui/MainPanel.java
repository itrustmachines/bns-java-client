package com.itrustmachines.bnsautofolderattest.ui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.JOptionPane;

import com.itrustmachines.bnsautofolderattest.config.Config;
import com.itrustmachines.bnsautofolderattest.exception.InitializationException;
import com.itrustmachines.bnsautofolderattest.exception.InputException;
import com.itrustmachines.bnsautofolderattest.listener.LogoutListener;
import com.itrustmachines.bnsautofolderattest.listener.PinCodeListener;
import com.itrustmachines.bnsautofolderattest.listener.PrivateKeyListener;
import com.itrustmachines.bnsautofolderattest.listener.StatusListener;
import com.itrustmachines.bnsautofolderattest.ui.common.CardBnsPanel;
import com.itrustmachines.bnsautofolderattest.ui.page.EnterPinCodePanel;
import com.itrustmachines.bnsautofolderattest.ui.page.EnterPrivateKeyPanel;
import com.itrustmachines.bnsautofolderattest.ui.page.InfoPanel;
import com.itrustmachines.bnsautofolderattest.ui.page.WelcomePanel;
import com.itrustmachines.bnsautofolderattest.util.AESUtil;
import com.itrustmachines.bnsautofolderattest.util.FileUtil;
import com.itrustmachines.client.config.BnsClientConfig;
import com.itrustmachines.client.exception.BnsClientException;
import com.itrustmachines.common.util.HashUtils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainPanel extends CardBnsPanel implements PrivateKeyListener, PinCodeListener, LogoutListener {
  
  private static final String WELCOME_PANEL = "WELCOME_PANEL";
  private static final String ENTER_PIN_CODE_PANEL = "ENTER_PIN_CODE_PANEL";
  private static final String ENTER_PRIVATE_KEY_PANEL = "ENTER_PRIVATE_KEY_PANEL";
  private static final String CREATE_PIN_CODE_PANEL = "CREATE_PIN_CODE_PANEL";
  private static final String INFO_PANEL = "INFO_PANEL";
  
  private final String[] propertiesPathList;
  private final Path encryptedPrivateKeyPath;
  @Nullable
  private Config config = null;
  @Nullable
  private BnsClientConfig bnsClientConfig = null;
  
  private final WelcomePanel welcomePanel = new WelcomePanel();
  private final EnterPinCodePanel enterPinCodePanel = new EnterPinCodePanel(false);
  private final EnterPrivateKeyPanel privateKeyPanel = new EnterPrivateKeyPanel();
  private final EnterPinCodePanel createPinCodePanel = new EnterPinCodePanel(true);
  private final InfoPanel infoPanel = new InfoPanel();
  
  public MainPanel(String[] propertiesPathList, Path encryptedPrivateKeyPath) {
    this.propertiesPathList = propertiesPathList;
    this.encryptedPrivateKeyPath = encryptedPrivateKeyPath;
    create();
    layout.show(this, WELCOME_PANEL);
    
    log.debug("new instance={}", this);
  }
  
  @Override
  public void addComponent() {
    add(welcomePanel, WELCOME_PANEL);
    add(infoPanel, INFO_PANEL);
    add(enterPinCodePanel, ENTER_PIN_CODE_PANEL);
    add(privateKeyPanel, ENTER_PRIVATE_KEY_PANEL);
    add(createPinCodePanel, CREATE_PIN_CODE_PANEL);
  }
  
  @Override
  public void setupComponent() {
    welcomePanel.addOnNextClickListener(e -> start());
    infoPanel.addLogoutListener(this);
    enterPinCodePanel.addPinCodeListener(this::decryptPrivateKey);
    privateKeyPanel.addPrivateKeyListener(this);
    createPinCodePanel.addPinCodeListener(this);
  }
  
  public void addStatusListener(@NonNull final StatusListener... listeners) {
    infoPanel.addStatusListener(listeners);
  }
  
  private void start() {
    try {
      final String configPath = FileUtil.findFile(propertiesPathList);
      log.info("start() configPath={}", configPath);
      
      config = Config.load(configPath);
      log.info("start() config={}", config);
      
      bnsClientConfig = BnsClientConfig.load(configPath);
      log.info("start() bnsClientConfig={}", bnsClientConfig);
      
      if (Files.exists(encryptedPrivateKeyPath)) {
        layout.show(this, ENTER_PIN_CODE_PANEL);
      } else {
        layout.show(this, ENTER_PRIVATE_KEY_PANEL);
      }
    } catch (InitializationException | BnsClientException e) {
      JOptionPane.showMessageDialog(this, e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
      System.exit(1);
    }
  }
  
  private void decryptPrivateKey(String pinCode) throws InputException {
    if (bnsClientConfig == null) {
      layout.show(this, WELCOME_PANEL);
    }
    try {
      final List<String> fileContent = Files.readAllLines(encryptedPrivateKeyPath, StandardCharsets.UTF_8);
      
      final String pinCodeHash = HashUtils.sha256(pinCode)
                                          .toLowerCase(Locale.ROOT);
      if (!pinCodeHash.equalsIgnoreCase(fileContent.get(0))) {
        throw new InputException("invalid pin code");
      }
      bnsClientConfig.setPrivateKey(AESUtil.decrypt(fileContent.get(1), pinCode));
      layout.show(MainPanel.this, INFO_PANEL);
      infoPanel.startAsync(config, bnsClientConfig);
    } catch (InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException | IllegalBlockSizeException
        | BadPaddingException | IOException e) {
      throw new InputException(e);
    }
  }
  
  @Override
  public void onPrivateKey(@NonNull final String privateKey) {
    if (bnsClientConfig == null) {
      layout.show(this, WELCOME_PANEL);
    }
    bnsClientConfig.setPrivateKey(privateKey);
    layout.show(MainPanel.this, CREATE_PIN_CODE_PANEL);
  }
  
  @Override
  public void onPinCode(@NonNull final String pinCode) throws InputException {
    if (bnsClientConfig == null || bnsClientConfig.getPrivateKey() == null) {
      layout.show(this, WELCOME_PANEL);
    }
    final String pinCodeHash = HashUtils.sha256(pinCode)
                                        .toLowerCase(Locale.ROOT);
    final List<String> fileContent;
    try {
      fileContent = List.of(pinCodeHash, AESUtil.encrypt(bnsClientConfig.getPrivateKey(), pinCode));
      Files.write(encryptedPrivateKeyPath, fileContent, StandardCharsets.UTF_8);
      layout.show(MainPanel.this, INFO_PANEL);
      infoPanel.startAsync(config, bnsClientConfig);
    } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException
        | BadPaddingException | IOException e) {
      throw new InputException(e);
    }
  }
  
  @Override
  public void onLogout() {
    layout.show(this, WELCOME_PANEL);
  }
}
