package com.trading.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiConfig {
    

    // 交易配置
    @Value("${trading.symbol:cmt_btcusdt}")
    private String tradingSymbol;
    

    @Value("${trading.strategy:aiSignal}")
    private String tradingStrategy;

    @Value("${trading.interval:60000}")
    private long tradingInterval;


    public String getTradingSymbol() {
        return tradingSymbol;
    }

    public String getWeexTradingSymbol() {
        return tradingSymbol;
    }
    public long getTradingInterval() {
        return tradingInterval;
    }

    public long getWeexTradingInterval() {
        return tradingInterval;
    }

    public String getTradingStrategy() {
        return tradingStrategy;
    }

    public String getWeexTradingStrategy() {
        return tradingStrategy;
    }
}