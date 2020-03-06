package com.appskimo.ktube.service.impl;

import java.net.URI;
import java.util.Calendar;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.appskimo.ktube.service.ScheduledService;
import com.appskimo.ktube.service.VideoService;
import com.appskimo.ktube.service.YouTubeCollectable;

@Service
@Profile("product")
public class ScheduledServiceImpl implements ScheduledService {
    @Autowired private YouTubeCollectable youTubeCollector;
    @Autowired private VideoService videoService;
//    @Autowired private NaverSearchable naverSearchable;
    @Autowired private RestTemplate restTemplate;

    @Value("#{taskExecutor}") private ThreadPoolTaskExecutor taskExecutor;

    private static final String URL_CACHE_CLEAN = "https://api.some.domain/api/cache/clean";

    @Override
    @Scheduled(cron="0 30 17 * * ?")
    public void collectNewVideos() {
        youTubeCollector.collectNewVideos();
        waitUntilDone();
        youTubeCollector.cleanByTitleIsNull();
    }

    @Override
    @Scheduled(cron="0 30 20 * * ?")
    public void collectNewVideos2() {
        youTubeCollector.collectNewLyricsVideos();
        youTubeCollector.collectNewKaraokeVideos();
        youTubeCollector.collectNewFancamVideos();

        waitUntilDone();
        youTubeCollector.cleanByTitleIsNull();
    }

    @Override
    @Scheduled(cron="0 05 0 * * ?")
    public void updateRank() {
        youTubeCollector.updateViewCount();
        videoService.updateRankDaily();

        youTubeCollector.updateKaraokeViewCount();
        videoService.updateKaraokeRankDaily();

        youTubeCollector.updateFancamViewCount();
        videoService.updateFancamRankDaily();

        updateWeekly();
        updateMonthly();
        cleanApiCache();
    }

    private void waitUntilDone() {
        do {
            try {
                Thread.sleep(10 * 3000L);
            } catch (InterruptedException e) {}
        } while(taskExecutor.getActiveCount() > 0);
    }

    private void updateWeekly() {
        if (Calendar.getInstance(Locale.KOREA).get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            videoService.updateRankWeekly();
            videoService.updateKaraokeRankWeekly();
            videoService.updateFancamRankWeekly();
        }
    }

    private void updateMonthly() {
        if (Calendar.getInstance(Locale.KOREA).get(Calendar.DAY_OF_MONTH) == 1) {
            videoService.updateRankMonthly();
            videoService.updateKaraokeRankMonthly();
            videoService.updateFancamRankMonthly();
        }
    }

    private String cleanApiCache() {
        ResponseEntity<String> response = restTemplate.exchange(URI.create(URL_CACHE_CLEAN), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
        return response != null ? response.getBody() : null;
    }
}
