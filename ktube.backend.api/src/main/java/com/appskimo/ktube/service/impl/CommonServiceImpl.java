package com.appskimo.ktube.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.appskimo.ktube.config.CacheKeyHolder;
import com.appskimo.ktube.domain.model.RadioStation;
import com.appskimo.ktube.domain.persist.RadioStationRepository;
import com.appskimo.ktube.service.RadioStationService;

@Component
@Transactional(readOnly = true)
public class CommonServiceImpl implements RadioStationService {

    @Autowired private RadioStationRepository radioStationRepository;

    @Override
    @Cacheable(value = CacheKeyHolder.V1_KEY_RADIOS, key = "#root.methodName")
    public List<RadioStation> findAll() {
        return radioStationRepository.findAll();
    }

}
