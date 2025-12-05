package com.example.demo.strategy;

import com.example.demo.ai.AIClient.TradingSignal;
import com.example.demo.strategy.Strategy.TradingDecision;
import com.example.demo.strategy.Strategy.TradingDecision.Action;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "trading.amount=0.001"
})
class AISignalStrategyTest {

    @Autowired
    private AISignalStrategy aiSignalStrategy;

    @BeforeEach
    void setUp() {
        // 自动注入已经完成初始化
    }

    @Test
    void testGetTradingDecision_BuySignal() throws Exception {
        // 模拟市场数据
        Map<String, Object> marketData = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> ticker = new HashMap<>();
        ticker.put("last", "45000.0");
        data.put("ticker", ticker);
        marketData.put("data", data);
        
        // 测试买入决策
        TradingDecision decision = aiSignalStrategy.getTradingDecision("BTCUSDT", marketData, TradingSignal.BUY);
        assertNotNull(decision);
        assertEquals(Action.BUY, decision.getAction());
        assertEquals(0.001, decision.getAmount());
    }

    @Test
    void testGetTradingDecision_SellSignal() throws Exception {
        // 模拟市场数据
        Map<String, Object> marketData = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> ticker = new HashMap<>();
        ticker.put("last", "45000.0");
        data.put("ticker", ticker);
        marketData.put("data", data);
        
        // 测试卖出决策
        TradingDecision decision = aiSignalStrategy.getTradingDecision("BTCUSDT", marketData, TradingSignal.SELL);
        assertNotNull(decision);
        assertEquals(Action.SELL, decision.getAction());
        assertEquals(0.001, decision.getAmount());
    }

    @Test
    void testGetTradingDecision_HoldSignal() throws Exception {
        // 模拟市场数据
        Map<String, Object> marketData = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> ticker = new HashMap<>();
        ticker.put("last", "45000.0");
        data.put("ticker", ticker);
        marketData.put("data", data);
        
        // 测试持有决策
        TradingDecision decision = aiSignalStrategy.getTradingDecision("BTCUSDT", marketData, TradingSignal.HOLD);
        assertNotNull(decision);
        assertEquals(Action.HOLD, decision.getAction());
        assertEquals(0, decision.getAmount());
    }

    @Test
    void testSetParameter() throws Exception {
        // 测试参数设置
        aiSignalStrategy.setParameter("tradingAmount", 0.002);
        
        // 验证参数是否生效（通过决策中的金额验证）
        Map<String, Object> marketData = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> ticker = new HashMap<>();
        ticker.put("last", "45000.0");
        data.put("ticker", ticker);
        marketData.put("data", data);
        
        TradingDecision decision = aiSignalStrategy.getTradingDecision("BTCUSDT", marketData, TradingSignal.BUY);
        assertEquals(0.002, decision.getAmount());
    }
}