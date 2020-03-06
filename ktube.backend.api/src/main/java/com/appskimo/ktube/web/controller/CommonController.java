package com.appskimo.ktube.web.controller;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.appskimo.ktube.service.CacheCleaner;
import com.appskimo.ktube.service.RadioStationService;

@RestController
@RequestMapping(value = "api", produces = MediaType.APPLICATION_JSON_VALUE)
public class CommonController {
    @Autowired private RadioStationService radioStationService;
    @Autowired private CacheCleaner cacheCleaner;

    @RequestMapping(value = "cache/clean", method = RequestMethod.GET)
    public Callable<Object> idols() {
        return () -> {
            cacheCleaner.evictAll();
            return null;
        };
    }

    private ResponseEntity<Object> responseCache(Object body, long seconds) {
        CacheControl cacheControl = CacheControl.maxAge(seconds, TimeUnit.SECONDS).cachePublic();
        return ResponseEntity.ok().cacheControl(cacheControl).body(body);
    }

    @RequestMapping(value = "radios", method = RequestMethod.GET)
    public Object radios() {
        return responseCache(radioStationService.findAll(), 2592000L);
    }
}
