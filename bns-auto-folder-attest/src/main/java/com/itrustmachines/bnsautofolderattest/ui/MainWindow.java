package com.itrustmachines.bnsautofolderattest.ui;

import java.awt.AWTException;
import java.awt.SystemTray;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import com.itrustmachines.bnsautofolderattest.exception.InitializationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainWindow extends JFrame {
  
  public MainWindow(String[] propertiesPathList, Path encryptedPrivateKeyPath)
      throws InitializationException, IOException, AWTException {
    super("BNS Auto Folder Attest");
    final BufferedImage image = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader()
                                                                              .getResource("itm.png")));
    
    final BnsTrayIcon trayIcon = new BnsTrayIcon(image);
    
    trayIcon.addOpenClickListener(e1 -> {
      setVisible(true);
      setExtendedState(JFrame.NORMAL);
    });
    
    SystemTray.getSystemTray()
              .add(trayIcon);
    
    final MainPanel mainPanel = new MainPanel(propertiesPathList, encryptedPrivateKeyPath);
    
    mainPanel.addStatusListener(trayIcon);
    
    add(mainPanel);
    
    // minimize to tray
    addWindowStateListener(e -> {
      if (e.getNewState() == ICONIFIED) {
        setVisible(false);
      }
      if (e.getNewState() == 7) {
        setVisible(false);
      }
      if (e.getNewState() == MAXIMIZED_BOTH) {
        setVisible(true);
      }
      if (e.getNewState() == NORMAL) {
        setVisible(true);
      }
    });
    
    setIconImage(image);
    setVisible(true);
    setSize(300, 200);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    log.debug("new instance={}", this);
  }
  
}
