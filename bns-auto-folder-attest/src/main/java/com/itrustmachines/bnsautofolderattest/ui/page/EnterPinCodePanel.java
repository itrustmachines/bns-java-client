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

import com.itrustmachines.bnsautofolderattest.exception.InputException;
import com.itrustmachines.bnsautofolderattest.listener.PinCodeListener;
import com.itrustmachines.bnsautofolderattest.ui.common.SpringBnsPanel;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnterPinCodePanel extends SpringBnsPanel {
  
  private final boolean repeat;
  private final List<PinCodeListener> listeners = new ArrayList<>();
  private final JLabel pinCodeLabel = new JLabel("Enter Pin Code");
  private final JPasswordField pinCodeField = new JPasswordField();
  private final JLabel repeatPinCodeLabel = new JLabel("Repeat Pin Code");
  private final JPasswordField repeatPinCodeField = new JPasswordField();
  private final JLabel errorLabel = new JLabel("");
  private final JButton okButton = new JButton("OK");
  
  public EnterPinCodePanel(boolean repeat) {
    this.repeat = repeat;
    create();
    
    log.debug("new instance={}", this);
  }
  
  @Override
  public void addComponent() {
    add(pinCodeLabel);
    add(pinCodeField);
    if (repeat) {
      add(repeatPinCodeLabel);
      add(repeatPinCodeField);
    }
    add(errorLabel);
    add(okButton);
  }
  
  @Override
  public void setupComponent() {
    if (repeat) {
      pinCodeField.addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
          if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            repeatPinCodeField.requestFocus();
          }
        }
      });
      repeatPinCodeField.addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
          if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            onOkClick();
          }
        }
      });
    } else {
      pinCodeField.addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
          if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            onOkClick();
          }
        }
      });
    }
    
    okButton.addActionListener(event -> onOkClick());
  }
  
  @Override
  public void putConstraint() {
    layout.putConstraint(SpringLayout.NORTH, pinCodeLabel, 6, SpringLayout.NORTH, this);
    layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, pinCodeLabel, 6, SpringLayout.HORIZONTAL_CENTER, this);
    
    layout.putConstraint(SpringLayout.NORTH, pinCodeField, 6, SpringLayout.SOUTH, pinCodeLabel);
    layout.putConstraint(SpringLayout.WEST, pinCodeField, 6, SpringLayout.WEST, this);
    layout.putConstraint(SpringLayout.EAST, pinCodeField, -6, SpringLayout.EAST, this);
    
    if (repeat) {
      layout.putConstraint(SpringLayout.NORTH, repeatPinCodeLabel, 6, SpringLayout.SOUTH, pinCodeField);
      layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, repeatPinCodeLabel, 6, SpringLayout.HORIZONTAL_CENTER, this);
      
      layout.putConstraint(SpringLayout.NORTH, repeatPinCodeField, 6, SpringLayout.SOUTH, repeatPinCodeLabel);
      layout.putConstraint(SpringLayout.WEST, repeatPinCodeField, 6, SpringLayout.WEST, this);
      layout.putConstraint(SpringLayout.EAST, repeatPinCodeField, -6, SpringLayout.EAST, this);
    }
    
    layout.putConstraint(SpringLayout.NORTH, errorLabel, 6, SpringLayout.SOUTH,
        repeat ? repeatPinCodeField : pinCodeField);
    layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, errorLabel, 6, SpringLayout.HORIZONTAL_CENTER, this);
    
    layout.putConstraint(SpringLayout.NORTH, okButton, 6, SpringLayout.SOUTH, errorLabel);
    layout.putConstraint(SpringLayout.WEST, okButton, 6, SpringLayout.WEST, this);
    layout.putConstraint(SpringLayout.EAST, okButton, -6, SpringLayout.EAST, this);
  }
  
  @Override
  public void setVisible(boolean aFlag) {
    super.setVisible(aFlag);
    pinCodeField.requestFocus();
  }
  
  public void addPinCodeListener(@NonNull final PinCodeListener... listeners) {
    Collections.addAll(this.listeners, listeners);
  }
  
  private void onOkClick() {
    final String pinCode = new String(pinCodeField.getPassword());
    if (repeat) {
      final String repeatPinCode = new String(repeatPinCodeField.getPassword());
      if (!StringUtils.equals(pinCode, repeatPinCode)) {
        errorLabel.setText("Pin Code not match");
      }
    }
    try {
      for (PinCodeListener listener : listeners) {
        listener.onPinCode(pinCode);
      }
      pinCodeField.setText("");
      repeatPinCodeField.setText("");
    } catch (InputException e) {
      errorLabel.setText(e.getMessage());
    }
  }
  
}
