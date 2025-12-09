package com.trading.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.ai.AIClient;
import com.trading.ai.AIClient.TradingSignal;
import com.trading.config.ApiConfig;
import com.trading.exchange.WeexClient;
import com.trading.model.AiReq;
import com.trading.strategy.Strategy;
import com.trading.strategy.Strategy.TradingDecision;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class TradingService {

    private static final Logger logger = LoggerFactory.getLogger(TradingService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ApiConfig apiConfig;
    private final WeexClient weexClient;
    private final AIClient aiClient;
    private final Strategy strategy;

    private ScheduledExecutorService executorService;
    private boolean isRunning = false;
    private final List<TradeRecord> tradeHistory = new ArrayList<>();
    private TradingStatus currentStatus = TradingStatus.STOPPED;

    @Autowired
    public TradingService(ApiConfig apiConfig, WeexClient weexClient, AIClient aiClient, Strategy strategy) {
        this.apiConfig = apiConfig;
        this.weexClient = weexClient;
        this.aiClient = aiClient;
        this.strategy = strategy;
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
        executorService.scheduleAtFixedRate(this::executeTradeCycle, 0, 60000, TimeUnit.MILLISECONDS);
        
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
            logger.info("Position: {}", objectMapper.writeValueAsString(req));
            // 2. 获取AI交易信号
            TradingSignal aiSignal = aiClient.getTradingSignal(req);
            
            // 3. 根据策略生成交易决策
            TradingDecision decision = strategy.getTradingDecision(apiConfig.getTradingSymbol(), marketData, aiSignal);
            
            // 4. 执行交易决策
            if (decision.getAction() != TradingDecision.Action.HOLD) {
                executeTrade(decision);
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
    private void executeTrade(TradingDecision decision) throws Exception {
        logger.info("Executing trade: {} {} {} at price {}", 
                decision.getAction(), apiConfig.getTradingSymbol(), decision.getAmount(), decision.getPrice());
        
        Map<String, Object> result;
        String side = decision.getAction() == TradingDecision.Action.BUY ? "BUY" : "SELL";
        
        // 执行市价订单
        result = weexClient.createMarketOrder(apiConfig.getTradingSymbol(), side, decision.getAmount());
        
        // 记录交易
        TradeRecord record = new TradeRecord(
                System.currentTimeMillis(),
                apiConfig.getTradingSymbol(),
                decision.getAction(),
                decision.getPrice(),
                decision.getAmount(),
                result,
                decision.getReason()
        );
        
        synchronized (tradeHistory) {
            tradeHistory.add(record);
        }
        
        logger.info("Trade executed successfully: {}", record);
    }

    /**
     * 获取交易状态
     */
    public TradingStatus getStatus() {
        return currentStatus;
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
        private final TradingDecision.Action action;
        private final double price;
        private final double amount;
        private final Map<String, Object> result;
        private final String reason;
        
        public TradeRecord(long timestamp, String symbol, TradingDecision.Action action, double price, double amount, Map<String, Object> result, String reason) {
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
        
        public TradingDecision.Action getAction() {
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