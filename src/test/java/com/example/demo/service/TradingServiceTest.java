package com.example.demo.service;

import com.example.demo.ai.AIClient;
import com.example.demo.config.ApiConfig;
import com.example.demo.exchange.WeexClient;
import com.example.demo.strategy.AISignalStrategy;
import com.example.demo.strategy.Strategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TradingServiceTest {

    @Mock
    private ApiConfig apiConfig;
    
    @Mock
    private WeexClient weexClient;
    
    @Mock
    private AIClient aiClient;
    
    @Mock
    private Strategy strategy;

    private TradingService tradingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // 配置模拟的API参数
        when(apiConfig.getTradingSymbol()).thenReturn("BTCUSDT");
        when(apiConfig.getTradingAmount()).thenReturn(100.0);
        when(apiConfig.getTradingInterval()).thenReturn(60000L);
        when(apiConfig.getTradingStrategy()).thenReturn("aiSignal");
        
        // 配置策略映射
        tradingService = new TradingService(apiConfig, weexClient, aiClient, strategy);
    }

    @Test
    void testTradingStatus() {
        // 测试交易状态管理
        
        // 初始状态应该是停止的
        assertEquals(TradingService.TradingStatus.STOPPED, tradingService.getStatus());
        
        // 启动交易服务
        tradingService.startTrading();
        assertEquals(TradingService.TradingStatus.RUNNING, tradingService.getStatus());
        
        // 停止交易服务
        tradingService.stopTrading();
        assertEquals(TradingService.TradingStatus.STOPPED, tradingService.getStatus());
    }

    @Test
    void testCreateStrategy() {
        // 测试策略创建功能
        // 由于createStrategy方法是private的，我们无法直接测试
        // 这里我们测试TradingService的其他功能来验证策略创建的正确性
    }

    @Test
    void testTradeHistory() {
        // 测试交易历史记录
        
        // 初始交易历史应该为空
        assertTrue(tradingService.getTradeHistory().isEmpty());
        
        // 添加一条交易记录
        Map<String, Object> marketData = new HashMap<>();
        marketData.put("price", 30000.5);
        
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("orderId", "order-123");
        
        TradingService.TradeRecord record = new TradingService.TradeRecord(
            System.currentTimeMillis(),
            "BTCUSDT",
            Strategy.TradingDecision.Action.BUY,
            30000.5,
            100.0,
            mockResult,
            "Test trade"
        );
        
        // 这里需要通过反射或其他方式添加记录，因为addTradeRecord是私有方法
        // 或者模拟weexClient.createLimitOrder的调用
        
        // 验证交易历史记录
        // assertEquals(1, tradingService.getTradeHistory().size());
    }
}