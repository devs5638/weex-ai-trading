package com.trading.ai;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.trading.config.ApiConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AIClient {

    private static final Logger logger = LoggerFactory.getLogger(AIClient.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ApiConfig apiConfig;

    @Autowired
    public AIClient(ApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    /**
     * 获取交易信号分析
     */
    public TradingSignal getTradingSignal(String marketData) throws Exception {
        String url = apiConfig.getDeepseekApiUrl() + "/chat/completions";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "deepseek-chat");
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 100);
        
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一个专业的加密货币交易分析师，根据提供的市场数据，给出明确的交易信号：BUY、SELL或HOLD。请直接输出交易信号，不要添加其他解释。");
        messages.add(systemMessage);
        
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", marketData);
        messages.add(userMessage);
        
        requestBody.put("messages", messages);
        
        HttpResponse response = HttpRequest.post(url)
            .body(objectMapper.writeValueAsString(requestBody))
            .contentType("application/json")
            .header("Authorization", "Bearer " + apiConfig.getDeepseekApiKey())
            .execute();
            
        String responseString = response.body();
        
        logger.info("AI response: {}", responseString);
        
        // 解析响应
        Map<String, Object> responseMap = objectMapper.readValue(responseString, Map.class);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String content = (String) message.get("content");
        
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