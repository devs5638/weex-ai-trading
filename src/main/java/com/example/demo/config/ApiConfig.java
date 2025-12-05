package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiConfig {
    
    // Weex交易所API配置
    @Value("${weex.api.url:https://api.weex.com/v1}")
    private String weexApiUrl;
    
    @Value("${weex.api.key:your-weex-api-key}")
    private String weexApiKey;
    
    @Value("${weex.api.secret:your-weex-api-secret}")
    private String weexApiSecret;
    
    // DeepSeek AI API配置
    @Value("${deepseek.api.url:https://api.deepseek.com/v1}")
    private String deepseekApiUrl;
    
    @Value("${deepseek.api.key:your-deepseek-api-key}")
    private String deepseekApiKey;
    
    // 交易配置
    @Value("${trading.symbol:BTCUSDT}")
    private String tradingSymbol;
    
    @Value("${trading.amount:0.001}")
    private double tradingAmount;
    
    @Value("${trading.interval:60000}")
    private long tradingInterval;

    @Value("${weex.api.passphrase:your-weex-api-passphrase}")
    private String weexApiPassphrase;

    @Value("${trading.strategy:aiSignal}")
    private String tradingStrategy;

    public String getWeexApiUrl() {
        return weexApiUrl;
    }

    public String getWeexApiKey() {
        return weexApiKey;
    }

    public String getWeexApiSecret() {
        return weexApiSecret;
    }

    public String getWeexApiPassphrase() {
        return weexApiPassphrase;
    }

    public String getDeepseekApiUrl() {
        return deepseekApiUrl;
    }

    public String getDeepseekApiKey() {
        return deepseekApiKey;
    }

    public String getTradingSymbol() {
        return tradingSymbol;
    }

    public String getWeexTradingSymbol() {
        return tradingSymbol;
    }

    public double getTradingAmount() {
        return tradingAmount;
    }

    public double getWeexTradingAmount() {
        return tradingAmount;
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