package com.dario.agenttrader;

import com.iggroup.webapi.samples.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);
    private IGClient igClient;

    public Application(){
        igClient = IGClient.getInstance();
    }

    public static void main(String[] args){
        LOG.info("Starting Dario Agent Trader...");
        if (args.length < 2) {
            LOG.error("Usage:- Application identifier password apikey");
            System.exit(-1);
        }
        PropertiesUtil.addProperty(IGClient.IDENTIFIER,args[0]);
        PropertiesUtil.addProperty(IGClient.PASSWORD,args[1]);
        PropertiesUtil.addProperty(IGClient.API_KEY,args[2]);



        Application application = new Application();

        try {
            application.run();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    public void run() throws Exception {
        igClient.connect();

        igClient.subscribeToLighstreamerAccountUpdates();

        igClient.listOpenPositions();

       Thread.sleep(20000);
        igClient.disconnect();
    }


}
