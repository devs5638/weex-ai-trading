package com.trading.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiConfig {
    

    // 交易配置
    @Value("${trading.symbol:cmt_dogeusdt}")
//    @Value("${trading.symbol:cmt_btcusdt}")
    private String tradingSymbol;
    

    @Value("${trading.strategy:aiSignal}")
    private String tradingStrategy;

    @Value("${trading.interval:10000}")
    private long tradingInterval;


    public double getTradingVolume() {
        return tradingVolume;
    }

    private int tradingVolume = 3600*12;  // dogeusdt
//    private double tradingVolume = 0.08;  // btcusdt


    public String getTradingSymbol() {
        return tradingSymbol;
    }

    public void setTradingSymbol(String tradingSymbol) {
        this.tradingSymbol = tradingSymbol;
    }

    public void setTradingVolume(double tradingVolume) {
        this.tradingVolume = (int) tradingVolume;
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