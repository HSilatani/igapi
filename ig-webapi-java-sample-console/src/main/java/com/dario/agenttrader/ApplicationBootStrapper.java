package com.dario.agenttrader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iggroup.webapi.samples.PropertiesUtil;
import com.iggroup.webapi.samples.client.RestAPI;
import com.iggroup.webapi.samples.client.StreamingAPI;
import com.iggroup.webapi.samples.client.rest.AuthenticationService;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Properties;

public class ApplicationBootStrapper {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationBootStrapper.class);
    public static final String IG_API_DARK_CLUSTER = "ig.api.dark.cluster";
    public static final String IG_API_DARK_CLUSTER_QUERY_PARAMETER = "ig.api.dark.cluster.query.parameter";

    private ObjectMapper objectMapper;

    private RestAPI restAPI;

    private StreamingAPI streamingAPI;

    private HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory;

    private RestTemplate restTemplate;

    private AuthenticationService authenticationService;

    private Properties properties = PropertiesUtil.getProperties();

    public static String IG_ENV_KEY = "ig.api.domain.URL";

    private boolean igApiDarkCluster=false;
    private String  darkClusterQueryParameter;
    private String identifier;
    private String password;
    private String apiKey;

    public ApplicationBootStrapper(String[] args){
        if (args.length < 2) {
            LOG.error("Usage:- Application identifier password apikey");
            System.exit(-1);
        }

        logInitialisationParameters(args);
        identifier = args[0];
        password = args[1];
        apiKey = args[2];

        initialise();
    }

    private void initialise() {
        igApiDarkCluster = Boolean.valueOf(
                properties.getProperty(IG_API_DARK_CLUSTER,"false")).booleanValue();
        darkClusterQueryParameter = properties.getProperty(IG_API_DARK_CLUSTER_QUERY_PARAMETER,
                "deal_cluster");
        objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        httpComponentsClientHttpRequestFactory=new HttpComponentsClientHttpRequestFactory();
        restTemplate = new RestTemplate(requestFactory());
        restAPI = new RestAPI(
                                objectMapper,
                                restTemplate,
                                httpClient(),
                                igApiDomainURL(),
                                getIgApiDarkCluster(),
                                getDarkClusterQueryParameter());
        authenticationService = new AuthenticationService(restAPI,
                                objectMapper,
                                restTemplate,
                                httpClient(),
                                igApiDomainURL(),
                                getIgApiDarkCluster(),
                                getDarkClusterQueryParameter());

        restAPI.setAuthenticationService(authenticationService);

        streamingAPI = new StreamingAPI(restAPI);
    }

    private void logInitialisationParameters(String[] args) {
        LOG.info("Initializing with these parameters..");
        LOG.info("identifier=" + args[0]);
        LOG.info("password supplied =" + !args[1].isEmpty());
        LOG.info("apiKey=" + args[2]);
        LOG.info("IG Envinronment=" + igApiDomainURL());
    }

    public String igApiDomainURL() {
        return properties.getProperty(IG_ENV_KEY );
    }

    public boolean getIgApiDarkCluster(){
        return igApiDarkCluster;
    }

    public String getDarkClusterQueryParameter(){
        return darkClusterQueryParameter;
    }




    public ObjectMapper objectMapper(){
        return objectMapper;
    }

    public RestAPI restAPI(){

        return restAPI;
    }

    public StreamingAPI streamingAPI() {
        return streamingAPI;
    }

    public HttpComponentsClientHttpRequestFactory requestFactory(){
        return httpComponentsClientHttpRequestFactory;
    }

    public HttpClient httpClient(){
        return requestFactory().getHttpClient();
    }
    public HttpClient httpClient2() {
        return HttpClients.createDefault();
    }

    public RestTemplate restTemplate(){
        return restTemplate;
    }

    public String identifier() {
        return identifier;
    }

    public String password() {
        return password;
    }

    public String apiKey() {
        return  apiKey;
    }
}
