package com.example.demo.strategy;

import com.example.demo.ai.AIClient.TradingSignal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AISignalStrategy implements Strategy {

    private static final Logger logger = LoggerFactory.getLogger(AISignalStrategy.class);
    
    @Value("${trading.amount:0.001}")
    private double tradingAmount;
    
    private final Map<String, Object> parameters = new ConcurrentHashMap<>();

    @Override
    public TradingDecision getTradingDecision(String symbol, Map<String, Object> marketData, TradingSignal aiSignal) throws Exception {
        logger.info("Processing trading decision for symbol: {}, AI Signal: {}", symbol, aiSignal);
        
        // 获取当前价格
        double currentPrice = getCurrentPrice(marketData);
        
        switch (aiSignal) {
            case BUY:
                return new TradingDecision(
                        TradingDecision.Action.BUY,
                        currentPrice,
                        tradingAmount,
                        "AI Signal: BUY"
                );
            case SELL:
                return new TradingDecision(
                        TradingDecision.Action.SELL,
                        currentPrice,
                        tradingAmount,
                        "AI Signal: SELL"
                );
            default:
                return new TradingDecision(
                        TradingDecision.Action.HOLD,
                        currentPrice,
                        0,
                        "AI Signal: HOLD"
                );
        }
    }

    @Override
    public String getName() {
        return "AI_SIGNAL_STRATEGY";
    }

    @Override
    public void setParameter(String key, Object value) {
        parameters.put(key, value);
        if ("tradingAmount".equals(key) && value instanceof Number) {
            this.tradingAmount = ((Number) value).doubleValue();
        }
    }

    /**
     * 从市场数据中获取当前价格
     */
    private double getCurrentPrice(Map<String, Object> marketData) {
        try {
            // 假设marketData的结构类似于: {"data": {"ticker": {"last": "45000.0"}}}
            if (marketData.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) marketData.get("data");
                if (data.containsKey("ticker")) {
                    Map<String, Object> ticker = (Map<String, Object>) data.get("ticker");
                    if (ticker.containsKey("last")) {
                        Object lastPrice = ticker.get("last");
                        if (lastPrice instanceof String) {
                            return Double.parseDouble((String) lastPrice);
                        } else if (lastPrice instanceof Number) {
                            return ((Number) lastPrice).doubleValue();
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error extracting current price from market data", e);
        }
        return 0;
    }
}