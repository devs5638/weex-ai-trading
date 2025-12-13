package com.trading.model;

public class TradingSignal {

    public   String operation;

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getType() {
        if (operation.equals("BUY") && side.equals("LONG")) {
            type = 1;
        } else if (operation.equals("BUY") && side.equals("SHORT")) {
            type = 2;
        } else if (operation.equals("SELL") && side.equals("LONG")){
            type = 3;
        } else if (operation.equals("SELL") && side.equals("SHORT")){
            type = 4;
        }
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public   String side ;
    public   Integer amount;
    public   Integer type;




}
