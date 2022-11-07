package com.example.service;

import com.example.client.ExchangeClient;
import com.example.data.CommissionApplied;
import com.example.data.CommissionType;
import com.example.data.RateDto;
import com.example.data.RequestType;
import com.example.data.ResponseType;
import com.example.repository.CommisionsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@org.springframework.stereotype.Service
public class Service {

    private static final String EUR = "EUR";
    private static final BigDecimal STANDARD_COMMISSION = BigDecimal.valueOf(0.005);
    private static final BigDecimal MINIMUM_AMOUNT = BigDecimal.valueOf(0.05);
    private static final BigDecimal RULE3_COMMISSION = BigDecimal.valueOf(0.03);

    private final CommisionsRepository commissionsRepository;
    private final ExchangeClient exchangeClient;

    public Service(CommisionsRepository commissionsRepository, ExchangeClient exchangeClient) {
        this.commissionsRepository = commissionsRepository;
        this.exchangeClient = exchangeClient;
    }

    @Transactional
    public ResponseEntity<Object> addTransaction(RequestType requestBody) {

        Date requestedDate = requestBody.date();
        String dateYearMonthDay = getDateInFormat(requestedDate, "yyyy-MM-dd");
        String thisTransactionCurrency = requestBody.currency();

        // get rates
        BigDecimal thisTransactionAmount = requestBody.amount();
        BigDecimal thisTransactionAmountInEUR;
        int clientId = requestBody.client_id();

        if (!thisTransactionCurrency.equals(EUR)) {
            BigDecimal currencyRateObject = getRates(requestBody).getRates().get(thisTransactionCurrency);
            thisTransactionAmountInEUR = thisTransactionAmount.divide(currencyRateObject, 2, RoundingMode.CEILING);
            log.info("Client client_id {} converted {} {} to {} {}", clientId, thisTransactionAmount, thisTransactionCurrency, thisTransactionAmountInEUR, EUR);
        } else {
            thisTransactionAmountInEUR = thisTransactionAmount;
        }

        // CALCULATE COMMISSIONS
        // Rule #1: Default pricing
        BigDecimal rule1Commission = thisTransactionAmountInEUR.multiply(STANDARD_COMMISSION).max(MINIMUM_AMOUNT);

        // Rule #2: Client with a discount
        BigDecimal rule2Commission = BigDecimal.ZERO;

        // Rule #3: High turnover discount
        String requestedYearAndMonth = getDateInFormat(requestedDate, "yyyy-MM");
        BigDecimal alreadyProcessedMonthlyTurnoverPerClient = commissionsRepository.getClientSumOfTurnoverPerMonth(clientId, requestedYearAndMonth).orElse(BigDecimal.ZERO);
        BigDecimal rule3Commission = BigDecimal.ZERO;
        boolean monthlyTurnoverOf1000EUROHasBeenReached;
        monthlyTurnoverOf1000EUROHasBeenReached = alreadyProcessedMonthlyTurnoverPerClient.compareTo(BigDecimal.valueOf(1000)) >= 0;

        log.info("Client client_id {} processed monthly turnover was {} {}", clientId, alreadyProcessedMonthlyTurnoverPerClient, EUR);
        if (monthlyTurnoverOf1000EUROHasBeenReached) {
            // commission is = 0,03 EUR for the current transaction
            rule3Commission = RULE3_COMMISSION;
        }

        // APPLY COMMISSIONS RULES
        BigDecimal resultCommission;
        CommissionApplied commissionAppliedRule = calculateRule(clientId, rule1Commission, rule3Commission, monthlyTurnoverOf1000EUROHasBeenReached);

        switch (commissionAppliedRule) {
            case RULE3 -> resultCommission = rule3Commission;
            case RULE2 -> resultCommission = rule2Commission.max(MINIMUM_AMOUNT);
            default -> resultCommission = rule1Commission;
        }
        resultCommission = resultCommission.setScale(2, RoundingMode.CEILING);
        log.info("Client client_id {} applied commision {} = {} EUR on {}", clientId, commissionAppliedRule, resultCommission, dateYearMonthDay);

        // get current date CommissionType
        CommissionType commissionType = commissionsRepository.findByIdAndDate(clientId, requestedDate).orElse(new CommissionType(0, clientId, requestBody.date(), thisTransactionAmountInEUR));

        // get processed daily turnover add this transaction amount and save to db
        commissionType.setAmount(commissionType.getAmount().add(thisTransactionAmountInEUR));

        commissionsRepository.save(commissionType);

        return new ResponseEntity<>(new ResponseType(resultCommission, EUR), HttpStatus.CREATED);

    }

    private RateDto getRates(RequestType requestBody) {
        ResponseEntity<RateDto> ratesEntity = exchangeClient.getRates(requestBody.date());
        if (!ratesEntity.getStatusCode().equals(HttpStatus.OK))
            throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Getting currency rates failed.");
        return ratesEntity.getBody();
    }

    private CommissionApplied calculateRule(int clientId, BigDecimal commission1, BigDecimal commission3, boolean turnoverReached) {
        if (clientId == 42 && turnoverReached && commission1.compareTo(commission3) <= 0)
            return CommissionApplied.RULE1;
        if (clientId == 42 && turnoverReached && commission1.compareTo(commission3) > 0) return CommissionApplied.RULE3;
        if (clientId == 42 && !turnoverReached) return CommissionApplied.RULE2;
        if (clientId != 42 && !turnoverReached) return CommissionApplied.RULE1;
        return CommissionApplied.RULE3;
    }

    private String getDateInFormat(Date requestedDate, String pattern) {
        SimpleDateFormat simpleDateFormatYearMonth = new SimpleDateFormat(pattern);
        return simpleDateFormatYearMonth.format(requestedDate);
    }
}
