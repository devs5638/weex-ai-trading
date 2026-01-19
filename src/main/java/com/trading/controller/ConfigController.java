package com.trading.controller;

import com.trading.model.TradingConfig;
import com.trading.service.TradingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置管理控制器
 * 用于管理交易配置，包括币种、数量、AI提示词等
 */
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);

    private final TradingService tradingService;

    @Autowired
    public ConfigController(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    /**
     * 获取当前交易配置
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getConfig() {
        try {
            TradingConfig config = tradingService.getTradingConfig();
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("config", config.toMap());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting config: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to get config: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 更新交易配置
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> updateConfig(@RequestBody TradingConfig newConfig) {
        try {
            tradingService.updateTradingConfig(newConfig);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "配置更新成功");
            response.put("config", newConfig.toMap());
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            logger.warn("Cannot update config: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Error updating config: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to update config: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取支持的币种列表
     */
    @GetMapping("/symbols")
    public ResponseEntity<Map<String, Object>> getSupportedSymbols() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            
            // 支持的币种列表
            Map<String, String>[] symbols = new Map[]{
                createSymbol("cmt_btcusdt", "BTC/USDT", "比特币"),
                createSymbol("cmt_ethusdt", "ETH/USDT", "以太坊"),
                createSymbol("cmt_dogeusdt", "DOGE/USDT", "狗狗币"),
                createSymbol("cmt_solusdt", "SOL/USDT", "Solana"),
                createSymbol("cmt_bnbusdt", "BNB/USDT", "币安币"),
                createSymbol("cmt_xrpusdt", "XRP/USDT", "瑞波币"),
                createSymbol("cmt_adausdt", "ADA/USDT", "艾达币"),
                createSymbol("cmt_avaxusdt", "AVAX/USDT", "雪崩协议"),
                createSymbol("cmt_maticusdt", "MATIC/USDT", "Polygon"),
                createSymbol("cmt_linkusdt", "LINK/USDT", "Chainlink")
            };
            
            response.put("symbols", symbols);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting supported symbols: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to get supported symbols: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取推荐的交易数量选项
     */
    @GetMapping("/amounts")
    public ResponseEntity<Map<String, Object>> getRecommendedAmounts() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            
            // 推荐的交易数量选项（USDT）
            double[] amounts = {10, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 43200};
            response.put("amounts", amounts);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting recommended amounts: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to get recommended amounts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private Map<String, String> createSymbol(String value, String label, String name) {
        Map<String, String> symbol = new HashMap<>();
        symbol.put("value", value);
        symbol.put("label", label);
        symbol.put("name", name);
        return symbol;
    }
}

