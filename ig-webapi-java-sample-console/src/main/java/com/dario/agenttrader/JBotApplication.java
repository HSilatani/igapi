package com.dario.agenttrader;


import com.iggroup.webapi.samples.PropertiesUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SpringBootApplication(scanBasePackages = {"me.ramswaroop.jbot", "com.dario.agenttrader"})
public class JBotApplication {

    private static final Logger LOG = LoggerFactory.getLogger(JBotApplication.class);

    /**
     * Entry point of the application. Run this method to start the sample bots,
     * but don't forget to add the correct tokens in application.properties file.
     *
     * @param args
     */
    public static void main(String[] args) {
       if (args.length < 3) {
            LOG.error("Usage:- Application identifier password apikey");
            System.exit(-1);
        }
        PropertiesUtil.addProperty(IGClient.IDENTIFIER,args[0]);
        PropertiesUtil.addProperty(IGClient.PASSWORD,args[1]);
        PropertiesUtil.addProperty(IGClient.API_KEY,args[2]);
        PropertiesUtil.addProperty(SlackBot.SLACK_BOT_TOKEN,args[3]);
        IGClient igClient = IGClient.getInstance();
        try {
            igClient.connect();
            Runtime.getRuntime().addShutdownHook(new IGClientShutDownHook());
            SpringApplication app = new SpringApplication(JBotApplication.class);

            app.run(JBotApplication.class, args);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
