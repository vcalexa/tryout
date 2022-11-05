package com.example.data;

import java.math.BigDecimal;
import java.util.Date;

public record RequestType(Date date, BigDecimal amount, String currency, int client_id) { }
