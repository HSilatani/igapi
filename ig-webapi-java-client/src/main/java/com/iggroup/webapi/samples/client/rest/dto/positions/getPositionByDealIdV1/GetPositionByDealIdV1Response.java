package com.iggroup.webapi.samples.client.rest.dto.positions.getPositionByDealIdV1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/*
Open position data
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetPositionByDealIdV1Response {

/*
Open position data
*/
private Position position;

/*
Market data
*/
private Market market;

public Position getPosition() { return position; }
public void setPosition(Position position) { this.position=position; }
public Market getMarket() { return market; }
public void setMarket(Market market) { this.market=market; }
}
