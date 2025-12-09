package com.trading.ai;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.model.AiReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AIClient {

    private static final Logger logger = LoggerFactory.getLogger(AIClient.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();


    private static final String AI_URL = "https://api.longcat.chat/openai/v1/chat/completions";
    private static final String AI_AK = "ak_1SZ8DD4KX4pg8Ha5nH7qH66v3207Y";

    /**
     * 获取交易信号分析
     */
    public TradingSignal getTradingSignal(AiReq req) throws Exception {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "LongCat-Flash-Chat");
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 30);
        requestBody.put("frequency_penalty", 0);
        requestBody.put("top_k", 50);
        requestBody.put("top_p", 0.7);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一个专业的加密货币交易分析师，深耕币圈20年，具有丰富的经验，并且实时关注社会经济动态，了解加密政策的利好，能够根据提供的市场数据，给出明确的交易信号");
        messages.add(systemMessage);
        
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        if (req.isHasPosition()){
            userMessage.put("content", " btc的当前价格" + req.getPrice()
                    + ", 当前有持仓 20x 方向：" + req.getSide()
                    + " 开仓价格：" + req.getAvgEntryPrice()
                    + " 未实现盈亏：" + req.getUnrealizePnl()
                    + " 预估强平价：" + req.getLiquidatePrice()
                    + " 当前可用账户余额：" + req.getBalance()
                    + "，请根据这些数据给出本次的交易信号, 交易信号只能是三个维度: 操作： BUY, SELL, HOLD; 开单方向： LONG, SHORT；购买金额。请将返回值格式化为 JSON 字符串 {\"operation\":\"...\",\"side\":\"...\",\"amount\":...}");
        } else {
            userMessage.put("content", " btc的当前价格" + req.getPrice() + ", 当前没有持仓，请给出本次的交易信号,  交易信号只能是三个维度: 操作： BUY, SELL, HOLD; 开单方向： LONG, SHORT；购买金额。请将返回值格式化为 JSON 字符串 {\"operation\":\"...\",\"side\":\"...\",\"amount\":...}");
        }
        messages.add(userMessage);
        
        requestBody.put("messages", messages);
        
        HttpResponse response = HttpRequest.post(AI_URL)
            .body(objectMapper.writeValueAsString(requestBody))
            .contentType("application/json")
            .header("Authorization", "Bearer " + AI_AK)
            .execute();
            
        String responseString = response.body();
        
        logger.info("AI response: {}", responseString);
        
        // 解析响应
        Map<String, Object> responseMap = objectMapper.readValue(responseString, Map.class);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String content = (String) message.get("content");

        logger.info("Trading signal: {}", content);
        
        // 解析交易信号
        return parseTradingSignal(content);
    }

    /**
     * 解析AI返回的交易信号
     */
    public TradingSignal parseTradingSignal(String content) {
        String signalContent = content.trim().toUpperCase();
        
        if (signalContent.contains("BUY")) {
            return TradingSignal.BUY;
        } else if (signalContent.contains("SELL")) {
            return TradingSignal.SELL;
        } else {
            return TradingSignal.HOLD;
        }
    }

    /**
     * 格式化市场数据为AI输入格式
     */
    public String formatMarketData(String symbol, double price, double high, double low, double volume) {
        return String.format("Symbol: %s\nCurrent Price: %.2f\n24h High: %.2f\n24h Low: %.2f\n24h Volume: %.2f",
                symbol, price, high, low, volume);
    }

    /**
     * 交易信号枚举
     */
    public enum TradingSignal {
        BUY, SELL, HOLD
    }
}