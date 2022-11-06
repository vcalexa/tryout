package com.example.client;

import com.example.data.RateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@CacheConfig(cacheNames = "rates")
@Component

public class ExchangeClient {
    @Autowired
    private RestTemplate restTemplate;

    @Cacheable(value = "rates")
    public ResponseEntity<RateDto> getRates(Date requestedDate) {
        log.info("Retrieving rates from external API");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateYearMonthDay = simpleDateFormat.format(requestedDate);
        return restTemplate.exchange("https://api.exchangerate.host/" + dateYearMonthDay, HttpMethod.GET, HttpEntity.EMPTY, RateDto.class);
    }
}
