package com.dario.agenttrader;

import com.iggroup.webapi.samples.PropertiesUtil;
import com.iggroup.webapi.samples.client.RestAPI;
import com.iggroup.webapi.samples.client.StreamingAPI;
import com.iggroup.webapi.samples.client.rest.AuthenticationResponseAndConversationContext;
import com.iggroup.webapi.samples.client.rest.ConversationContext;
import com.iggroup.webapi.samples.client.rest.dto.markets.getMarketDetailsV2.CurrenciesItem;
import com.iggroup.webapi.samples.client.rest.dto.markets.getMarketDetailsV2.GetMarketDetailsV2Response;
import com.iggroup.webapi.samples.client.rest.dto.markets.getMarketDetailsV2.MarketOrderPreference;
import com.iggroup.webapi.samples.client.rest.dto.positions.getPositionsV2.GetPositionsV2Response;
import com.iggroup.webapi.samples.client.rest.dto.positions.getPositionsV2.PositionsItem;
import com.iggroup.webapi.samples.client.rest.dto.positions.otc.closeOTCPositionV1.CloseOTCPositionV1Request;
import com.iggroup.webapi.samples.client.rest.dto.positions.otc.closeOTCPositionV1.TimeInForce;
import com.iggroup.webapi.samples.client.rest.dto.positions.otc.createOTCPositionV1.CreateOTCPositionV1Request;
import com.iggroup.webapi.samples.client.rest.dto.positions.otc.createOTCPositionV1.Direction;
import com.iggroup.webapi.samples.client.rest.dto.positions.otc.createOTCPositionV1.OrderType;
import com.iggroup.webapi.samples.client.rest.dto.prices.getPricesByDateRangeV2.GetPricesByDateRangeV2Response;
import com.iggroup.webapi.samples.client.rest.dto.prices.getPricesByNumberOfPointsV2.GetPricesByNumberOfPointsV2Response;
import com.iggroup.webapi.samples.client.rest.dto.session.createSessionV2.CreateSessionV2Request;
import com.iggroup.webapi.samples.client.rest.dto.watchlists.getWatchlistByWatchlistIdV1.GetWatchlistByWatchlistIdV1Response;
import com.iggroup.webapi.samples.client.rest.dto.watchlists.getWatchlistByWatchlistIdV1.MarketStatus;
import com.iggroup.webapi.samples.client.rest.dto.watchlists.getWatchlistByWatchlistIdV1.MarketsItem;
import com.iggroup.webapi.samples.client.rest.dto.watchlists.getWatchlistsV1.GetWatchlistsV1Response;
import com.iggroup.webapi.samples.client.rest.dto.watchlists.getWatchlistsV1.WatchlistsItem;
import com.iggroup.webapi.samples.client.streaming.HandyTableListenerAdapter;
import com.lightstreamer.ls_client.UpdateInfo;


import java.util.ArrayList;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IGClient {
    private static IGClient OneAndOnlyIGClient = new IGClient();

    public static IGClient getInstance(){
        return OneAndOnlyIGClient;
    }

    private static final Logger LOG = LoggerFactory.getLogger(IGClient.class);
    public static final String IDENTIFIER = "identifier";
    public static final String PASSWORD = "password";
    public static final String API_KEY = "apiKey";

    private Calculator cal = new Calculator();

    private final ApplicationBootStrapper applicationBootStrapper;
    AuthenticationResponseAndConversationContext authenticationContext = null;
    ArrayList<HandyTableListenerAdapter> listeners = new ArrayList<HandyTableListenerAdapter>();
    String tradeableEpic = null;
    AtomicBoolean receivedConfirm = new AtomicBoolean(false);
    AtomicBoolean receivedOPU = new AtomicBoolean(false);

    private StreamingAPI streamingAPI;
    private RestAPI restAPI;



    private IGClient(){
        String[] args = new String[3];
        args[0] = PropertiesUtil.getProperty(IDENTIFIER);
        args[1] = PropertiesUtil.getProperty(PASSWORD);
        args[2] = PropertiesUtil.getProperty(API_KEY);

        this.applicationBootStrapper = new ApplicationBootStrapper(args);
        init();
    }

    private void init() {
        streamingAPI = applicationBootStrapper.streamingAPI();
        restAPI = applicationBootStrapper.restAPI();

    }

    public void connect() throws Exception {
       String identifier = applicationBootStrapper.identifier();
       String password = applicationBootStrapper.password();
       String apiKey = applicationBootStrapper.apiKey();

        LOG.info("Connecting as {}", identifier);

        boolean encrypt = Boolean.TRUE;

        CreateSessionV2Request authRequest = new CreateSessionV2Request();
        authRequest.setIdentifier(identifier);
        authRequest.setPassword(password);
        authRequest.setEncryptedPassword(encrypt);
        authenticationContext =
                restAPI.createSession(authRequest, apiKey, encrypt);
        streamingAPI.connect(
                authenticationContext.getAccountId(),
                authenticationContext.getConversationContext(),
                authenticationContext.getLightstreamerEndpoint());
    }

    public void disconnect() throws Exception {
        unsubscribeAllLightstreamerListeners();
        streamingAPI.disconnect();
    }

    public void unsubscribeAllLightstreamerListeners() throws Exception {

        for (HandyTableListenerAdapter listener : listeners) {
            streamingAPI.unsubscribe(listener.getSubscribedTableKey());
        }
    }

    public void subscribeToLighstreamerAccountUpdates() throws Exception {

        LOG.info("Subscribing to Lightstreamer account updates");
        listeners.add(streamingAPI
                .subscribeForAccountBalanceInfo(
                        authenticationContext.getAccountId(),
                        new HandyTableListenerAdapter() {
                            @Override
                            public void onUpdate(int i, String s, UpdateInfo updateInfo) {
                                LOG.info("Account balance info = " + updateInfo);
                            }
                        }));

    }
    
    public String listOpenPositions() throws Exception {

        StringJoiner positionsStr = new StringJoiner("\n\n");
        positionsStr.add("Ammo amir");
        ConversationContext conversationContext = authenticationContext.getConversationContext();
        GetPositionsV2Response positionsResponse = restAPI.getPositionsV2(conversationContext);
      LOG.info("Open positions Amoo Amir: {}", positionsResponse.getPositions().size());
      for (PositionsItem position : positionsResponse.getPositions()) {
         GetPricesByNumberOfPointsV2Response prices=restAPI.getPricesByNumberOfPointsV2(conversationContext
                 ,"1",position.getMarket().getEpic(),"MINUTE");

         StringJoiner positionStr = new StringJoiner("|","","");
         String name = position.getMarket().getInstrumentName();
         positionStr.add(name.substring(0, Math.min(name.length(), 3)));
         positionStr.add(""+position.getPosition().getDirection());
         positionStr.add(""+position.getPosition().getSize());
         String pl = cal.calPandLString(position,prices);
         int idx = pl.indexOf(45);
         pl= ((idx>=0)?"#FF0000"+pl:pl);
         positionStr.add(""+ pl);
         positionsStr.add(positionStr.toString());
      }
      LOG.info(positionsStr.toString());
      return positionsStr.toString();

   }

   public void listWatchlists() throws Exception {

      GetWatchlistsV1Response watchlistsResponse = restAPI.getWatchlistsV1(authenticationContext.getConversationContext());
      LOG.info("Watchlists: {}", watchlistsResponse.getWatchlists().size());
      for (WatchlistsItem watchlist : watchlistsResponse.getWatchlists()) {
         LOG.info(watchlist.getName() + " : ");
         GetWatchlistByWatchlistIdV1Response watchlistInstrumentsResponse = restAPI.getWatchlistByWatchlistIdV1(authenticationContext.getConversationContext(), watchlist.getId());
         for (MarketsItem market : watchlistInstrumentsResponse.getMarkets()) {
            LOG.info(market.getEpic());
            if (market.getStreamingPricesAvailable() && market.getMarketStatus() == MarketStatus.TRADEABLE) {
               tradeableEpic = market.getEpic();
            }
         }
      }
   }

}