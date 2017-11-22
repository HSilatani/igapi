package com.dario.agenttrader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);
    private final com.dario.agenttrader.IGClient IGClient;

    private ApplicationBootStrapper applicationBootStrapper;


    protected Application(ApplicationBootStrapper abs){
        applicationBootStrapper=abs;
        IGClient = new IGClient(abs);
    }

    public static void main(String args[]){
        LOG.info("Starting Dario Agent Trader...");

        ApplicationBootStrapper applicationBootStrapper = new ApplicationBootStrapper(args);

        Application application = new Application(applicationBootStrapper);

        try {
            application.run();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    public void run() throws Exception {
        IGClient.connect();

        IGClient.subscribeToLighstreamerAccountUpdates();

        IGClient.listOpenPositions();

       Thread.sleep(20000);
        IGClient.disconnect();
    }


}
