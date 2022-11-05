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
//		extends AbstractIntegrationTests
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
	static void beforeAll(){
		calendar.set(Calendar.YEAR, 2021);
		calendar.set(Calendar.MONTH, 1);
		calendar.set(Calendar.DATE, 1);
	}

	@Test
	void integrationTestsForSameDay() throws InterruptedException {

		commisionsRepository.deleteAll();
		url = "http://localhost:" + port + "/transaction";

		try {
			RequestType requestTypeRule1Apply1stClient = new RequestType(calendar.getTime(), BigDecimal.valueOf(10000), amountCurrency, 1);
			HttpEntity<RequestType> httpEntityRule1Apply1stClient = new HttpEntity<>(requestTypeRule1Apply1stClient);
			ResponseEntity<ResponseType> responseEntityRule1Apply1stClient = testRestTemplate.exchange(url, HttpMethod.POST, httpEntityRule1Apply1stClient, ResponseType.class);
			Assertions.assertEquals(HttpStatus.CREATED, responseEntityRule1Apply1stClient.getStatusCode());
			Assertions.assertEquals(BigDecimal.valueOf(11.09), ((ResponseType) responseEntityRule1Apply1stClient.getBody()).amount());

			RequestType requestTypeRule1Apply2ndClient = new RequestType(calendar.getTime(), BigDecimal.valueOf(2000), amountCurrency, 2);
			HttpEntity<RequestType> httpEntityRule1Apply2ndClient = new HttpEntity<>(requestTypeRule1Apply2ndClient);
			ResponseEntity<ResponseType> responseEntityRule1Apply2ndClient = testRestTemplate.exchange(url, HttpMethod.POST, httpEntityRule1Apply2ndClient, ResponseType.class);
			Assertions.assertEquals(HttpStatus.CREATED, responseEntityRule1Apply2ndClient.getStatusCode());
			Assertions.assertEquals(BigDecimal.valueOf(2.22), ((ResponseType) responseEntityRule1Apply2ndClient.getBody()).amount());

			RequestType requestTypeRule3Apply1stClient = new RequestType(calendar.getTime(), BigDecimal.valueOf(100), amountCurrency, 1);
			HttpEntity<RequestType> httpEntityRule3Apply1stClient = new HttpEntity<>(requestTypeRule3Apply1stClient);
			ResponseEntity<ResponseType> responseEntityRule3Apply1stClient = testRestTemplate.exchange(url, HttpMethod.POST, httpEntityRule3Apply1stClient, ResponseType.class);
			Assertions.assertEquals(HttpStatus.CREATED, responseEntityRule3Apply1stClient.getStatusCode());
			Assertions.assertEquals(BigDecimal.valueOf(0.03), ((ResponseType) responseEntityRule3Apply1stClient.getBody()).amount());

			RequestType requestTypeRule2Apply4thClient = new RequestType(calendar.getTime(), BigDecimal.valueOf(30000), amountCurrency, 42);
			HttpEntity<RequestType> httpEntityRule2Apply4thClient = new HttpEntity<>(requestTypeRule2Apply4thClient);
			ResponseEntity<ResponseType> responseEntityRule2Apply4thClient = testRestTemplate.exchange(url, HttpMethod.POST, httpEntityRule2Apply4thClient, ResponseType.class);
			Assertions.assertEquals(HttpStatus.CREATED, responseEntityRule2Apply4thClient.getStatusCode());
			Assertions.assertEquals(BigDecimal.valueOf(0.05), ((ResponseType) responseEntityRule2Apply4thClient.getBody()).amount());

			RequestType requestTypeRule2ApplyButRule3IsLower4thClient = new RequestType(calendar.getTime(), BigDecimal.valueOf(99), amountCurrency, 42);
			HttpEntity<RequestType> httpEntityRule2ApplyButRule3IsLower4thClient = new HttpEntity<>(requestTypeRule2ApplyButRule3IsLower4thClient);
			ResponseEntity<ResponseType> responseEntityRule2ApplyButRule3IsLower4thClient = testRestTemplate.exchange(url, HttpMethod.POST, httpEntityRule2ApplyButRule3IsLower4thClient, ResponseType.class);
			Assertions.assertEquals(HttpStatus.CREATED, responseEntityRule2ApplyButRule3IsLower4thClient.getStatusCode());
			Assertions.assertEquals(BigDecimal.valueOf(0.03), ((ResponseType) responseEntityRule2ApplyButRule3IsLower4thClient.getBody()).amount());
		}catch (Exception e){
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
			RequestType requestTypeRule2Apply = new RequestType(calendar.getTime(), BigDecimal.valueOf(30000), amountCurrency, clientId);
			HttpEntity<RequestType> httpEntityRule2Apply = new HttpEntity<>(requestTypeRule2Apply);
			ResponseEntity<ResponseType> responseEntityRule2Apply = testRestTemplate.exchange(url, HttpMethod.POST, httpEntityRule2Apply, ResponseType.class);
			Assertions.assertEquals(HttpStatus.CREATED, responseEntityRule2Apply.getStatusCode());
			Assertions.assertEquals(BigDecimal.valueOf(0.05), ((ResponseType) responseEntityRule2Apply.getBody()).amount());

			calendar.set(Calendar.DATE, 2);
			RequestType requestTypeRule2ApplyButRule3IsLower = new RequestType(calendar.getTime(), BigDecimal.valueOf(100), amountCurrency, clientId);
			HttpEntity<RequestType> httpEntityRule2ApplyButRule3IsLower = new HttpEntity<>(requestTypeRule2ApplyButRule3IsLower);
			ResponseEntity<ResponseType> responseEntityRule2ApplyButRule3IsLower = testRestTemplate.exchange(url, HttpMethod.POST, httpEntityRule2ApplyButRule3IsLower, ResponseType.class);
			Assertions.assertEquals(HttpStatus.CREATED, responseEntityRule2ApplyButRule3IsLower.getStatusCode());
			Assertions.assertEquals(BigDecimal.valueOf(0.03), ((ResponseType) responseEntityRule2ApplyButRule3IsLower.getBody()).amount());


			calendar.set(Calendar.DATE, 3);
			RequestType requestTypeRule2ApplyButRule3IsLowerForDifferentDay = new RequestType(calendar.getTime(), BigDecimal.valueOf(200), amountCurrency, clientId);
			HttpEntity<RequestType> httpEntityRule2ApplyButRule3IsLowerForDifferentDay = new HttpEntity<>(requestTypeRule2ApplyButRule3IsLowerForDifferentDay);
			ResponseEntity<ResponseType> responseEntityRule2ApplyButRule3IsLowerForDifferentDay = testRestTemplate.exchange(url, HttpMethod.POST, httpEntityRule2ApplyButRule3IsLowerForDifferentDay, ResponseType.class);
			Assertions.assertEquals(HttpStatus.CREATED, responseEntityRule2ApplyButRule3IsLowerForDifferentDay.getStatusCode());
			Assertions.assertEquals(BigDecimal.valueOf(0.03), ((ResponseType) responseEntityRule2ApplyButRule3IsLowerForDifferentDay.getBody()).amount());

			calendar.set(Calendar.MONTH, 3);
			RequestType requestTypeRule2ApplyForDifferentMonth = new RequestType(calendar.getTime(), BigDecimal.valueOf(100), amountCurrency, clientId);
			HttpEntity<RequestType> httpEntityRule2ApplyForDifferentMonth = new HttpEntity<>(requestTypeRule2ApplyForDifferentMonth);
			ResponseEntity<ResponseType> responseEntityRule2ApplyForDifferentMonth = testRestTemplate.exchange(url, HttpMethod.POST, httpEntityRule2ApplyForDifferentMonth, ResponseType.class);
			Assertions.assertEquals(HttpStatus.CREATED, responseEntityRule2ApplyForDifferentMonth.getStatusCode());
			Assertions.assertEquals(BigDecimal.valueOf(0.05), ((ResponseType) responseEntityRule2ApplyForDifferentMonth.getBody()).amount());
		} catch (Exception e){
			Assertions.fail();
			commisionsRepository.deleteAll();
			e.printStackTrace();
		}
		commisionsRepository.deleteAll();
	}
}
