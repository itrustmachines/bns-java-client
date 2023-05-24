package com.itrustmachines.bnsautofolderattest.ui.page;

import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SpringLayout;

import com.itrustmachines.bnsautofolderattest.ui.common.SpringBnsPanel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WelcomePanel extends SpringBnsPanel {
  private final JLabel welcomeLabel = new JLabel("BNS Auto Folder Attest");
  private final JButton nextButton = new JButton("Next");
  
  public WelcomePanel() {
    create();
    log.debug("new instance={}", this);
  }
  
  @Override
  public void addComponent() {
    add(welcomeLabel);
    add(nextButton);
  }
  
  @Override
  public void putConstraint() {
    layout.putConstraint(SpringLayout.NORTH, welcomeLabel, 6, SpringLayout.NORTH, this);
    layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, welcomeLabel, 6, SpringLayout.HORIZONTAL_CENTER, this);
    
    layout.putConstraint(SpringLayout.NORTH, nextButton, 6, SpringLayout.SOUTH, welcomeLabel);
    layout.putConstraint(SpringLayout.WEST, nextButton, 6, SpringLayout.WEST, this);
    layout.putConstraint(SpringLayout.EAST, nextButton, -6, SpringLayout.EAST, this);
  }
  
  public void addOnNextClickListener(ActionListener nextButtonActionListener) {
    nextButton.addActionListener(nextButtonActionListener);
  }
  
}
