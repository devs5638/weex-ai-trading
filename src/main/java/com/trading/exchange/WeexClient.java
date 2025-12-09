package com.trading.exchange;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class WeexClient {

    private static final Logger logger = LoggerFactory.getLogger(WeexClient.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    // 合约API基础URL（从配置中获取，应设置为 https://api-contract.weex.com/capi/v2）
    private static final String API_KEY = "weex_045c783182b43aea80332292326458a7"; // 替换为实际的 API Key
    private static final String SECRET_KEY = "aecbf1cbe853a7201186c26963f7cdefb6336a5f79c1aa8bbe501521f3cb6545"; // 替换为实际的 Secret Key
    private static final String ACCESS_PASSPHRASE = "test011111111"; // 替换为实际的 Access Passphrase
    private static final String BASE_URL = "https://stg-pro-openapi.weex.tech";//231231

    /**
     * 获取市场行情数据（使用合约API）
     */
    public Map<String, Object> getMarketData(String symbol) throws Exception {
        String requestPath = "/capi/v2/market/index";
        String queryString = "?symbol=" + symbol + "&priceType=INDEX";
        String responseString = sendRequestGet(API_KEY, SECRET_KEY, ACCESS_PASSPHRASE, "GET", requestPath, queryString);

        logger.info("Market data response: {}", responseString);
        return objectMapper.readValue(responseString, Map.class);
    }

    public Map<String, Object> getPosition() throws Exception {
        String requestPath = "/capi/v2/account/position/singlePosition";
        String queryString = "?symbol=cmt_btcusdt";
        String responseString = sendRequestGet(API_KEY, SECRET_KEY, ACCESS_PASSPHRASE, "GET", requestPath, queryString);
        logger.info("singlePosition data response: {}", responseString);
        // 解析响应并过滤USDT数据
        JSONArray jsonArray = new JSONArray(responseString);

        // 查找USDT的账户信息
        Optional<JSONObject> usdtAccountOpt = jsonArray.stream()
                .map(item -> new JSONObject((Map<String, Object>) item))
                .filter(account -> "cmt_btcusdt".equals(account.getStr("symbol")))
                .findFirst();

        if (usdtAccountOpt.isPresent()) {
            JSONObject usdtAccount = usdtAccountOpt.get();

            // 创建返回的USDT账户信息Map
            Map<String, Object> result = new HashMap<>();
            result.put("liquidatePrice", usdtAccount.getStr("liquidatePrice"));
            result.put("unrealizePnl", usdtAccount.getStr("unrealizePnl"));
            result.put("side", usdtAccount.getStr("side"));
            result.put("openValue", usdtAccount.getStr("openValue"));
            result.put("size", usdtAccount.getStr("size"));
            
            // 计算开仓均价：Position.avgEntryPrice = round( Position.openValue / Position.size, Contract.tickSize)
            // 假设cmt_btcusdt合约的tickSize为0.1（实际值需要根据合约信息获取）
            final BigDecimal tickSize = new BigDecimal("0.1");
            
            // 获取开仓价值和开仓数量
            BigDecimal openValue = new BigDecimal(usdtAccount.getStr("openValue"));
            BigDecimal size = new BigDecimal(usdtAccount.getStr("size"));
            
            // 计算开仓均价
            BigDecimal avgEntryPrice = openValue.divide(size, 10, BigDecimal.ROUND_HALF_UP);
            
            // 根据持仓方向进行取整处理
            String side = usdtAccount.getStr("side");
            if ("LONG".equalsIgnoreCase(side)) {
                // 多仓向上取整
                avgEntryPrice = avgEntryPrice.divide(tickSize, 0, BigDecimal.ROUND_UP).multiply(tickSize);
            } else if ("SHORT".equalsIgnoreCase(side)) {
                // 空仓向下取整
                avgEntryPrice = avgEntryPrice.divide(tickSize, 0, BigDecimal.ROUND_DOWN).multiply(tickSize);
            }
            
            // 保留一位小数（与tickSize对应）
            avgEntryPrice = avgEntryPrice.setScale(1, BigDecimal.ROUND_HALF_UP);
            
            result.put("avgEntryPrice", avgEntryPrice.toPlainString());

            return result;
        } else {
            // 如果没有找到USDT信息，返回空Map或抛出异常
            logger.warn("No cmt_btcusdt singlePosition found in response");
            return new HashMap<>();
        }
    }

    /**
     * 获取合约账户余额，只返回USDT的账户信息
     */
    public Map<String, Object> getAccountBalance() throws Exception {

        String requestPath = "/capi/v2/account/assets";
        String response = sendRequestGet(API_KEY, SECRET_KEY, ACCESS_PASSPHRASE, "GET", requestPath, "");

        logger.info("Account balance response: {}", response);

//        return objectMapper.readValue(response, Map.class);
        // 解析响应并过滤USDT数据
        JSONArray jsonArray = new JSONArray(response);

        // 查找USDT的账户信息
        Optional<JSONObject> usdtAccountOpt = jsonArray.stream()
            .map(item -> new JSONObject((Map<String, Object>) item))
            .filter(account -> "USDT".equals(account.getStr("coinName")))
            .findFirst();

        if (usdtAccountOpt.isPresent()) {
            JSONObject usdtAccount = usdtAccountOpt.get();

            // 创建返回的USDT账户信息Map
            Map<String, Object> result = new HashMap<>();
            result.put("coinName", usdtAccount.getStr("coinName"));
            result.put("available", usdtAccount.getStr("available")); // 可用余额
            result.put("equity", usdtAccount.getStr("equity")); // 账户权益
            result.put("frozen", usdtAccount.getStr("frozen")); // 冻结金额
            result.put("unrealizePnl", usdtAccount.getStr("unrealizePnl")); // 未实现盈亏

            return result;
        } else {
            // 如果没有找到USDT信息，返回空Map或抛出异常
            logger.warn("No USDT account found in response");
            return new HashMap<>();
        }
    }

    /**
     * 创建市价订单（合约API）
     */
    public Map<String, Object> createMarketOrder(String symbol, String side, double amount) throws Exception {
        String requestPath = "/capi/v2/order/placeOrder";
        
        Map<String, Object> orderParams = new HashMap<>();
        orderParams.put("symbol", symbol);
        orderParams.put("client_oid", UUID.randomUUID().toString()); // 自定义订单号（不超过40个字符）
        orderParams.put("side", side); // BUY or SELL
        orderParams.put("type", "market"); // 1:开多 2:开空 3:平多 4:平空
        orderParams.put("size", new BigDecimal(amount)); // 下单数量
        orderParams.put("order_type", 0); // 0:普通，1:只做maker；2:全部成交或立即取消；3:立即成交并取消剩余
        orderParams.put("match_price", 1); // 0:限价，1:市价

        String requestBody = objectMapper.writeValueAsString(orderParams);
        String response = sendRequestPost(API_KEY, SECRET_KEY, ACCESS_PASSPHRASE, "POST", requestPath, "", requestBody);


        logger.info("Create market order response: {}", response);
        return objectMapper.readValue(response, Map.class);
    }



    // 生成签名（POST请求）
    public static String generateSignature(String secretKey, String timestamp, String method, String requestPath, String queryString, String body) throws Exception {
        String message = timestamp + method.toUpperCase() + requestPath + queryString + body;
        return generateHmacSha256Signature(secretKey, message);
    }
    // 生成签名（GET请求）
    public static String generateSignatureGet(String secretKey, String timestamp, String method, String requestPath, String queryString) throws Exception {
        String message = timestamp + method.toUpperCase() + requestPath + queryString;
        return generateHmacSha256Signature(secretKey, message);
    }
    // 生成 HMAC SHA256 签名
    private static String generateHmacSha256Signature(String secretKey, String message) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKeySpec);
        byte[] signatureBytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    // 发送 POST 请求
    public static String sendRequestPost(String apiKey, String secretKey, String accessPassphrase, String method, String requestPath, String queryString, String body) throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String signature = generateSignature(secretKey, timestamp, method, requestPath, queryString, body);

        HttpResponse response = HttpRequest.post(BASE_URL + requestPath)
                .body(body)
                .contentType("application/json")
                .header("ACCESS-KEY", apiKey)
                .header("ACCESS-SIGN", signature)
                .header("ACCESS-TIMESTAMP", timestamp)
                .header("ACCESS-PASSPHRASE", accessPassphrase)
                .header("Content-Type", "application/json")
                .header("locale", "zh-CN")
                .execute();

        String responseString = response.body();
        return responseString;
    }
    // 发送 GET 请求
    public static String sendRequestGet(String apiKey, String secretKey, String accessPassphrase, String method, String requestPath, String queryString) throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String signature = generateSignatureGet(secretKey, timestamp, method, requestPath, queryString);

        HttpResponse response = HttpRequest.get(BASE_URL + requestPath+queryString)
                .contentType("application/json")
                .header("ACCESS-KEY", apiKey)
                .header("ACCESS-SIGN", signature)
                .header("ACCESS-TIMESTAMP", timestamp)
                .header("ACCESS-PASSPHRASE", accessPassphrase)
                .header("Content-Type", "application/json")
                .header("locale", "zh-CN")
                .execute();

        return response.body();
    }
}