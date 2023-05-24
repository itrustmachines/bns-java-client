package com.itrustmachines.bnsautofolderattest.ui.common;

import java.awt.CardLayout;

import javax.swing.JPanel;

public abstract class CardBnsPanel extends JPanel implements BnsPanel {
  
  protected final CardLayout layout = new CardLayout();
  
  protected void create() {
    setLayout(layout);
    addComponent();
    setupComponent();
    putConstraint();
  }
  
  @Override
  public void addComponent() {
    
  }
  
  @Override
  public void setupComponent() {
    
  }
  
  @Override
  public void putConstraint() {
    
  }
}
