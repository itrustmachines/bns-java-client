package com.itrustmachines.bnsautofolderattest.ui.page;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.SpringLayout;

import org.apache.commons.lang3.StringUtils;
import org.web3j.crypto.Credentials;

import com.itrustmachines.bnsautofolderattest.listener.PrivateKeyListener;
import com.itrustmachines.bnsautofolderattest.ui.common.SpringBnsPanel;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnterPrivateKeyPanel extends SpringBnsPanel {
  
  private final List<PrivateKeyListener> listeners = new ArrayList<>();
  private final JLabel privateKeyLabel = new JLabel("Enter PrivateKey");
  private final JPasswordField privateKeyField = new JPasswordField();
  private final JLabel errorLabel = new JLabel("");
  private final JButton okButton = new JButton("OK");
  
  @Override
  public void addComponent() {
    add(privateKeyLabel);
    add(privateKeyField);
    add(errorLabel);
    add(okButton);
  }
  
  @Override
  public void setupComponent() {
    okButton.addActionListener(event -> onOkClick());
    
    privateKeyField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          onOkClick();
        }
      }
    });
    
  }
  
  @Override
  public void putConstraint() {
    layout.putConstraint(SpringLayout.NORTH, privateKeyLabel, 6, SpringLayout.NORTH, this);
    layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, privateKeyLabel, 6, SpringLayout.HORIZONTAL_CENTER, this);
    
    layout.putConstraint(SpringLayout.NORTH, privateKeyField, 6, SpringLayout.SOUTH, privateKeyLabel);
    layout.putConstraint(SpringLayout.WEST, privateKeyField, 6, SpringLayout.WEST, this);
    layout.putConstraint(SpringLayout.EAST, privateKeyField, -6, SpringLayout.EAST, this);
    
    layout.putConstraint(SpringLayout.NORTH, errorLabel, 6, SpringLayout.SOUTH, privateKeyField);
    layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, errorLabel, 6, SpringLayout.HORIZONTAL_CENTER, this);
    
    layout.putConstraint(SpringLayout.NORTH, okButton, 6, SpringLayout.SOUTH, errorLabel);
    layout.putConstraint(SpringLayout.WEST, okButton, 6, SpringLayout.WEST, this);
    layout.putConstraint(SpringLayout.EAST, okButton, -6, SpringLayout.EAST, this);
    
  }
  
  public EnterPrivateKeyPanel() {
    create();
    
    log.debug("new instance={}", this);
  }
  
  @Override
  public void setVisible(boolean aFlag) {
    super.setVisible(aFlag);
    privateKeyField.requestFocus();
  }
  
  public void addPrivateKeyListener(@NonNull final PrivateKeyListener... listeners) {
    Collections.addAll(this.listeners, listeners);
  }
  
  private void onOkClick() {
    final String privateKey = new String(privateKeyField.getPassword());
    if (StringUtils.isBlank(privateKey)) {
      errorLabel.setText("please input private key");
    }
    try {
      Credentials.create(privateKey);
      for (PrivateKeyListener listener : listeners) {
        listener.onPrivateKey(privateKey);
      }
      privateKeyField.setText("");
    } catch (NumberFormatException e) {
      log.error("Invalid private Key format");
      errorLabel.setText("invalid private Key format");
    }
  }
}
