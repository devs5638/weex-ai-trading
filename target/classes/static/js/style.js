// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    // 初始化页面数据
    loadBalance();
    loadHistory();
    loadStatus();

    // 启动交易按钮事件
    const startBtn = document.getElementById('start-btn');
    startBtn.addEventListener('click', startTrading);

    // 停止交易按钮事件
    const stopBtn = document.getElementById('stop-btn');
    stopBtn.addEventListener('click', stopTrading);

    // 定时刷新数据
    setInterval(() => {
        loadBalance();
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
            document.getElementById('balance').textContent = 'USDT: ' + accountBalance.available;
        }
    })
    .catch(error => {
        console.error('加载余额出错:', error);
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

// 加载持仓量
function loadPosition() {
    fetch('/api/trading/position')
    .then(response => response.json())
    .then(data => {
        if (data.status === 'success') {
            document.getElementById('position').textContent = 'BTC: ' + data.position;
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