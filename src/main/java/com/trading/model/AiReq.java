package com.trading.model;


import java.util.Map;

public class AiReq {


    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public boolean isHasPosition() {
        return isHasPosition;
    }

    public void setHasPosition(boolean hasPosition) {
        isHasPosition = hasPosition;
    }

    public Object getSide() {
        return side;
    }

    public void setSide(Object side) {
        this.side = side;
    }

    public Object getOpenValue() {
        return openValue;
    }

    public void setOpenValue(Object openValue) {
        this.openValue = openValue;
    }

    public Object getUnrealizePnl() {
        return unrealizePnl;
    }

    public void setUnrealizePnl(Object unrealizePnl) {
        this.unrealizePnl = unrealizePnl;
    }

    public Object getLiquidatePrice() {
        return liquidatePrice;
    }

    public void setLiquidatePrice(Object liquidatePrice) {
        this.liquidatePrice = liquidatePrice;
    }

    public Object getAvgEntryPrice() {
        return avgEntryPrice;
    }

    public void setAvgEntryPrice(Object avgEntryPrice) {
        this.avgEntryPrice = avgEntryPrice;
    }

    private String symbol;
    private String price;
    private String balance;

    private boolean isHasPosition = false;
    private Object side ;
    private Object openValue; //开仓价值
    private Object unrealizePnl; //未实现盈亏
    private Object liquidatePrice; //预估强平价
    private Object avgEntryPrice; //开仓均价


}
