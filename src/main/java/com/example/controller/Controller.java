package com.example.controller;

import com.example.data.RequestType;
import com.example.data.ResponseType;
import com.example.service.Service;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(value = "Commission microservice")
public class Controller {

    @Autowired
    Service service;

    @ApiOperation("Add transaction")
    @PostMapping(value = "/transaction")
    public @ResponseBody ResponseEntity<Object> addTransaction(@RequestBody RequestType body) {
        return service.addTransaction(body);
    }
}
