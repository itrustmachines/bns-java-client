package com.itrustmachines.bnsautofolderattest.ui;

import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.itrustmachines.bnsautofolderattest.exception.InitializationException;
import com.itrustmachines.bnsautofolderattest.listener.StatusListener;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BnsTrayIcon extends TrayIcon implements StatusListener {
  
  private final MenuItem statusItem;
  private final MenuItem openItem;
  
  public BnsTrayIcon(Image image) throws InitializationException {
    super(image, "BNS Auto Folder Attest");
    
    if (!SystemTray.isSupported()) {
      throw new InitializationException("SystemTray is not supported");
    }
    
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      
      final URL resource = getClass().getClassLoader()
                                     .getResource("itm.png");
      if (resource == null) {
        throw new InitializationException();
      }
      final PopupMenu popup = new PopupMenu();
      
      final MenuItem statusCaptionItem = new MenuItem("Status");
      this.statusItem = new MenuItem("");
      this.openItem = new MenuItem("Open");
      final MenuItem exitItem = new MenuItem("Exit");
      
      exitItem.addActionListener(e1 -> System.exit(0));
      
      popup.add(statusCaptionItem);
      popup.add(statusItem);
      popup.addSeparator();
      popup.add(openItem);
      popup.add(exitItem);
      
      setImageAutoSize(true);
      setPopupMenu(popup);
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
        | UnsupportedLookAndFeelException e) {
      throw new InitializationException(e);
    }
    
    log.debug("new instance={}", this);
  }
  
  public void addOpenClickListener(ActionListener listener) {
    openItem.addActionListener(listener);
  }
  
  @Override
  public void onStatus(@NonNull String status) {
    statusItem.setLabel("> " + status);
  }
}
