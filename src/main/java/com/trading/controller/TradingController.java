package com.trading.controller;

import com.trading.service.TradingService;
import com.trading.service.TradingService.TradingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trading")
public class TradingController {

    private static final Logger logger = LoggerFactory.getLogger(TradingController.class);

    private final TradingService tradingService;

    @Autowired
    public TradingController(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    /**
     * 启动交易服务
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startTrading() {
        try {
            tradingService.startTrading();
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Trading service started successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error starting trading service: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to start trading service: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 停止交易服务
     */
    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopTrading() {
        try {
            tradingService.stopTrading();
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Trading service stopped successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error stopping trading service: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to stop trading service: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取交易状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        try {
            TradingStatus status = tradingService.getStatus();
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("tradingStatus", status);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting trading status: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to get trading status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    /**
     * 获取账户余额
     */
    @GetMapping("/balance")
    public ResponseEntity<Map<String, Object>> getAccountBalance() {
        try {
            Map<String, Object> balance = tradingService.getAccountBalance();
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("accountBalance", balance);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting account balance: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to get account balance: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取市场数据
     */
    @GetMapping("/market")
    public ResponseEntity<Map<String, Object>> getMarketData() {
        try {
            Map<String, Object> marketData = tradingService.getMarketData();
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("marketData", marketData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting market data: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to get market data: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取当前持仓
     */
    @GetMapping("/position")
    public ResponseEntity<Map<String, Object>> getPosition() {
        try {
            Map<String, Object> position = tradingService.getPosition();
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("position", position);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting position: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to get position: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}