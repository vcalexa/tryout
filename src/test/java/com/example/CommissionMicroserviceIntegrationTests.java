package com.example;

import com.example.data.RequestType;
import com.example.data.ResponseType;
import com.example.repository.CommisionsRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Calendar;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class CommissionMicroserviceIntegrationTests
{
    @Autowired
    TestRestTemplate testRestTemplate;

    @LocalServerPort
    int port;

    private static Calendar calendar = Calendar.getInstance();

    @Autowired
    CommisionsRepository commisionsRepository;

    String amountCurrency = "PLN";
    String url;

    @BeforeAll
    static void beforeAll() {
        calendar.set(Calendar.YEAR, 2021);
        calendar.set(Calendar.MONTH, 1);
        calendar.set(Calendar.DATE, 1);
    }

    @Test
    void integrationTestsForSameDay() throws InterruptedException {

        commisionsRepository.deleteAll();
        url = "http://localhost:" + port + "/transaction";

        try {
            ResponseEntity<ResponseType> responseEntityRule1Apply1stClient = getResponseFromEndpoint(BigDecimal.valueOf(10000), 1);
            verifyResponseStatusAndBody(BigDecimal.valueOf(11.09),responseEntityRule1Apply1stClient);

            ResponseEntity<ResponseType> responseEntityRule1Apply2ndClient = getResponseFromEndpoint(BigDecimal.valueOf(2000), 2);
            verifyResponseStatusAndBody(BigDecimal.valueOf(2.22), responseEntityRule1Apply2ndClient);

            ResponseEntity<ResponseType> responseEntityRule3Apply1stClient = getResponseFromEndpoint(BigDecimal.valueOf(100), 1);
            verifyResponseStatusAndBody(BigDecimal.valueOf(0.03), responseEntityRule3Apply1stClient);

            ResponseEntity<ResponseType> responseEntityRule2Apply4thClient = getResponseFromEndpoint(BigDecimal.valueOf(30000), 42);
            verifyResponseStatusAndBody(BigDecimal.valueOf(0.05),responseEntityRule2Apply4thClient);

            ResponseEntity<ResponseType> responseEntityRule2ApplyButRule3IsLower4thClient = getResponseFromEndpoint(BigDecimal.valueOf(100), 42);
            verifyResponseStatusAndBody(BigDecimal.valueOf(0.03), responseEntityRule2ApplyButRule3IsLower4thClient);
        } catch (Exception e) {
            Assertions.fail();
            commisionsRepository.deleteAll();
            e.printStackTrace();
        }
        commisionsRepository.deleteAll();
        Thread.sleep(5000);
    }

    @Test
    void integrationTestsWithMoreDates() {

        commisionsRepository.deleteAll();
        url = "http://localhost:" + port + "/transaction";

        int clientId = 42;

        try {
            calendar.set(Calendar.DATE, 1);
            ResponseEntity<ResponseType> responseEntityRule2Apply = getResponseFromEndpoint(BigDecimal.valueOf(30000), clientId);
            verifyResponseStatusAndBody(BigDecimal.valueOf(0.05), responseEntityRule2Apply);

            calendar.set(Calendar.DATE, 2);
            ResponseEntity<ResponseType> responseEntityRule2ApplyButRule3IsLower = getResponseFromEndpoint(BigDecimal.valueOf(100), clientId);
            verifyResponseStatusAndBody(BigDecimal.valueOf(0.03), responseEntityRule2ApplyButRule3IsLower);

            calendar.set(Calendar.DATE, 3);
            ResponseEntity<ResponseType> responseEntityRule2ApplyButRule3IsLowerForDifferentDay = getResponseFromEndpoint(BigDecimal.valueOf(200), clientId);
            verifyResponseStatusAndBody(BigDecimal.valueOf(0.03), responseEntityRule2ApplyButRule3IsLowerForDifferentDay);

            calendar.set(Calendar.MONTH, 3);
            ResponseEntity<ResponseType> responseEntityRule2ApplyForDifferentMonth = getResponseFromEndpoint(BigDecimal.valueOf(100), clientId);
            verifyResponseStatusAndBody(BigDecimal.valueOf(0.05), responseEntityRule2ApplyForDifferentMonth);
        } catch (Exception e) {
            Assertions.fail();
            commisionsRepository.deleteAll();
            e.printStackTrace();
        }
        commisionsRepository.deleteAll();
    }

    private void verifyResponseStatusAndBody(BigDecimal expectedValue, ResponseEntity<ResponseType> response) {
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assertions.assertEquals(expectedValue, (response.getBody()).amount());
    }

    private ResponseEntity<ResponseType> getResponseFromEndpoint(BigDecimal amount, int id) {
        RequestType requestType = new RequestType(calendar.getTime(), amount, amountCurrency, id);
        HttpEntity<RequestType> httpEntityRule = new HttpEntity<>(requestType);
        return testRestTemplate.exchange(url, HttpMethod.POST, httpEntityRule, ResponseType.class);
    }
}
