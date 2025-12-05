package com.example.demo.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.trading.ai.AIClient;
import com.trading.config.ApiConfig;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class AIClientTest {

    @Mock
    private ApiConfig apiConfig;

    private AIClient aiClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // 配置模拟的API参数
        when(apiConfig.getDeepseekApiUrl()).thenReturn("https://api.deepseek.com");
        when(apiConfig.getDeepseekApiKey()).thenReturn("test_key");
        
        aiClient = new AIClient(apiConfig);
    }

    @Test
    void testParseTradingSignal() {
        // 测试信号解析功能
        
        // 测试BUY信号
        String buyResponse = "BUY";
        AIClient.TradingSignal buySignal = aiClient.parseTradingSignal(buyResponse);
        assertEquals(AIClient.TradingSignal.BUY, buySignal);
        
        // 测试SELL信号
        String sellResponse = "SELL";
        AIClient.TradingSignal sellSignal = aiClient.parseTradingSignal(sellResponse);
        assertEquals(AIClient.TradingSignal.SELL, sellSignal);
        
        // 测试HOLD信号
        String holdResponse = "HOLD";
        AIClient.TradingSignal holdSignal = aiClient.parseTradingSignal(holdResponse);
        assertEquals(AIClient.TradingSignal.HOLD, holdSignal);
        
        // 测试无法识别的信号
        String unknownResponse = "UNKNOWN";
        AIClient.TradingSignal unknownSignal = aiClient.parseTradingSignal(unknownResponse);
        assertEquals(AIClient.TradingSignal.HOLD, unknownSignal);
    }

    @Test
    void testFormatMarketData() {
        // 测试市场数据格式化功能
        String symbol = "BTCUSDT";
        double price = 30000.5;
        double volume = 1000000.0;
        double high = 31000.0;
        double low = 29000.0;
        
        String formattedData = aiClient.formatMarketData(symbol, price, volume, high, low);
        
        assertNotNull(formattedData);
        assertTrue(formattedData.contains("BTCUSDT"));
        assertTrue(formattedData.contains("30000.5"));
        System.out.println("Formatted market data: " + formattedData);
    }
}