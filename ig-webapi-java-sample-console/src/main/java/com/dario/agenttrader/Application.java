package com.dario.agenttrader;

import com.iggroup.webapi.samples.client.rest.AuthenticationResponseAndConversationContext;
import com.iggroup.webapi.samples.client.rest.dto.session.createSessionV2.CreateSessionV2Request;
import com.iggroup.webapi.samples.client.streaming.HandyTableListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import com.lightstreamer.ls_client.UpdateInfo;

public class Application {
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    private ApplicationBootStrapper applicationBootStrapper;
    private AuthenticationResponseAndConversationContext authenticationContext = null;
    private ArrayList<HandyTableListenerAdapter> listeners = new ArrayList<HandyTableListenerAdapter>();

    private String tradeableEpic = null;

    private AtomicBoolean receivedConfirm = new AtomicBoolean(false);
    private AtomicBoolean receivedOPU = new AtomicBoolean(false);


    protected Application(ApplicationBootStrapper abs){
        applicationBootStrapper=abs;
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
       connect(this.applicationBootStrapper.identifier(),
               this.applicationBootStrapper.password(),
               this.applicationBootStrapper.apiKey());

       subscribeToLighstreamerAccountUpdates();

       Thread.sleep(20000);
       disconnect();
    }
    private void connect(String identifier, String password, String apiKey) throws Exception {
        LOG.info("Connecting as {}", identifier);

        boolean encrypt = Boolean.TRUE;

        CreateSessionV2Request authRequest = new CreateSessionV2Request();
        authRequest.setIdentifier(identifier);
        authRequest.setPassword(password);
        authRequest.setEncryptedPassword(encrypt);
        authenticationContext =
                applicationBootStrapper.restAPI().createSession(authRequest, apiKey, encrypt);
        applicationBootStrapper.streamingAPI().connect(
                authenticationContext.getAccountId(),
                authenticationContext.getConversationContext(),
                authenticationContext.getLightstreamerEndpoint());
    }

    private void disconnect() throws Exception {
        unsubscribeAllLightstreamerListeners();
        applicationBootStrapper.streamingAPI().disconnect();
    }

    private void unsubscribeAllLightstreamerListeners() throws Exception {

        for (HandyTableListenerAdapter listener : listeners) {
            applicationBootStrapper.streamingAPI().unsubscribe(listener.getSubscribedTableKey());
        }
    }

    private void subscribeToLighstreamerAccountUpdates() throws Exception {

      LOG.info("Subscribing to Lightstreamer account updates");
      listeners.add(applicationBootStrapper.streamingAPI()
              .subscribeForAccountBalanceInfo(
                      authenticationContext.getAccountId(),
                      new HandyTableListenerAdapter() {
         @Override
         public void onUpdate(int i, String s, UpdateInfo updateInfo) {
            LOG.info("Account balance info = " + updateInfo);
         }
      }));

   }

}
