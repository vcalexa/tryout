package com.example.data;

import java.math.BigDecimal;

public record ResponseType(BigDecimal amount, String currency) {
}