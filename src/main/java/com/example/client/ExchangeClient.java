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

import java.util.Date;

@Slf4j
@CacheConfig(cacheNames = "rates")
@Component

public class ExchangeClient {
    @Autowired
    private RestTemplate restTemplate;

    @Cacheable(value = "rates")
    public ResponseEntity<RateDto> getRates(Date date) {
        log.info("Retrieving rates from external API");
        return restTemplate.exchange("https://api.exchangerate.host/" + date, HttpMethod.GET, HttpEntity.EMPTY, RateDto.class);
    }
}
