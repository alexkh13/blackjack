/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.gui.utils;

import javafx.application.Platform;
import javafx.scene.control.ProgressIndicator;

/**
 *
 * @author idmlogic
 */
public class ProgressTimeout {
    private final ProgressIndicator pi;
    private Thread runner;
    
    public ProgressTimeout(ProgressIndicator pi) {
        this.pi = pi;
    }
    
    public void start(int timeout) {
        if(runner != null && runner.isAlive()) {
            runner.interrupt();
        }
        runner = new Thread(new TimeoutRunner(timeout));
        runner.start();
    }
    
    public void stop() {
        if(runner != null) {
            runner.interrupt();
        }
    }
    
    private class TimeoutRunner implements Runnable {

        final int timeout;
        TimeoutRunner(int timeout) {
            this.timeout = timeout;
        }
        
        @Override
        public void run() {
            boolean running = true;
            pi.setProgress(0);
            while(timeout > 0 && running && pi.getProgress() < 1) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        double current = pi.getProgress();
                        double sec = 1;
                        double add = sec / timeout;
                        pi.setProgress(current + add);
                    }
                });
                try {
                    Thread.sleep(1000);
                } 
                catch (InterruptedException ex) {
                    running = false;
                }
            }
        }
        
    }
}
