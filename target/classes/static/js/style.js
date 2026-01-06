// 全局变量，用于保存余额、未实现盈亏和账户保证金
let currentBalance = 0;
let currentUnrealizePnl = 0;
let currentMarginSize = 0;

// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    // 初始化页面数据
    loadBalance();
    loadPosition();
    loadStatus();
    
    // 初始化总权益显示
    document.getElementById('total-equity').textContent = 'USDT: 0.00';

    // 启动交易按钮事件
    const startBtn = document.getElementById('start-btn');
    startBtn.addEventListener('click', startTrading);

    // 停止交易按钮事件
    const stopBtn = document.getElementById('stop-btn');
    stopBtn.addEventListener('click', stopTrading);

    // 一键平仓按钮事件
    const closePositionBtn = document.getElementById('close-position-btn');
    if (closePositionBtn) {
        closePositionBtn.addEventListener('click', closePosition);
    }

    // 定时刷新数据
    setInterval(() => {
        loadBalance();
        loadPosition();
        loadStatus();
    }, 5000);
});

// 保存配置
function saveConfig() {
    const config = {
        apiKey: document.getElementById('apiKey').value,
        apiSecret: document.getElementById('apiSecret').value,
        symbol: document.getElementById('symbol').value,
        amount: document.getElementById('amount').value,
        interval: document.getElementById('interval').value
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
            alert('配置保存成功');
        } else {
            alert('配置保存失败: ' + data.message);
        }
    })
    .catch(error => {
        console.error('保存配置出错:', error);
        alert('保存配置出错');
    });
}

// 启动交易
function startTrading() {
    fetch('/api/trading/start', {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        if (data.status === 'success') {
            document.getElementById('status').textContent = '交易状态: 运行中';
            document.getElementById('status').style.color = 'green';
        } else {
            alert('启动交易失败: ' + data.message);
        }
    })
    .catch(error => {
        console.error('启动交易出错:', error);
        alert('启动交易出错');
    });
}

// 停止交易
function stopTrading() {
    fetch('/api/trading/stop', {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        if (data.status === 'success') {
            document.getElementById('status').textContent = '交易状态: 已停止';
            document.getElementById('status').style.color = 'red';
        } else {
            alert('停止交易失败: ' + data.message);
        }
    })
    .catch(error => {
        console.error('停止交易出错:', error);
        alert('停止交易出错');
    });
}

// 加载账户余额
function loadBalance() {
    fetch('/api/trading/balance')
    .then(response => response.json())
    .then(data => {
        if (data.status === 'success') {
            const accountBalance = data.accountBalance;
            // 保存余额值到全局变量
            currentBalance = parseFloat(accountBalance.available) || 0;
            document.getElementById('balance').textContent = 'USDT: ' + accountBalance.available;
            // 计算并更新总权益
            calculateTotalEquity();
        }
    })
    .catch(error => {
        console.error('加载余额出错:', error);
    });
}

// 计算并更新账户总权益
function calculateTotalEquity() {
    const totalEquity = currentBalance + currentUnrealizePnl + currentMarginSize;
    document.getElementById('total-equity').textContent = 'USDT: ' + totalEquity.toFixed(2);
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
                
                // 计算并更新总权益
                calculateTotalEquity();
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
                
                // 计算并更新总权益
                calculateTotalEquity();
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
            const statusElement = document.getElementById('status');
            if (data.tradingStatus === 'RUNNING' || data.tradingStatus === 'EXECUTING') {
                statusElement.textContent = '交易状态: 运行中';
                statusElement.style.color = 'green';
            } else {
                statusElement.textContent = '交易状态: 已停止';
                statusElement.style.color = 'red';
            }
        }
    })
    .catch(error => {
        console.error('加载交易状态出错:', error);
    });
}

// 一键平仓
function closePosition() {
    if (!confirm('确认一键平仓当前持仓吗？')) {
        return;
    }

    fetch('/api/trading/close-position', {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        if (data.status === 'success') {
            alert('平仓指令已提交');
            // 平仓后刷新持仓与余额信息
            loadPosition();
            loadBalance();
        } else {
            alert('平仓失败: ' + (data.message || '未知错误'));
        }
    })
    .catch(error => {
        console.error('平仓出错:', error);
        alert('平仓出错，请稍后重试');
    });
}