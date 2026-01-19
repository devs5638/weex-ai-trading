// 全局变量
let currentBalance = 0;
let currentUnrealizePnl = 0;
let currentMarginSize = 0;
let currentConfig = null;

// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    // 初始化页面
    initializePage();

    // 加载配置
    loadConfig();

    // 加载支持的币种
    loadSupportedSymbols();

    // 初始化页面数据
    loadBalance();
    loadPosition();
    loadStatus();

    // 初始化总权益显示
    document.getElementById('total-equity').textContent = '0.00 USDT';

    // 启动交易按钮事件
    document.getElementById('start-btn').addEventListener('click', startTrading);

    // 停止交易按钮事件
    document.getElementById('stop-btn').addEventListener('click', stopTrading);

    // 一键平仓按钮事件
    document.getElementById('close-position-btn').addEventListener('click', closePosition);

    // 配置折叠按钮事件
    document.getElementById('toggle-config-btn').addEventListener('click', toggleConfig);

    // 保存配置按钮事件
    document.getElementById('save-config-btn').addEventListener('click', saveConfig);

    // 重置配置按钮事件
    document.getElementById('reset-config-btn').addEventListener('click', resetConfig);

    // 定时刷新数据
    setInterval(() => {
        loadBalance();
        loadPosition();
        loadStatus();
    }, 5000);
});

// 初始化页面
function initializePage() {
    console.log('Initializing Weex AI Trading Dashboard...');
}

// 折叠/展开配置区域
function toggleConfig() {
    const configContent = document.getElementById('config-content');
    const toggleBtn = document.getElementById('toggle-config-btn');

    configContent.classList.toggle('collapsed');
    toggleBtn.classList.toggle('rotated');
}

// 加载配置
function loadConfig() {
    fetch('/api/config')
    .then(response => response.json())
    .then(data => {
        if (data.status === 'success') {
            currentConfig = data.config;
            updateConfigUI(data.config);
        }
    })
    .catch(error => {
        console.error('加载配置出错:', error);
    });
}

// 更新配置UI
function updateConfigUI(config) {
    document.getElementById('symbol-select').value = config.symbol || 'cmt_dogeusdt';
    document.getElementById('amount-select').value = config.tradingAmount || 43200;
    document.getElementById('stop-loss').value = config.stopLossThreshold || -10;
    document.getElementById('take-profit').value = config.takeProfitThreshold || 30;
    document.getElementById('ai-system-prompt').value = config.aiSystemPrompt || '';
    document.getElementById('ai-prompt-with-position').value = config.aiUserPromptWithPosition || '';
    document.getElementById('ai-prompt-no-position').value = config.aiUserPromptNoPosition || '';

    // 更新当前币种显示
    updateCurrentSymbol(config.symbol);
}

// 更新当前币种显示
function updateCurrentSymbol(symbol) {
    if (symbol) {
        // 提取币种名称（去掉cmt_前缀）
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

// 加载支持的币种
function loadSupportedSymbols() {
    fetch('/api/config/symbols')
    .then(response => response.json())
    .then(data => {
        if (data.status === 'success') {
            const symbolSelect = document.getElementById('symbol-select');
            symbolSelect.innerHTML = '';

            data.symbols.forEach(symbol => {
                const option = document.createElement('option');
                option.value = symbol.value;
                option.textContent = `${symbol.label} - ${symbol.name}`;
                symbolSelect.appendChild(option);
            });

            // 如果已有配置，设置选中项
            if (currentConfig && currentConfig.symbol) {
                symbolSelect.value = currentConfig.symbol;
            }
        }
    })
    .catch(error => {
        console.error('加载币种列表出错:', error);
    });
}

// 保存配置
function saveConfig() {
    const config = {
        symbol: document.getElementById('symbol-select').value,
        tradingAmount: parseFloat(document.getElementById('amount-select').value),
        stopLossThreshold: parseFloat(document.getElementById('stop-loss').value),
        takeProfitThreshold: parseFloat(document.getElementById('take-profit').value),
        aiSystemPrompt: document.getElementById('ai-system-prompt').value,
        aiUserPromptWithPosition: document.getElementById('ai-prompt-with-position').value,
        aiUserPromptNoPosition: document.getElementById('ai-prompt-no-position').value
    };

    fetch('/api/config', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(config)
    })
    .then(response => response.json())
    .then(data => {
        if (data.status === 'success') {
            alert('✅ 配置保存成功！');
            currentConfig = data.config;
            updateCurrentSymbol(config.symbol);
            // 立即刷新持仓数据，显示新币种的持仓
            loadPosition();
            loadBalance();
        } else {
            alert('❌ 配置保存失败: ' + data.message);
        }
    })
    .catch(error => {
        console.error('保存配置出错:', error);
        alert('❌ 保存配置出错');
    });
}

// 重置配置为默认值
function resetConfig() {
    if (!confirm('确定要重置为默认配置吗？')) {
        return;
    }

    const defaultConfig = {
        symbol: 'cmt_dogeusdt',
        tradingAmount: 43200,
        stopLossThreshold: -10,
        takeProfitThreshold: 30,
        aiSystemPrompt: '你是一个专业的加密货币交易分析师，深耕币圈20年，具有丰富的经验，并且实时关注社会经济动态，了解加密政策的利好，能够根据网络上实时的市场数据，必须每次给出明确的交易信号，可以收集近期的资讯以及利好和美国的一些政策来决策',
        aiUserPromptWithPosition: '当前价格: {price}, 持仓方向: {side}, 开仓价格: {avgEntryPrice}, 未实现盈亏: {unrealizePnl}, 预估强平价: {liquidatePrice}, 可用余额: {balance}。请根据K线数据和市场情况给出交易信号。',
        aiUserPromptNoPosition: '当前价格: {price}, 可用余额: {balance}。请根据K线数据和市场情况给出交易信号。'
    };

    updateConfigUI(defaultConfig);
    alert('✅ 已重置为默认配置，请点击"保存配置"按钮保存');
}

// 启动交易
function startTrading() {
    fetch('/api/trading/start', {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        if (data.status === 'success') {
            updateStatusUI('运行中', 'running');
            alert('✅ 交易已启动！');
        } else {
            alert('❌ 启动交易失败: ' + data.message);
        }
    })
    .catch(error => {
        console.error('启动交易出错:', error);
        alert('❌ 启动交易出错');
    });
}

// 停止交易
function stopTrading() {
    if (!confirm('确定要停止交易吗？')) {
        return;
    }

    fetch('/api/trading/stop', {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        if (data.status === 'success') {
            updateStatusUI('已停止', 'stopped');
            alert('✅ 交易已停止！');
        } else {
            alert('❌ 停止交易失败: ' + data.message);
        }
    })
    .catch(error => {
        console.error('停止交易出错:', error);
        alert('❌ 停止交易出错');
    });
}

// 更新状态UI
function updateStatusUI(statusText, statusClass) {
    const statusElement = document.getElementById('status');
    const statusTextElement = document.getElementById('status-text');

    statusTextElement.textContent = statusText;
    statusElement.className = 'status ' + statusClass;
}

// 加载账户余额
function loadBalance() {
    fetch('/api/trading/balance')
    .then(response => response.json())
    .then(data => {
        if (data.status === 'success') {
            const accountBalance = data.accountBalance;

            // 可用余额
            currentBalance = parseFloat(accountBalance.available) || 0;
            document.getElementById('balance').textContent = currentBalance.toFixed(2) + ' USDT';

            // 总权益（使用API返回的equity）
            const totalEquity = parseFloat(accountBalance.equity) || 0;
            document.getElementById('total-equity').textContent = totalEquity.toFixed(2) + ' USDT';

            // 未实现盈亏（从API获取）
            if (accountBalance.unrealizePnl !== undefined) {
                currentUnrealizePnl = parseFloat(accountBalance.unrealizePnl) || 0;
            }
        }
    })
    .catch(error => {
        console.error('加载余额出错:', error);
        document.getElementById('balance').textContent = '加载失败';
        document.getElementById('total-equity').textContent = '加载失败';
    });
}

// 加载当前价格
function loadPrice() {
    fetch('/api/trading/market')
    .then(response => response.json())
    .then(data => {
        if (data.status === 'success') {
            document.getElementById('price').textContent = 'BTCUSDT: ' + data.price;
        }
    })
    .catch(error => {
        console.error('加载价格出错:', error);
    });
}

// 加载当前持仓
function loadPosition() {
    fetch('/api/trading/position')
    .then(response => response.json())
    .then(data => {
        if (data.status === 'success') {
            const position = data.position;
            const unrealizePnlElement = document.getElementById('position-unrealize-pnl');
            
            if (position) {
                // 转换持仓方向显示文本
                let sideText = position.side;
                if (sideText.toLowerCase() === 'long') {
                    sideText = '开多';
                } else if (sideText.toLowerCase() === 'short') {
                    sideText = '开空';
                }
                document.getElementById('position-side').textContent = sideText;
                document.getElementById('position-size').textContent = position.size;
                document.getElementById('position-open-value').textContent = position.avgEntryPrice;
                document.getElementById('position-liquidate-price').textContent = position.liquidatePrice;
                
                // 设置账户保证金
                const marginSize = position.marginSize || '0';
                document.getElementById('position-margin-size').textContent = marginSize;
                currentMarginSize = parseFloat(marginSize) || 0;
                
                // 设置盈亏值并添加样式
                const unrealizePnl = position.unrealizePnl;
                unrealizePnlElement.textContent = unrealizePnl;
                currentUnrealizePnl = parseFloat(unrealizePnl) || 0;
                
                // 移除之前的样式类
                unrealizePnlElement.classList.remove('positive', 'negative');
                
                // 添加正负样式
                if (currentUnrealizePnl > 0) {
                    unrealizePnlElement.classList.add('positive');
                } else if (currentUnrealizePnl < 0) {
                    unrealizePnlElement.classList.add('negative');
                }
            } else {
                document.getElementById('position-side').textContent = '无持仓';
                document.getElementById('position-size').textContent = '0';
                document.getElementById('position-open-value').textContent = '0';
                document.getElementById('position-liquidate-price').textContent = 'N/A';
                document.getElementById('position-margin-size').textContent = '0';

                // 无持仓时重置盈亏样式和值
                unrealizePnlElement.textContent = '0';
                unrealizePnlElement.classList.remove('positive', 'negative');

                // 重置全局变量
                currentMarginSize = 0;
                currentUnrealizePnl = 0;
            }
        }
    })
    .catch(error => {
        console.error('加载持仓出错:', error);
    });
}

// 加载交易历史
function loadHistory() {
    fetch('/api/trading/history')
    .then(response => response.json())
    .then(data => {
        if (data.status === 'success') {
            const historyList = document.getElementById('history-list');
            historyList.innerHTML = '';

            if (data.history.length === 0) {
                historyList.innerHTML = '<p>暂无交易历史</p>';
                return;
            }

            data.history.forEach(trade => {
                const tradeItem = document.createElement('div');
                tradeItem.className = 'trade-item';
                tradeItem.innerHTML = `
                    <strong>${trade.type}</strong> ${trade.amount} BTC at ${trade.price} USDT
                    <br>
                    <small>${new Date(trade.timestamp).toLocaleString()}</small>
                `;
                historyList.appendChild(tradeItem);
            });
        }
    })
    .catch(error => {
        console.error('加载交易历史出错:', error);
    });
}

// 加载交易状态
function loadStatus() {
    fetch('/api/trading/status')
    .then(response => response.json())
    .then(data => {
        if (data.status === 'success') {
            const tradingStatus = data.tradingStatus;
            let statusText = '';
            let statusClass = '';

            if (tradingStatus === 'RUNNING' || tradingStatus === 'EXECUTING') {
                statusText = '运行中';
                statusClass = 'running';
            } else {
                statusText = '已停止';
                statusClass = 'stopped';
            }

            updateStatusUI(statusText, statusClass);
        }
    })
    .catch(error => {
        console.error('加载交易状态出错:', error);
    });
}

// 一键平仓
function closePosition() {
    if (!confirm('⚠️ 确认一键平仓当前持仓吗？')) {
        return;
    }

    fetch('/api/trading/close-position', {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        if (data.status === 'success') {
            alert('✅ 平仓指令已提交！');
            // 平仓后刷新持仓与余额信息
            loadPosition();
            loadBalance();
        } else {
            alert('❌ 平仓失败: ' + (data.message || '未知错误'));
        }
    })
    .catch(error => {
        console.error('平仓出错:', error);
        alert('❌ 平仓出错，请稍后重试');
    });
}