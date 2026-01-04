package com.trading.ai;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import com.alibaba.fastjson2.JSON;
import com.trading.exchange.WeexClient;
import com.trading.model.AiReq;
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

    /**
     * 获取交易信号分析
     */
    public TradingSignal getTradingSignal(AiReq req) throws Exception {

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
        systemMessage.put("content", "你是一个专业的加密货币交易分析师，深耕币圈20年，具有丰富的经验，并且实时关注社会经济动态，了解加密政策的利好，能够根据网络上实时的市场数据，必须每次给出明确的交易信号，可以收集近期的资讯以及利好和美国的一些政策来决策");
        messages.add(systemMessage);
        
        // 获取历史K线数据
        String candlesData = "";
        try {
            String symbol = req.getSymbol() != null ? req.getSymbol() : "cmt_dogeusdt";
            JSONArray candles = weexClient.getHistoryCandles(symbol, "1m");
            candlesData = "历史K线数据(1分钟): " + JSON.toJSONString(candles);
            logger.info("Fetched candles data for symbol: {}, count: {}", symbol, candles.size());
        } catch (Exception e) {
            logger.error("Failed to fetch history candles: {}", e.getMessage(), e);
            candlesData = "历史K线数据获取失败: " + e.getMessage();
        }
        
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        if (req.isHasPosition()){
            userMessage.put("content", " doge的当前价格" + req.getPrice()
                    + ", 当前有持仓 20x 方向：" + req.getSide()
                    + " 开仓价格：" + req.getAvgEntryPrice()
                    + " 未实现盈亏：" + req.getUnrealizePnl()
                    + " 预估强平价：" + req.getLiquidatePrice()
                    + " 当前可用账户余额：" + req.getBalance()
                    + candlesData + "k线参数说明：" +
                    "参数名\t类型\t说明\n" +
                    "index[0]\tString\tK线时间\n" +
                    "index[1]\tString\t开盘价\n" +
                    "index[2]\tString\t最高价\n" +
                    "index[3]\tString\t最低价\n" +
                    "index[4]\tString\t收盘价\n" +
                    "index[5]\tString\t交易币成交量\n" +
                    "index[6]\tString\t计价币成交量"
                    + " 生成策略信号如下：请根据这些数据给出本次的交易信号, 交易信号包含4个内容: operation操作： BUY, SELL,HOLD; side开单方向： LONG（开多）, SHORT（开空）;amount本次购买金额:usdt单位; reason:一句话总结给出本次交易信号的原因控制在100个字。请将返回值格式化为 JSON 字符串 {\"operation\":\"...\",\"side\":\"...\",\"reason\":\"...\"}"
                    + " 信号结果可以参考以下几个角度：" +
                    " 1、如果未实现盈亏<-10,直接返回 SELL 信号"+
                    " 2、如果未实现盈亏>30,直接返回 SELL 信号"+
                    " 3、搜索全网相关币圈的利好利空政策来决定判断后期走向给出交易信号"+
                    " 4、参考一分钟k线指标判断走向给出交易信号");
        } else {
            userMessage.put("content", " doge的当前价格" + req.getPrice() 
                    + candlesData
                    + ", 当前没有持仓，请必须明确给出本次的交易信号,  交易信号包含4个内容: operation操作： BUY, SELL,HOLD; side开单方向： LONG（开多）, SHORT（开空）;amount本次购买金额:usdt单位; reason:一句话总结给出本次交易信号的原因控制在100个字。请将返回值格式化为 JSON 字符串 {\"operation\":\"...\",\"side\":\"...\",\"reason\":\"...\"}");
        }
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