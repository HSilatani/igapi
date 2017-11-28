package com.dario.agenttrader;


import com.iggroup.webapi.samples.PropertiesUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;


@SpringBootApplication(scanBasePackages = {"me.ramswaroop.jbot", "com.dario.agenttrader"})
public class JBotApplication extends SpringBootServletInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(JBotApplication.class);

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(JBotApplication.class);
    }
    /**
     * Entry point of the application. Run this method to start the sample bots,
     * but don't forget to add the correct tokens in application.properties file.
     *
     * @param args
     */
    public static void main(String[] args) {
       if (args.length < 3 && PropertiesUtil.getProperty(IGClient.IDENTIFIER)==null) {
            LOG.error("Usage:- Application identifier password apikey");
            System.exit(-1);
        }

        JBotApplication jbApp = new JBotApplication();
        try {
            IGClient igclient = jbApp.igClient();
            Runtime.getRuntime().addShutdownHook(new IGClientShutDownHook());
            SpringApplication app = new SpringApplication(JBotApplication.class);
            app.run(JBotApplication.class, args);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Bean
    public IGClient igClient() throws Exception {
        IGClient igClient = IGClient.getInstance();
        igClient.connect();
        return igClient;
    }

}
