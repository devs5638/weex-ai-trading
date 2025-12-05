package com.trading.strategy;

import java.util.Map;

import com.trading.ai.AIClient.TradingSignal;
import com.trading.exchange.WeexClient;

public interface Strategy {
    /**
     * 获取交易决策
     */
    TradingDecision getTradingDecision(String symbol, Map<String, Object> marketData, TradingSignal aiSignal) throws Exception;
    
    /**
     * 获取策略名称
     */
    String getName();
    
    /**
     * 设置策略参数
     */
    void setParameter(String key, Object value);
    
    /**
     * 交易决策类
     */
    class TradingDecision {
        private final Action action;
        private final double price;
        private final double amount;
        private final String reason;
        
        public TradingDecision(Action action, double price, double amount, String reason) {
            this.action = action;
            this.price = price;
            this.amount = amount;
            this.reason = reason;
        }
        
        public Action getAction() {
            return action;
        }
        
        public double getPrice() {
            return price;
        }
        
        public double getAmount() {
            return amount;
        }
        
        public String getReason() {
            return reason;
        }
        
        public enum Action {
            BUY, SELL, HOLD
        }
    }
}