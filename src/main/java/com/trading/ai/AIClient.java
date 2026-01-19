package com.trading.ai;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import com.alibaba.fastjson2.JSON;
import com.trading.config.ApiConfig;
import com.trading.exchange.WeexClient;
import com.trading.model.AiReq;
import com.trading.model.TradingConfig;
import com.trading.model.TradingSignal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AIClient {

    private static final Logger logger = LoggerFactory.getLogger(AIClient.class);

    @Autowired
    private WeexClient weexClient;


    private static final String AI_URL = "https://api.longcat.chat/openai/v1/chat/completions";
    private static final String AI_AK = "ak_1SZ8DD4KX4pg8Ha5nH7qH66v3207Y";

    @Autowired
    private ApiConfig apiConfig;

    /**
     * 获取交易信号分析（使用配置的提示词）
     */
    public TradingSignal getTradingSignal(AiReq req, TradingConfig config) throws Exception {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "LongCat-Flash-Chat");
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 1024);
        requestBody.put("frequency_penalty", 0);
        requestBody.put("top_k", 50);
        requestBody.put("top_p", 0.7);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        // 使用配置的系统提示词
        systemMessage.put("content", config.getAiSystemPrompt());
        messages.add(systemMessage);

        // 获取历史K线数据
        String candlesData = "";
        try {
            String symbol = req.getSymbol() != null ? req.getSymbol() : config.getSymbol();
            JSONArray candles = weexClient.getHistoryCandles(symbol, "1m");
            candlesData = "\n\n历史K线数据(1分钟): " + JSON.toJSONString(candles) +
                    "\n\nK线参数说明：" +
                    "\nindex[0]: K线时间" +
                    "\nindex[1]: 开盘价" +
                    "\nindex[2]: 最高价" +
                    "\nindex[3]: 最低价" +
                    "\nindex[4]: 收盘价" +
                    "\nindex[5]: 交易币成交量" +
                    "\nindex[6]: 计价币成交量";
            logger.info("Fetched candles data for symbol: {}, count: {}", symbol, candles.size());
        } catch (Exception e) {
            logger.error("Failed to fetch history candles: {}", e.getMessage(), e);
            candlesData = "\n\n历史K线数据获取失败: " + e.getMessage();
        }

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");

        String userPrompt;
        if (req.isHasPosition()){
            // 使用配置的有持仓提示词模板，并替换占位符
            userPrompt = config.getAiUserPromptWithPosition()
                    .replace("{symbol}", req.getSymbol())
                    .replace("{price}", String.valueOf(req.getPrice()))
                    .replace("{side}", String.valueOf(req.getSide()))
                    .replace("{avgEntryPrice}", String.valueOf(req.getAvgEntryPrice()))
                    .replace("{unrealizePnl}", String.valueOf(req.getUnrealizePnl()))
                    .replace("{liquidatePrice}", String.valueOf(req.getLiquidatePrice()))
                    .replace("{balance}", String.valueOf(req.getBalance()));

            // 添加K线数据和交易信号格式说明
            userPrompt += candlesData;
            userPrompt += "\n\n请根据以上数据给出交易信号，交易信号包含4个内容：";
            userPrompt += "\n1. operation操作：BUY（买入开仓）, HOLD（持有）, SELL（卖出平仓）";
            userPrompt += "\n2. side开单方向：LONG（开多）, SHORT（开空）";
            userPrompt += "\n3. amount本次购买金额：USDT单位，建议使用" + config.getTradingAmount();
            userPrompt += "\n4. reason：一句话总结给出本次交易信号的原因，控制在100个字以内";
            userPrompt += "\n\n决策参考：";
            userPrompt += "\n- 如果未实现盈亏 < " + config.getStopLossThreshold() + " USDT，建议止损";
            userPrompt += "\n- 如果未实现盈亏 > " + config.getTakeProfitThreshold() + " USDT，建议止盈";
            userPrompt += "\n- 参考K线技术指标判断走向";
            userPrompt += "\n- 关注币圈最新资讯和政策动态";
            userPrompt += "\n\n请将返回值格式化为JSON字符串：{\"operation\":\"...\",\"side\":\"...\",\"amount\":...,\"reason\":\"...\"}";
        } else {
            // 使用配置的无持仓提示词模板
            userPrompt = config.getAiUserPromptNoPosition()
                    .replace("{symbol}", req.getSymbol())
                    .replace("{price}", String.valueOf(req.getPrice()))
                    .replace("{balance}", String.valueOf(req.getBalance()));

            // 添加K线数据和交易信号格式说明
            userPrompt += candlesData;
            userPrompt += "\n\n请根据以上数据给出交易信号，交易信号包含4个内容：";
            userPrompt += "\n1. operation操作：BUY（买入开仓）, SELL（卖出开仓）";
            userPrompt += "\n2. side开单方向：LONG（开多）, SHORT（开空）";
            userPrompt += "\n3. amount本次购买金额：USDT单位，建议使用" + config.getTradingAmount();
            userPrompt += "\n4. reason：一句话总结给出本次交易信号的原因，控制在100个字以内";
            userPrompt += "\n\n请将返回值格式化为JSON字符串：{\"operation\":\"...\",\"side\":\"...\",\"amount\":...,\"reason\":\"...\"}";
        }

        userMessage.put("content", userPrompt);
        messages.add(userMessage);
        
        requestBody.put("messages", messages);
        
        HttpResponse response = HttpRequest.post(AI_URL)
            .body(JSON.toJSONString(requestBody))
            .contentType("application/json")
            .header("Authorization", "Bearer " + AI_AK)
            .execute();
            
        String responseString = response.body();
        
        logger.info("AI response: {}", responseString);
        
        // 解析响应
        Map<String, Object> responseMap = JSON.parseObject(responseString, Map.class);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String content = (String) message.get("content");

        logger.info("Trading signal: {}", content);
        
        // 解析交易信号
        TradingSignal tradingSignal = parseTradingSignal(content);
        
        // 上报AI日志到指定接口
        try {
            uploadAiLogToWeex(req, requestBody, userMessage.get("content"), content, tradingSignal);
        } catch (Exception e) {
            logger.error("Failed to upload AI log: {}", e.getMessage(), e);
            // 日志上报失败不影响主流程，只记录错误
        }
        return tradingSignal;
    }

    /**
     * 上报AI日志到Weex接口
     */
    private void uploadAiLogToWeex(AiReq req, Map<String, Object> requestBody, String prompt, String aiResponse, TradingSignal tradingSignal) throws Exception {
        // 构建input信息
        Map<String, Object> input = new HashMap<>();
        input.put("prompt", prompt);
        
        // 构建output信息
        Map<String, Object> output = new HashMap<>();
        output.put("response", aiResponse);
        
        // 获取模型名称
        String model = (String) requestBody.get("model");
        if (model == null) {
            model = "LongCat-Flash-Chat";
        }
        
        // 构建explanation
        String explanation = tradingSignal.getReason();
        
        // 调用WeexClient上传日志
        weexClient.uploadAiLog(null, "Decision Making", model, input, output, explanation);
    }

    /**
     * 解析AI返回的交易信号
     */
    public TradingSignal parseTradingSignal(String content) {
        return JSON.parseObject(content, TradingSignal.class);
    }
}