package com.dario.agenttrader;

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
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IGClient {

    private static final Logger LOG = LoggerFactory.getLogger(IGClient.class);

    private Calculator cal = new Calculator();

    private final ApplicationBootStrapper applicationBootStrapper;
    AuthenticationResponseAndConversationContext authenticationContext = null;
    ArrayList<HandyTableListenerAdapter> listeners = new ArrayList<HandyTableListenerAdapter>();
    String tradeableEpic = null;
    AtomicBoolean receivedConfirm = new AtomicBoolean(false);
    AtomicBoolean receivedOPU = new AtomicBoolean(false);

    private StreamingAPI streamingAPI;
    private RestAPI restAPI;

    public IGClient(ApplicationBootStrapper abs) {
        this.applicationBootStrapper = abs;

        streamingAPI = applicationBootStrapper.streamingAPI();
        restAPI = applicationBootStrapper.restAPI();
    }

    void connect() throws Exception {
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

    void disconnect() throws Exception {
        unsubscribeAllLightstreamerListeners();
        streamingAPI.disconnect();
    }

    void unsubscribeAllLightstreamerListeners() throws Exception {

        for (HandyTableListenerAdapter listener : listeners) {
            streamingAPI.unsubscribe(listener.getSubscribedTableKey());
        }
    }

    void subscribeToLighstreamerAccountUpdates() throws Exception {

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
    
    public void listOpenPositions() throws Exception {

        ConversationContext conversationContext = authenticationContext.getConversationContext();
        GetPositionsV2Response positionsResponse = restAPI.getPositionsV2(conversationContext);
      LOG.info("Open positions: {}", positionsResponse.getPositions().size());
      for (PositionsItem position : positionsResponse.getPositions()) {
         GetPricesByNumberOfPointsV2Response prices=restAPI.getPricesByNumberOfPointsV2(conversationContext
                 ,"1",position.getMarket().getEpic(),"MINUTE");
         LOG.info(position.getMarket().getEpic() +
                 ", " + position.getMarket().getExpiry() +
                 ", " + position.getPosition().getDirection() +
                 ", " + position.getPosition().getSize()+
                 ":P&l = "+ cal.calPandL(position,prices));
      }

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