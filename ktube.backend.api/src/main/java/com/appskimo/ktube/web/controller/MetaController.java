package com.appskimo.ktube.web.controller;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.appskimo.ktube.domain.model.App;
import com.appskimo.ktube.service.MetaService;

@RestController
@RequestMapping(value = "api", produces = MediaType.APPLICATION_JSON_VALUE)
public class MetaController {
    @Autowired private MetaService metaService;

    private ResponseEntity<Object> responseCache(Object body, long seconds) {
        CacheControl cacheControl = CacheControl.maxAge(seconds, TimeUnit.SECONDS).cachePublic();
        return ResponseEntity.ok().cacheControl(cacheControl).body(body);
    }

    @RequestMapping(value = "meta/{app}", method = RequestMethod.GET)
    public Object meta(@PathVariable("app") String app) {
        try {
            return responseCache(metaService.getMeta(App.valueOf(app.toUpperCase())), 7200L);
        } catch (Exception e) {
            return null;
        }
    }
}
