# Weex AI 自动交易工具

## 项目概述

本项目是一个基于 Spring Boot 的自动交易工具，集成了 Weex 交易所 API 和 LongCat AI API，实现了基于 AI 分析的加密货币自动交易功能。

## 技术栈

- **后端框架**: Spring Boot 3.3.0
- **Java 版本**: Java 17
- **HTTP 客户端**: Hutool Http 客户端
- **JSON 处理**: FastJSON 2
- **日志系统**: Log4j2
- **AI 接口**: LongCat AI API
- **交易所**: Weex API

## 环境要求

- JDK 17 或更高版本
- Maven 3.6 或更高版本
- Weex 交易所 API 密钥
- LongCat AI API 密钥

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
weex.api.key=your_weex_api_key
weex.api.secret=your_weex_api_secret
weex.api.passphrase=your_weex_api_passphrase

# LongCat AI API Configuration
longcat.api.key=your_longcat_api_key
```

### 4. 配置交易参数

在 `application.properties` 中配置交易参数：

```properties
# Trading Configuration
weex.trading.symbol=cmt_btcusdt
weex.trading.amount=100
weex.trading.interval=60000
weex.trading.strategy=aiSignal
```

- `weex.trading.symbol`: 交易对（如 cmt_btcusdt）
- `weex.trading.amount`: 交易金额（美元）
- `weex.trading.interval`: 交易间隔（毫秒）
- `weex.trading.strategy`: 交易策略（目前仅支持 aiSignal）

### 5. 运行项目

```bash
mvn spring-boot:run
```

项目将在 http://localhost:8080 启动。

## 功能说明

### 1. 实时数据展示

- **账户总权益**: 显示账户的总权益，计算公式为：账户余额 + 未实现盈亏 + 账户保证金
- **账户余额**: 显示账户的可用余额

### 2. 交易控制

- **启动交易**: 启动自动交易服务
- **停止交易**: 停止自动交易服务

### 3. 当前持仓

显示当前持仓信息，包括：
- **持仓方向**: 显示开多或开空
- **持仓数量**: 显示当前持仓的数量
- **开仓均价**: 显示开仓均价
- **未实现盈亏**: 显示当前持仓的未实现盈亏，盈利显示绿色，亏损显示红色
- **强平价格**: 显示强平价格
- **账户保证金**: 显示账户保证金

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
      "timestamp": 1765616422000,
      "symbol": "cmt_btcusdt",
      "action": "BUY",
      "price": 0.0,
      "amount": 100.0,
      "result": {},
      "reason": "AI signal generated trade"
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
    "available": "10000.00",
    "equity": "10000.00",
    "frozen": "0.00",
    "unrealizePnl": "0.00"
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
    "index": "30000.00",
    "symbol": "cmt_btcusdt"
  }
}
```

### 7. 获取当前持仓

**GET** `/api/trading/position`

**响应示例**:
```json
{
  "status": "success",
  "position": {
    "side": "long",
    "size": "100",
    "avgEntryPrice": "30000.00",
    "unrealizePnl": "100.00",
    "liquidatePrice": "29000.00",
    "marginSize": "500.00"
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

### 项目结构

```
/src
  /main
    /java/com/trading
      /ai          # AI 客户端相关代码
      /config      # 配置相关代码
      /controller  # 控制器相关代码
      /exchange    # 交易所客户端相关代码
      /model       # 数据模型相关代码
      /service     # 服务层相关代码
      /strategy    # 交易策略相关代码
      TradeApplication.java  # 应用程序入口
    /resources
      /static      # 静态资源
      /templates   # 模板文件
      application.properties  # 配置文件
      log4j2.xml   # 日志配置文件
```

### 核心组件

1. **TradeApplication**: 应用程序入口，启动 Spring Boot 应用
2. **TradingService**: 核心交易服务，管理交易状态和执行交易逻辑
3. **AIClient**: AI 客户端，调用 AI API 获取交易信号
4. **WeexClient**: 交易所客户端，调用 Weex API 执行交易操作
5. **Strategy**: 交易策略接口，定义交易决策逻辑

## 许可证

本项目采用 MIT 许可证。

## 页面功能展示

<img width="2544" height="1934" alt="image" src="https://github.com/user-attachments/assets/f71d8bd9-f0d8-498f-adee-d67bf58158dc" />
