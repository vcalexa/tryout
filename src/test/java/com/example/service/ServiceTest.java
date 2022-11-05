package com.example.service;

import com.example.data.CommissionType;
import com.example.data.RequestType;
import com.example.data.ResponseType;
import com.example.repository.CommisionsRepository;
import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

@WebMvcTest(Service.class)
class ServiceTest {

	@MockBean
	private RestTemplate restTemplate;

	@Autowired
	private Service service;

	@MockBean
	private CommisionsRepository commissionsRepository;

	String responseBody = """
			{
				"motd": {
					"msg": "If you or your company use this project or like what we doing, please consider backing us so we can continue maintaining and evolving this project.",
					"url": "https://exchangerate.host/#/donate"
				},
				"success": true,
				"historical": true,
				"base": "EUR",
				"date": "2021-01-01",
				"rates": {
					"AED": 4.472422,
					"ALL": 123.368069,
					"PLN":4.570522
				}
			}
			""";

	private final String amountCurrency = "PLN";

	static Date requestedDate;
	static String dateYearMonth;
	private final Map responseBodyMap = new Gson().fromJson(responseBody, Map.class);

	@BeforeAll
	static void beforeAll(){
		requestedDate = new Date(2021,1,1);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
		 dateYearMonth = simpleDateFormat.format(requestedDate);
	}

	@Test
	void addTransactionRule1CommissionApply_UnitTest() {
		int clientId = 123;
		int consumedTurnoverPerMonth = 500;
		int consumedTurnoverForCurrentDay = 50;
		int currentTurnover = 100;
		double expectedCommission = 0.11;

		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Mockito.any(Class.class))).thenReturn(new ResponseEntity <Object>(responseBodyMap, HttpStatus.OK));
		Mockito.when(commissionsRepository.getClientSumOfTurnoverPerMonth(Mockito.anyInt(), Mockito.anyString())).thenReturn(BigDecimal.valueOf(consumedTurnoverPerMonth));
		Mockito.when(commissionsRepository.findByIdAndDate(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(new CommissionType(0, clientId, requestedDate, BigDecimal.valueOf(consumedTurnoverForCurrentDay)));

		ResponseEntity<?> responseEntity = service.addTransaction(new RequestType(requestedDate, BigDecimal.valueOf(currentTurnover), amountCurrency, clientId));

		Assertions.assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
		Assertions.assertEquals(BigDecimal.valueOf(expectedCommission), ((ResponseType)responseEntity.getBody()).amount());
	}

	@Test
	void addTransactionRule2CommissionApply_UnitTest() {
		int clientId = 42;
		int consumedTurnoverPerMonth = 500;
		int consumedTurnoverForCurrentDay = 50;
		int currentTurnover = 100;
		double expectedCommission = 0.05;

		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Mockito.any(Class.class))).thenReturn(new ResponseEntity <Object>(responseBodyMap, HttpStatus.OK));
		Mockito.when(commissionsRepository.getClientSumOfTurnoverPerMonth(Mockito.anyInt(),Mockito.anyString() )).thenReturn(BigDecimal.valueOf(consumedTurnoverPerMonth));
		Mockito.when(commissionsRepository.findByIdAndDate(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(new CommissionType(0,clientId, requestedDate, BigDecimal.valueOf(consumedTurnoverForCurrentDay)));

		ResponseEntity<?> responseEntity = service.addTransaction(new RequestType(requestedDate, BigDecimal.valueOf(currentTurnover), amountCurrency, clientId));

		Assertions.assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
		Assertions.assertEquals(BigDecimal.valueOf(expectedCommission), ((ResponseType)responseEntity.getBody()).amount());
	}

	@Test
	void addTransactionRule2CommissionApplyButRule3IsLowerThenApplied_UnitTest() {
		int clientId = 42;
		int consumedTurnoverPerMonth = 1000;
		int consumedTurnoverForCurrentDay = 0;
		int currentTurnover = 100;
		double expectedCommission = 0.03;

		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Mockito.any(Class.class))).thenReturn(new ResponseEntity <Object>(responseBodyMap, HttpStatus.OK));
		Mockito.when(commissionsRepository.getClientSumOfTurnoverPerMonth(Mockito.anyInt(),Mockito.anyString() )).thenReturn(BigDecimal.valueOf(consumedTurnoverPerMonth));
		Mockito.when(commissionsRepository.findByIdAndDate(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(new CommissionType(0,clientId, requestedDate, BigDecimal.valueOf(consumedTurnoverForCurrentDay)));

		ResponseEntity<?> responseEntity = service.addTransaction(new RequestType(requestedDate, BigDecimal.valueOf(currentTurnover), amountCurrency, clientId));

		Assertions.assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
		Assertions.assertEquals(BigDecimal.valueOf(expectedCommission), ((ResponseType)responseEntity.getBody()).amount());
	}

	@Test
	void addTransactionRule3CommissionApply_UnitTest() {
		int clientId = 123;
		int consumedTurnoverPerMonth = 1001;
		int consumedTurnoverForCurrentDay = 50;
		int currentTurnover = 100;
		double expectedCommission = 0.03;

		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Mockito.any(Class.class))).thenReturn(new ResponseEntity <Object>(responseBodyMap, HttpStatus.OK));
		Mockito.when(commissionsRepository.getClientSumOfTurnoverPerMonth(Mockito.anyInt(),Mockito.anyString() )).thenReturn(BigDecimal.valueOf(consumedTurnoverPerMonth));
		Mockito.when(commissionsRepository.findByIdAndDate(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(new CommissionType(0,clientId, requestedDate, BigDecimal.valueOf(consumedTurnoverForCurrentDay)));

		ResponseEntity<?> responseEntity = service.addTransaction(new RequestType(requestedDate, BigDecimal.valueOf(currentTurnover), amountCurrency, clientId));

		Assertions.assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
		Assertions.assertEquals(BigDecimal.valueOf(expectedCommission), ((ResponseType)responseEntity.getBody()).amount());
	}
}
