package com.example.demo.exchange;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.example.demo.config.ApiConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class WeexClient {

    private static final Logger logger = LoggerFactory.getLogger(WeexClient.class);
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ApiConfig apiConfig;

    @Autowired
    public WeexClient(ApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    /**
     * 获取市场行情数据
     */
    public Map<String, Object> getMarketData(String symbol) throws Exception {
        String url = apiConfig.getWeexApiUrl() + "/market/ticker?symbol=" + symbol;
        
        HttpResponse response = HttpRequest.get(url).execute();
        String responseString = response.body();
        
        logger.info("Market data response: {}", responseString);
        return objectMapper.readValue(responseString, Map.class);
    }

    /**
     * 获取账户余额
     */
    public Map<String, Object> getAccountBalance() throws Exception {
        String url = apiConfig.getWeexApiUrl() + "/account/balance";
        
        long timestamp = Instant.now().toEpochMilli();
        String signature = generateSignature(url, timestamp, "");
        
        HttpResponse response = HttpRequest.get(url)
            .header("X-API-KEY", apiConfig.getWeexApiKey())
            .header("X-TIMESTAMP", String.valueOf(timestamp))
            .header("X-SIGNATURE", signature)
            .execute();
            
        String responseString = response.body();
        
        logger.info("Account balance response: {}", responseString);
        return objectMapper.readValue(responseString, Map.class);
    }

    /**
     * 创建限价订单
     */
    public Map<String, Object> createLimitOrder(String symbol, String side, double price, double amount) throws Exception {
        String url = apiConfig.getWeexApiUrl() + "/order/create";
        
        Map<String, Object> orderParams = new HashMap<>();
        orderParams.put("symbol", symbol);
        orderParams.put("side", side); // BUY or SELL
        orderParams.put("type", "LIMIT");
        orderParams.put("price", new BigDecimal(price));
        orderParams.put("amount", new BigDecimal(amount));
        
        String requestBody = objectMapper.writeValueAsString(orderParams);
        long timestamp = Instant.now().toEpochMilli();
        String signature = generateSignature(url, timestamp, requestBody);
        
        HttpResponse response = HttpRequest.post(url)
            .body(requestBody)
            .contentType("application/json")
            .header("X-API-KEY", apiConfig.getWeexApiKey())
            .header("X-TIMESTAMP", String.valueOf(timestamp))
            .header("X-SIGNATURE", signature)
            .execute();
            
        String responseString = response.body();
        
        logger.info("Create order response: {}", responseString);
        return objectMapper.readValue(responseString, Map.class);
    }

    /**
     * 创建市价订单
     */
    public Map<String, Object> createMarketOrder(String symbol, String side, double amount) throws Exception {
        String url = apiConfig.getWeexApiUrl() + "/order/create";
        
        Map<String, Object> orderParams = new HashMap<>();
        orderParams.put("symbol", symbol);
        orderParams.put("side", side); // BUY or SELL
        orderParams.put("type", "MARKET");
        orderParams.put("amount", new BigDecimal(amount));
        
        String requestBody = objectMapper.writeValueAsString(orderParams);
        long timestamp = Instant.now().toEpochMilli();
        String signature = generateSignature(url, timestamp, requestBody);
        
        HttpResponse response = HttpRequest.post(url)
            .body(requestBody)
            .contentType("application/json")
            .header("X-API-KEY", apiConfig.getWeexApiKey())
            .header("X-TIMESTAMP", String.valueOf(timestamp))
            .header("X-SIGNATURE", signature)
            .execute();
            
        String responseString = response.body();
        
        logger.info("Create market order response: {}", responseString);
        return objectMapper.readValue(responseString, Map.class);
    }

    /**
     * 生成API签名
     */
    private String generateSignature(String url, long timestamp, String requestBody) {
        try {
            String message = timestamp + url + requestBody;
            SecretKeySpec secretKey = new SecretKeySpec(apiConfig.getWeexApiSecret().getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(secretKey);
            byte[] hash = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Error generating signature", e);
            throw new RuntimeException("Error generating signature", e);
        }
    }
}