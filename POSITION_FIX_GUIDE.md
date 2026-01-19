# 持仓数据与币种一致性修复说明

## 问题描述
当用户在前端切换币种后，持仓数据显示的仍然是之前币种的持仓，而不是新选择币种的持仓。

## 问题原因
1. `WeexClient.getPosition()` 使用 `apiConfig.getTradingSymbol()` 获取币种
2. 当用户更新配置时，只更新了 `TradingService.tradingConfig`
3. `ApiConfig` 中的 `tradingSymbol` 没有同步更新
4. 导致查询持仓时仍使用旧的币种

## 修复内容

### 1. 后端修复

#### ApiConfig.java
添加了 setter 方法，允许动态更新配置：
```java
public void setTradingSymbol(String tradingSymbol) {
    this.tradingSymbol = tradingSymbol;
}

public void setTradingVolume(double tradingVolume) {
    this.tradingVolume = (int) tradingVolume;
}
```

#### TradingService.java
在 `updateTradingConfig()` 方法中同步更新 `ApiConfig`：
```java
public synchronized void updateTradingConfig(TradingConfig newConfig) {
    if (isRunning) {
        logger.warn("Cannot update config while trading is running");
        throw new IllegalStateException("请先停止交易再更新配置");
    }
    this.tradingConfig = newConfig;
    
    // 同步更新ApiConfig中的币种和交易量，确保WeexClient使用最新配置
    if (newConfig.getSymbol() != null) {
        apiConfig.setTradingSymbol(newConfig.getSymbol());
        logger.info("Updated trading symbol to: {}", newConfig.getSymbol());
    }
    if (newConfig.getTradingAmount() > 0) {
        apiConfig.setTradingVolume(newConfig.getTradingAmount());
        logger.info("Updated trading amount to: {}", newConfig.getTradingAmount());
    }
    
    logger.info("Trading config updated: {}", newConfig);
}
```

### 2. 前端优化

#### Dashboard.html
在持仓区域标题添加当前币种显示：
```html
<h2>
    <i class="fas fa-briefcase"></i> 当前持仓
    <span style="font-size: 14px; font-weight: normal; color: #7f8c8d; margin-left: 10px;">
        (<span id="position-symbol-display">-</span>)
    </span>
</h2>
```

#### style.js
1. **保存配置后立即刷新持仓数据**：
```javascript
.then(data => {
    if (data.status === 'success') {
        alert('✅ 配置保存成功！');
        currentConfig = data.config;
        updateCurrentSymbol(config.symbol);
        // 立即刷新持仓数据，显示新币种的持仓
        loadPosition();
        loadBalance();
    }
})
```

2. **更新币种显示函数**：
```javascript
function updateCurrentSymbol(symbol) {
    if (symbol) {
        const displayName = symbol.replace('cmt_', '').toUpperCase();
        
        // 更新数据卡片中的币种显示
        const symbolDisplay = document.getElementById('current-symbol');
        if (symbolDisplay) {
            symbolDisplay.textContent = displayName;
        }
        
        // 更新持仓区域的币种显示
        const positionSymbolDisplay = document.getElementById('position-symbol-display');
        if (positionSymbolDisplay) {
            positionSymbolDisplay.textContent = displayName;
        }
    }
}
```

## 测试步骤

### 1. 准备测试
1. 启动应用程序
2. 打开浏览器访问 Dashboard
3. 确保交易已停止（如果正在运行，先停止）

### 2. 测试币种切换
1. 在"交易配置"区域，选择一个币种（例如：BTC/USDT）
2. 点击"保存配置"按钮
3. 观察以下变化：
   - ✅ "当前币种"卡片应显示 "BTCUSDT"
   - ✅ "当前持仓"标题后应显示 "(BTCUSDT)"
   - ✅ 持仓表格应显示 BTC/USDT 的持仓数据（如果有）
   - ✅ 如果没有持仓，应显示"无持仓"

4. 切换到另一个币种（例如：DOGE/USDT）
5. 点击"保存配置"按钮
6. 再次观察：
   - ✅ "当前币种"卡片应显示 "DOGEUSDT"
   - ✅ "当前持仓"标题后应显示 "(DOGEUSDT)"
   - ✅ 持仓表格应显示 DOGE/USDT 的持仓数据（如果有）

### 3. 测试交易流程
1. 选择一个币种并保存配置
2. 启动交易
3. 等待系统建立持仓
4. 观察持仓数据是否与选择的币种一致
5. 停止交易
6. 切换到另一个币种
7. 保存配置
8. 确认持仓数据立即切换到新币种

### 4. 验证日志
查看应用日志，应该看到类似以下内容：
```
Updated trading symbol to: cmt_btcusdt
Updated trading amount to: 1000.0
Trading config updated: TradingConfig{symbol='cmt_btcusdt', ...}
```

## 预期结果
- ✅ 切换币种后，持仓数据立即显示新币种的持仓
- ✅ 页面上的币种显示保持一致（数据卡片和持仓标题）
- ✅ 交易时使用正确的币种
- ✅ 查询持仓时使用正确的币种
- ✅ 配置更新后，ApiConfig 和 TradingConfig 保持同步

## 注意事项
1. **必须先停止交易才能切换币种**：这是为了防止在交易过程中切换币种导致的数据不一致
2. **保存配置后会立即刷新持仓数据**：确保显示的是新币种的持仓
3. **持仓区域标题会显示当前币种**：方便用户确认正在查看哪个币种的持仓
4. **如果新币种没有持仓**：会显示"无持仓"，这是正常的

## 相关文件
- `src/main/java/com/trading/config/ApiConfig.java`
- `src/main/java/com/trading/service/TradingService.java`
- `src/main/resources/templates/Dashboard.html`
- `src/main/resources/static/js/style.js`

