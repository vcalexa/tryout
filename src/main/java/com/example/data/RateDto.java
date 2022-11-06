package com.example.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.HashMap;

public class RateDto {
    @JsonProperty("success")
    public boolean success;
    @JsonProperty("historical")
    public boolean historical;
    @JsonProperty("base")
    public String base;
    @JsonProperty("date")
    public String date;
    @JsonProperty("rates")
    public HashMap<String, BigDecimal> rates;


    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isHistorical() {
        return historical;
    }

    public void setHistorical(boolean historical) {
        this.historical = historical;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public HashMap<String, BigDecimal> getRates() {
        return rates;
    }

    public void setRates(HashMap<String, BigDecimal> rates) {
        this.rates = rates;
    }
}
