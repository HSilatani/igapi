package com.dario.agenttrader;

import com.iggroup.webapi.samples.client.rest.dto.positions.getPositionsV2.Direction;
import com.iggroup.webapi.samples.client.rest.dto.positions.getPositionsV2.PositionsItem;
import com.iggroup.webapi.samples.client.rest.dto.prices.getPricesByNumberOfPointsV2.GetPricesByNumberOfPointsV2Response;

import java.math.BigDecimal;
import java.text.NumberFormat;

public class Calculator {

    public BigDecimal calPandL(PositionsItem position, GetPricesByNumberOfPointsV2Response prices) throws Exception {
        BigDecimal openlevel = position.getPosition().getLevel();
        Direction direction = position.getPosition().getDirection();
        BigDecimal size = position.getPosition().getSize();

        BigDecimal priceDiff = calculatePriceDifference(direction,openlevel ,prices);

        BigDecimal pAndL= priceDiff.multiply(size);


        return  pAndL;
    }

    public String calPandLString(PositionsItem position, GetPricesByNumberOfPointsV2Response prices) throws Exception {

        return NumberFormat.getCurrencyInstance().format(calPandL(position,prices));
    }

    private BigDecimal calculatePriceDifference(Direction direction,
                                               BigDecimal openLevel,
                                               GetPricesByNumberOfPointsV2Response prices) throws Exception {

        if(direction.name().equalsIgnoreCase("buy")){
            return prices.getPrices().get(0).getClosePrice().getBid().subtract(openLevel);

        }else if(direction.name().equalsIgnoreCase("sell")){
            return openLevel.subtract(prices.getPrices().get(0).getClosePrice().getAsk());

        }
        throw new Exception("Price not found");
    }
}
