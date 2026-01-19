package com.trading.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 交易配置类
 * 用于管理币种、交易数量、AI提示词等配置
 */
public class TradingConfig {
    
    // 当前选择的交易币种
    private String symbol;
    
    // 交易数量（USDT）
    private double tradingAmount;
    
    // AI系统提示词
    private String aiSystemPrompt;
    
    // AI用户提示词模板（有持仓时）
    private String aiUserPromptWithPosition;
    
    // AI用户提示词模板（无持仓时）
    private String aiUserPromptNoPosition;
    
    // 止损阈值（USDT）
    private double stopLossThreshold;
    
    // 止盈阈值（USDT）
    private double takeProfitThreshold;
    
    // 默认配置
    public TradingConfig() {
        this.symbol = "cmt_dogeusdt";
        this.tradingAmount = 3600 * 12;
        this.stopLossThreshold = -10.0;
        this.takeProfitThreshold = 30.0;
        
        // 默认AI系统提示词
        this.aiSystemPrompt = "你是一个专业的加密货币交易分析师，深耕币圈20年，具有丰富的经验，并且实时关注社会经济动态，了解加密政策的利好，能够根据网络上实时的市场数据，必须每次给出明确的交易信号，可以收集近期的资讯以及利好和美国的一些政策来决策";
        
        // 默认有持仓提示词模板
        this.aiUserPromptWithPosition = "当前价格: {price}, 持仓方向: {side}, 开仓价格: {avgEntryPrice}, 未实现盈亏: {unrealizePnl}, 预估强平价: {liquidatePrice}, 可用余额: {balance}。请根据K线数据和市场情况给出交易信号。";
        
        // 默认无持仓提示词模板
        this.aiUserPromptNoPosition = "当前价格: {price}, 可用余额: {balance}。请根据K线数据和市场情况给出交易信号。";
    }
    
    // Getters and Setters
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public double getTradingAmount() {
        return tradingAmount;
    }
    
    public void setTradingAmount(double tradingAmount) {
        this.tradingAmount = tradingAmount;
    }
    
    public String getAiSystemPrompt() {
        return aiSystemPrompt;
    }
    
    public void setAiSystemPrompt(String aiSystemPrompt) {
        this.aiSystemPrompt = aiSystemPrompt;
    }
    
    public String getAiUserPromptWithPosition() {
        return aiUserPromptWithPosition;
    }
    
    public void setAiUserPromptWithPosition(String aiUserPromptWithPosition) {
        this.aiUserPromptWithPosition = aiUserPromptWithPosition;
    }
    
    public String getAiUserPromptNoPosition() {
        return aiUserPromptNoPosition;
    }
    
    public void setAiUserPromptNoPosition(String aiUserPromptNoPosition) {
        this.aiUserPromptNoPosition = aiUserPromptNoPosition;
    }
    
    public double getStopLossThreshold() {
        return stopLossThreshold;
    }
    
    public void setStopLossThreshold(double stopLossThreshold) {
        this.stopLossThreshold = stopLossThreshold;
    }
    
    public double getTakeProfitThreshold() {
        return takeProfitThreshold;
    }
    
    public void setTakeProfitThreshold(double takeProfitThreshold) {
        this.takeProfitThreshold = takeProfitThreshold;
    }
    
    /**
     * 转换为Map格式
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("symbol", symbol);
        map.put("tradingAmount", tradingAmount);
        map.put("aiSystemPrompt", aiSystemPrompt);
        map.put("aiUserPromptWithPosition", aiUserPromptWithPosition);
        map.put("aiUserPromptNoPosition", aiUserPromptNoPosition);
        map.put("stopLossThreshold", stopLossThreshold);
        map.put("takeProfitThreshold", takeProfitThreshold);
        return map;
    }
    
    @Override
    public String toString() {
        return "TradingConfig{" +
                "symbol='" + symbol + '\'' +
                ", tradingAmount=" + tradingAmount +
                ", stopLossThreshold=" + stopLossThreshold +
                ", takeProfitThreshold=" + takeProfitThreshold +
                '}';
    }
}

