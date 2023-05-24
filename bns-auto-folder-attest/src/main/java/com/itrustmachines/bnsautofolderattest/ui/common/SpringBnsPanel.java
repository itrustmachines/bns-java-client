package com.itrustmachines.bnsautofolderattest.ui.common;

import javax.swing.JPanel;
import javax.swing.SpringLayout;

public abstract class SpringBnsPanel extends JPanel implements BnsPanel {
  
  protected final SpringLayout layout = new SpringLayout();
  
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
