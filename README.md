# Weex AI 自动交易工具

## 项目概述

本项目是一个基于 Spring Boot 的自动交易工具，集成了 Weex 交易所 API 和 DeepSeek AI API，实现了基于 AI 分析的加密货币自动交易功能。

## 技术栈

- **后端框架**: Spring Boot 3.3.0
- **Java 版本**: Java 17
- **HTTP 客户端**: Apache HttpClient 5
- **JSON 处理**: Jackson
- **日志系统**: Log4j2
- **AI 接口**: DeepSeek API
- **交易所**: Weex API

## 环境要求

- JDK 17 或更高版本
- Maven 3.6 或更高版本
- Weex 交易所 API 密钥
- DeepSeek API 密钥

## 安装步骤

### 1. 克隆项目

```bash
git clone <repository-url>
cd <project-directory>
```

### 2. 安装依赖

```bash
mvn clean install
```

### 3. 配置 API 密钥

编辑 `src/main/resources/application.properties` 文件，添加你的 API 密钥：

```properties
# Weex Exchange API Configuration
weex.api.url=https://api.weex.com
weex.api.key=your_weex_api_key
weex.api.secret=your_weex_api_secret
weex.api.passphrase=your_weex_api_passphrase

# DeepSeek AI API Configuration
deepseek.api.url=https://api.deepseek.com
deepseek.api.key=your_deepseek_api_key
```

### 4. 配置交易参数

在 `application.properties` 中配置交易参数：

```properties
# Trading Configuration
weex.trading.symbol=BTCUSDT
weex.trading.amount=100
weex.trading.interval=60000
weex.trading.strategy=aiSignal
```

- `weex.trading.symbol`: 交易对（如 BTCUSDT）
- `weex.trading.amount`: 交易金额（美元）
- `weex.trading.interval`: 交易间隔（毫秒）
- `weex.trading.strategy`: 交易策略（目前仅支持 aiSignal）

### 5. 运行项目

```bash
mvn spring-boot:run
```

项目将在 http://localhost:8080 启动。

## API 接口说明

### 1. 启动交易服务

**POST** `/api/trading/start`

**响应示例**:
```json
{
  "status": "success",
  "message": "Trading service started successfully"
}
```

### 2. 停止交易服务

**POST** `/api/trading/stop`

**响应示例**:
```json
{
  "status": "success",
  "message": "Trading service stopped successfully"
}
```

### 3. 获取交易状态

**GET** `/api/trading/status`

**响应示例**:
```json
{
  "status": "success",
  "tradingStatus": "RUNNING"
}
```

### 4. 获取交易历史

**GET** `/api/trading/history`

**响应示例**:
```json
{
  "status": "success",
  "tradeCount": 5,
  "tradeHistory": [
    {
      "timestamp": "2023-07-15T14:30:00Z",
      "action": "BUY",
      "symbol": "BTCUSDT",
      "price": 30000,
      "amount": 100,
      "status": "SUCCESS"
    }
  ]
}
```

### 5. 获取账户余额

**GET** `/api/trading/balance`

**响应示例**:
```json
{
  "status": "success",
  "accountBalance": {
    "USDT": 10000,
    "BTC": 0.05
  }
}
```

### 6. 获取市场数据

**GET** `/api/trading/market`

**响应示例**:
```json
{
  "status": "success",
  "marketData": {
    "symbol": "BTCUSDT",
    "price": 30000,
    "high": 31000,
    "low": 29000,
    "volume": 1000000
  }
}
```

## 日志说明

项目使用 Log4j2 记录日志，日志文件存储在 `logs/` 目录下：

- `logs/trading.log`: 交易相关日志
- `logs/ai_analysis.log`: AI 分析相关日志
- `logs/error.log`: 错误日志

## 注意事项

1. **风险提示**: 加密货币交易具有高风险，请谨慎使用本工具。
2. **API 密钥安全**: 请勿将 API 密钥提交到版本控制系统。
3. **测试环境**: 在正式交易前，请先在测试环境中进行充分测试。
4. **定期检查**: 定期检查交易日志和账户余额，确保交易正常执行。
5. **AI 预测风险**: AI 分析结果仅供参考，不构成投资建议。

## 开发说明

### 添加新的交易策略

1. 创建新的策略类，实现 `Strategy` 接口
2. 在 `TradingService` 中注册新策略
3. 在 `application.properties` 中配置新策略

### 扩展 AI 分析功能

修改 `AIClient` 类，集成更多 AI 模型或分析方法。

## 许可证

本项目采用 MIT 许可证。

## 联系方式

如有问题或建议，请通过以下方式联系：

- 邮箱: example@example.com
- GitHub: https://github.com/example/weex-ai-trading