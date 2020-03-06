package com.appskimo.ktube.service.impl;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

import com.appskimo.ktube.config.CacheKeyHolder;
import com.appskimo.ktube.service.CacheCleaner;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CacheCleanerImpl implements CacheCleaner {

    @Override
    @CacheEvict(value = {CacheKeyHolder.V1_KEY_IDOLS, CacheKeyHolder.V1_KEY_VIDEOS, CacheKeyHolder.V1_KEY_IMAGES, CacheKeyHolder.V1_KEY_RANK, CacheKeyHolder.V1_KEY_FANCAMS, CacheKeyHolder.V1_KEY_FANCAM_RANK, CacheKeyHolder.V1_KEY_TVSHOWS, CacheKeyHolder.V1_KEY_RADIOS, CacheKeyHolder.V2_KEY_IDOLS, CacheKeyHolder.V2_KEY_VIDEOS, CacheKeyHolder.V2_KEY_IMAGES, CacheKeyHolder.V2_KEY_RANK, CacheKeyHolder.V2_KEY_FANCAMS, CacheKeyHolder.V2_KEY_FANCAM_RANK, CacheKeyHolder.V2_KEY_RADIOS}, allEntries = true)
    public void evictAll() {
        log.info("clean all caches...");
    }

}
