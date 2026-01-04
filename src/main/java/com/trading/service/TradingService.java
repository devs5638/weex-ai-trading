package com.trading.service;

import com.alibaba.fastjson2.JSON;
import com.trading.ai.AIClient;
import com.trading.config.ApiConfig;
import com.trading.exchange.WeexClient;
import com.trading.model.AiReq;

import com.trading.model.TradingSignal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class TradingService {

    private static final Logger logger = LoggerFactory.getLogger(TradingService.class);

    private final ApiConfig apiConfig;
    private final WeexClient weexClient;
    private final AIClient aiClient;

    private ScheduledExecutorService executorService;
    private boolean isRunning = false;
    private final List<TradeRecord> tradeHistory = new ArrayList<>();
    private TradingStatus currentStatus = TradingStatus.STOPPED;

    @Autowired
    public TradingService(ApiConfig apiConfig, WeexClient weexClient, AIClient aiClient) {
        this.apiConfig = apiConfig;
        this.weexClient = weexClient;
        this.aiClient = aiClient;
    }

    /**
     * 启动交易服务
     */
    public synchronized void startTrading() {
        if (isRunning) {
            logger.warn("Trading service is already running");
            return;
        }

        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this::executeTradeCycle, 0, 5, TimeUnit.MINUTES);
        
        isRunning = true;
        currentStatus = TradingStatus.RUNNING;
        logger.info("Trading service started successfully");
    }

    /**
     * 停止交易服务
     */
    public synchronized void stopTrading() {
        if (!isRunning) {
            logger.warn("Trading service is not running");
            return;
        }

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        isRunning = false;
        currentStatus = TradingStatus.STOPPED;
        logger.info("Trading service stopped successfully");
    }

    /**
     * 执行单次交易周期
     */
    private void executeTradeCycle() {
        try {
            logger.info("Starting trade cycle for symbol: {}", apiConfig.getTradingSymbol());
            currentStatus = TradingStatus.EXECUTING;
            AiReq req = new AiReq();
            // 设置交易对符号
            req.setSymbol(apiConfig.getTradingSymbol());
            // 1. 获取市场价格
            Map<String, Object> marketData = weexClient.getMarketData(apiConfig.getTradingSymbol());
            String indexPrice = marketData.get("index").toString();
            req.setPrice(indexPrice);
            // 2. 获取当前余额
            Map<String, Object> accountBalance = weexClient.getAccountBalance();
            String balance =accountBalance.get("available").toString();
            req.setBalance(balance);

            // 查询当前仓位
            final Map<String, Object> position = weexClient.getPosition();
            if (position != null) {
              req.setHasPosition(true);
              req.setSide(position.get("side"));
              req.setOpenValue(position.get("openValue"));
              req.setUnrealizePnl(position.get("unrealizePnl"));
              req.setLiquidatePrice(position.get("liquidatePrice"));
              req.setAvgEntryPrice(position.get("avgEntryPrice"));
            }
            logger.info("Position: {}", JSON.toJSONString(req));
            // 2. 获取AI交易信号
            TradingSignal tradingSignal = aiClient.getTradingSignal(req);

            // 3. 根据策略生成交易决策
//            TradingDecision decision = strategy.getTradingDecision(apiConfig.getTradingSymbol(), marketData, aiSignal);
            
            // 4. 执行交易决策
            if (!Objects.equals(tradingSignal.getOperation(), "HOLD")) {
                executeTrade(tradingSignal);
            } else {
                logger.info("Hold decision, no trade executed");
            }
            
        } catch (Exception e) {
            logger.error("Error in trade cycle: {}", e.getMessage(), e);
            currentStatus = TradingStatus.ERROR;
        } finally {
            if (isRunning) {
                currentStatus = TradingStatus.RUNNING;
            }
        }
    }

    /**
     * 执行交易
     */
    private void executeTrade(TradingSignal tradingSignal) throws Exception {
        logger.info("Executing trade: {} {} {}",
                tradingSignal.getOperation(), apiConfig.getTradingSymbol(), tradingSignal.getAmount());
        
        Map<String, Object> result;

        // 执行市价订单
        try {
            weexClient.createMarketOrder(apiConfig.getTradingSymbol(), tradingSignal);
        } catch (Exception e) {
            logger.error("Error executing market order: {}", e.getMessage(), e);
        }

    }

    /**
     * 获取交易状态
     */
    public TradingStatus getStatus() {
        return currentStatus;
    }


    /**
     * 获取账户余额
     */
    public Map<String, Object> getAccountBalance() throws Exception {
        return weexClient.getAccountBalance();
    }

    /**
     * 获取市场数据
     */
    public Map<String, Object> getMarketData() throws Exception {
        return weexClient.getMarketData(apiConfig.getTradingSymbol());
    }

    /**
     * 获取当前持仓
     */
    public Map<String, Object> getPosition() throws Exception {
        return weexClient.getPosition();
    }

    /**
     * 获取交易历史
     */
    public List<TradeRecord> getTradeHistory() {
        synchronized (tradeHistory) {
            return new ArrayList<>(tradeHistory);
        }
    }

    /**
     * 交易状态枚举
     */
    public enum TradingStatus {
        IDLE, RUNNING, EXECUTING, STOPPED, ERROR
    }

    /**
     * 交易记录类
     */
    public static class TradeRecord {
        private final long timestamp;
        private final String symbol;
        private final String action;
        private final double price;
        private final double amount;
        private final Map<String, Object> result;
        private final String reason;
        
        public TradeRecord(long timestamp, String symbol, String action, double price, double amount, Map<String, Object> result, String reason) {
            this.timestamp = timestamp;
            this.symbol = symbol;
            this.action = action;
            this.price = price;
            this.amount = amount;
            this.result = result;
            this.reason = reason;
        }
        
        // Getters for all fields
        public long getTimestamp() {
            return timestamp;
        }
        
        public String getSymbol() {
            return symbol;
        }
        
        public String getAction() {
            return action;
        }
        
        public double getPrice() {
            return price;
        }
        
        public double getAmount() {
            return amount;
        }
        
        public Map<String, Object> getResult() {
            return result;
        }
        
        public String getReason() {
            return reason;
        }
        
        @Override
        public String toString() {
            return "TradeRecord{" +
                    "timestamp=" + timestamp +
                    ", symbol='" + symbol + '\'' +
                    ", action=" + action +
                    ", price=" + price +
                    ", amount=" + amount +
                    ", reason='" + reason + '\'' +
                    '}';
        }
    }

}