package com.itrustmachines.bnsautofolderattest.ui.page;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SpringLayout;
import javax.swing.SwingWorker;

import com.itrustmachines.bnsautofolderattest.config.Config;
import com.itrustmachines.bnsautofolderattest.exception.CallbackException;
import com.itrustmachines.bnsautofolderattest.exception.ScanException;
import com.itrustmachines.bnsautofolderattest.listener.LogoutListener;
import com.itrustmachines.bnsautofolderattest.listener.StatusListener;
import com.itrustmachines.bnsautofolderattest.service.AttestService;
import com.itrustmachines.bnsautofolderattest.ui.common.SpringBnsPanel;
import com.itrustmachines.client.config.BnsClientConfig;
import com.itrustmachines.client.exception.BnsClientException;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InfoPanel extends SpringBnsPanel implements StatusListener {
  
  @Nullable
  private Config config;
  @Nullable
  private BnsClientConfig bnsClientConfig;
  @Nullable
  private AttestService service;
  private final List<StatusListener> listeners = new ArrayList<>();
  private final List<LogoutListener> logoutListeners = new ArrayList<>();
  private final JLabel statusCaptionLabel = new JLabel("Status");
  private final JLabel statusLabel = new JLabel("");
  private final JButton stopButton = new JButton("");
  private final JButton logoutButton = new JButton("Logout");
  @Nullable
  private SwingWorker<Void, Void> swingWorker;
  
  public InfoPanel() {
    create();
    
    onStatus("Stopped");
    
    log.debug("new instance={}", this);
  }
  
  @Override
  public void addComponent() {
    add(statusCaptionLabel);
    add(statusLabel);
    add(stopButton);
    add(logoutButton);
  }
  
  @Override
  public void setupComponent() {
    stopButton.addActionListener(e -> {
      if (stopButton.getText()
                    .equals("Stop")) {
        stop();
      } else {
        startAsync(config, bnsClientConfig);
      }
    });
    logoutButton.addActionListener(e -> logout());
  }
  
  @Override
  public void putConstraint() {
    layout.putConstraint(SpringLayout.NORTH, statusCaptionLabel, 6, SpringLayout.NORTH, this);
    layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, statusCaptionLabel, 6, SpringLayout.HORIZONTAL_CENTER, this);
    
    layout.putConstraint(SpringLayout.NORTH, statusLabel, 6, SpringLayout.SOUTH, statusCaptionLabel);
    layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, statusLabel, 6, SpringLayout.HORIZONTAL_CENTER, this);
    
    layout.putConstraint(SpringLayout.NORTH, stopButton, 6, SpringLayout.SOUTH, statusLabel);
    layout.putConstraint(SpringLayout.WEST, stopButton, 6, SpringLayout.WEST, this);
    layout.putConstraint(SpringLayout.EAST, stopButton, -6, SpringLayout.EAST, this);
    
    layout.putConstraint(SpringLayout.NORTH, logoutButton, 6, SpringLayout.SOUTH, stopButton);
    layout.putConstraint(SpringLayout.WEST, logoutButton, 6, SpringLayout.WEST, this);
    layout.putConstraint(SpringLayout.EAST, logoutButton, -6, SpringLayout.EAST, this);
  }
  
  public void addStatusListener(@NonNull final StatusListener... listeners) {
    Collections.addAll(this.listeners, listeners);
  }
  
  public void addLogoutListener(@NonNull final LogoutListener... listeners) {
    Collections.addAll(logoutListeners, listeners);
  }
  
  private void logout() {
    stop();
    for (final LogoutListener listener : logoutListeners) {
      listener.onLogout();
    }
  }
  
  public void startAsync(@Nullable final Config config, @Nullable final BnsClientConfig bnsClientConfig) {
    this.config = config;
    this.bnsClientConfig = bnsClientConfig;
    startAsync();
  }
  
  private void startAsync() {
    if (config == null || bnsClientConfig == null) {
      return;
    }
    this.swingWorker = new SwingWorker<>() {
      @Override
      protected Void doInBackground() throws Exception {
        stopButton.setText("Stop");
        onStatus("Initializing...");
        service = new AttestService(config, bnsClientConfig);
        
        // auto restart until interrupted
        while (!Thread.currentThread()
                      .isInterrupted()) {
          try {
            onStatus("Login...");
            service.getBnsClient()
                   .login();
            
            // process and delay seconds
            while (!Thread.currentThread()
                          .isInterrupted()) {
              
              onStatus("Scan and Attesting...");
              service.process();
              
              onStatus("Verifying...");
              service.getBnsClient()
                     .verifyNow();
              onStatus("Idle...");
              
              TimeUnit.SECONDS.sleep(config.getScanDelay());
            }
          } catch (BnsClientException | CallbackException | SQLException | ScanException e) {
            onStatus(String.format("Restarting...\n%s", e.getMessage()));
            log.error("main() error", e);
            log.info("main() restart after 60 seconds");
            TimeUnit.SECONDS.sleep(60);
          }
        }
        return null;
      }
      
      @Override
      protected void done() {
        stopButton.setText("Start");
        onStatus("Stopped");
      }
    };
    swingWorker.execute();
  }
  
  public void stop() {
    if (swingWorker != null) {
      swingWorker.cancel(true);
    }
  }
  
  @Override
  public void onStatus(@NonNull String status) {
    statusLabel.setText(status);
    for (StatusListener listener : listeners) {
      listener.onStatus(status);
    }
  }
}
