package com.dario.agenttrader;

public class IGClientShutDownHook extends Thread {
             @Override
         public void run() {
             try {
                 IGClient.getInstance().disconnect();
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
}
