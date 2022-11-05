package com.example.service;

import com.example.data.CommissionApplied;
import com.example.data.CommissionType;
import com.example.data.RequestType;
import com.example.data.ResponseType;
import com.example.repository.CommisionsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

@Slf4j
@org.springframework.stereotype.Service
public class Service {

    private String EUR = "EUR";
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private CommisionsRepository commissionsRepository;

    @Transactional
    public ResponseEntity<Object> addTransaction(RequestType requestBody) {

        Date requestedDate = requestBody.date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateYearMonthDay = simpleDateFormat.format(requestedDate);
        String thisTransactionCurrency = requestBody.currency();
        // get rates
        ResponseEntity<Object> responseEntity = restTemplate.exchange("https://api.exchangerate.host/" + dateYearMonthDay, HttpMethod.GET, HttpEntity.EMPTY, Object.class);

        if (!responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            return new ResponseEntity<>("Getting currency rates failed.", HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            BigDecimal thisTransactionAmount = requestBody.amount();
            BigDecimal thisTransactionAmountInEUR;
            int clientId = requestBody.client_id();
            if (!thisTransactionCurrency.equals(EUR)) {
                Object currencyRateObject = ((Map) ((Map) responseEntity.getBody()).get("rates")).get(thisTransactionCurrency);
                BigDecimal currencyRate = null;
                if (currencyRateObject instanceof Double currencyRateDouble) {
                    currencyRate = BigDecimal.valueOf(currencyRateDouble);
                } else if (currencyRateObject instanceof BigDecimal currencyRateBigDecimal) {
                    currencyRate = currencyRateBigDecimal;
                }
                // convert thisTransactionAmount to EUR with 2 DECIMAL
                thisTransactionAmountInEUR = thisTransactionAmount.divide(currencyRate, 2, RoundingMode.CEILING);
                log.info("Client client_id {} converted {} {} to {} {}", clientId, thisTransactionAmount, thisTransactionCurrency, thisTransactionAmountInEUR, EUR);
            } else {
                thisTransactionAmountInEUR = thisTransactionAmount;
            }

            // CALCULATE COMMISSIONS
            // Rule #1: Default pricing
            BigDecimal rule1Commission = thisTransactionAmountInEUR.multiply(BigDecimal.valueOf(0.005));
            if (rule1Commission.compareTo(BigDecimal.valueOf(0.05)) == -1)
                rule1Commission = BigDecimal.valueOf(0.05);

            // Rule #2: Client with a discount
            BigDecimal rule2Commission = BigDecimal.ZERO;
            boolean isClientId42 = clientId == 42;
            if (isClientId42)
                rule2Commission = BigDecimal.valueOf(0.05);

            // Rule #3: High turnover discount
            SimpleDateFormat simpleDateFormatYearMonth = new SimpleDateFormat("yyyy-MM");
            String requestedYearAndMonth = simpleDateFormatYearMonth.format(requestedDate);
            BigDecimal alreadyProcessedMonthlyTurnoverPerClient = commissionsRepository.getClientSumOfTurnoverPerMonth(clientId, requestedYearAndMonth);
            BigDecimal rule3Commission = BigDecimal.ZERO;
            boolean monthlyTurnoverOf1000EUROHasBeenReached;
            if (alreadyProcessedMonthlyTurnoverPerClient == null) {
                alreadyProcessedMonthlyTurnoverPerClient = BigDecimal.ZERO;
                monthlyTurnoverOf1000EUROHasBeenReached = false;
            } else {
                // if equal or greater than 1000 EUR
                monthlyTurnoverOf1000EUROHasBeenReached = alreadyProcessedMonthlyTurnoverPerClient.compareTo(BigDecimal.valueOf(1000)) >= 0;
            }
            log.info("Client client_id {} processed monthly turnover was {} {}", clientId, alreadyProcessedMonthlyTurnoverPerClient, EUR);
            if (monthlyTurnoverOf1000EUROHasBeenReached) {
                // commission is = 0,03 EUR for the current transaction
                rule3Commission = BigDecimal.valueOf(0.03);
            }

            // APPLY COMMISSIONS RULES
            BigDecimal resultCommission;
            CommissionApplied commissionAppliedRule;
            if (isClientId42) {
                if (!monthlyTurnoverOf1000EUROHasBeenReached) {
                    resultCommission = rule2Commission;
                    commissionAppliedRule = CommissionApplied.RULE2;
                } else {
                    if (rule1Commission.compareTo(rule3Commission) == -1) {
                        resultCommission = rule1Commission;
                        commissionAppliedRule = CommissionApplied.RULE1;
                    } else {
                        resultCommission = rule3Commission;
                        commissionAppliedRule = CommissionApplied.RULE3;
                    }
                }
            } else if (monthlyTurnoverOf1000EUROHasBeenReached) {
                resultCommission = rule3Commission;
                commissionAppliedRule = CommissionApplied.RULE3;
            } else {
                resultCommission = rule1Commission;
                commissionAppliedRule = CommissionApplied.RULE1;
            }
            resultCommission = resultCommission.setScale(2, RoundingMode.CEILING);
            log.info("Client client_id {} applied commision {} = {} EUR on {}", clientId, commissionAppliedRule.toString(), resultCommission, dateYearMonthDay);

            // get current date CommissionType
            CommissionType commissionType = commissionsRepository.findByIdAndDate(clientId, requestedDate);

            BigDecimal processedAmount;
            if (commissionType == null) {
                commissionType = new CommissionType(0, clientId, requestBody.date(), thisTransactionAmountInEUR);
            } else {
                // get processed daily turnover add this transaction amount and save to db
                commissionType.setAmount(commissionType.getAmount().add(thisTransactionAmountInEUR));
            }
            commissionsRepository.save(commissionType);

            return new ResponseEntity<>(new ResponseType(resultCommission, EUR), HttpStatus.CREATED);
        }
    }
}
