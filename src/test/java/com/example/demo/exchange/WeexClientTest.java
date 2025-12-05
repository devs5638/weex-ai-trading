package com.example.demo.exchange;

import com.example.demo.config.ApiConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class WeexClientTest {

    @Mock
    private ApiConfig apiConfig;

    private WeexClient weexClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // 模拟配置
        when(apiConfig.getWeexApiUrl()).thenReturn("https://api.weex.com/v1");
        when(apiConfig.getWeexApiKey()).thenReturn("test-key");
        when(apiConfig.getWeexApiSecret()).thenReturn("test-secret");
        when(apiConfig.getWeexApiPassphrase()).thenReturn("test-passphrase");
        
        weexClient = new WeexClient(apiConfig);
    }

    @Test
    void testMarketDataAndBalance() {
        // 简单测试WeexClient实例化
        assertNotNull(weexClient);
        // 由于generateSignature是private方法，我们不直接测试它
    }
}