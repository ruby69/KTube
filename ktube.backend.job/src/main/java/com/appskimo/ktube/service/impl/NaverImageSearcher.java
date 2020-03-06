package com.appskimo.ktube.service.impl;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.appskimo.ktube.domain.model.Idol;
import com.appskimo.ktube.domain.model.IdolImage;
import com.appskimo.ktube.domain.model.Naver;
import com.appskimo.ktube.domain.persist.IdolImageRepository;
import com.appskimo.ktube.domain.persist.IdolRepository;
import com.appskimo.ktube.service.NaverSearchable;

@Component
public class NaverImageSearcher implements NaverSearchable {

    @Autowired private IdolRepository idolRepository;
    @Autowired private IdolImageRepository idolImageRepository;
    @Autowired private RestTemplate restTemplate;
    @Value("#{taskExecutor}") private ThreadPoolTaskExecutor taskExecutor;

    private static final String CLIENT_ID = "xxxxxxxxxx";
    private static final String CLIENT_SECRET = "xxxxxxxxxx";
    private static final String URL_FORMAT = "https://openapi.naver.com/v1/search/image.json?query=%s&display=%s&start=%s&sort=%s&filter=large";

    private Set<String> existedIdolImageUrlSet;

    @Override
    public void searchAll() {
        List<Idol> idols = idolRepository.findAll();
        idols
//        .stream().filter(idol -> idol.getIdolUid().longValue() > 71L)
        .forEach(idol -> {
            taskExecutor.execute(() -> {
                List<IdolImage> images = search(idol);
                for(int i = images.size()-1; i>=0; i--) {
                    try {
                        idolImageRepository.insert(images.get(i));
                    } catch (Exception e) {
                    }
                }
            });
        });
    }

    private List<IdolImage> search(Idol idol) {
        String text = null;
        try {
            text = URLEncoder.encode("\"HD포토\"" + idol.getSearchName(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            text = null;
        }

        if(text == null) {
            return Collections.emptyList();
        } else {
            List<IdolImage> images = new ArrayList<>();

            int start = 1;
            int total = 1;
            int size = 100;
            do {
                String str = String.format(URL_FORMAT, text, size, start, "sim", "all");
                Naver.Image response = execute(URI.create(str), null, Naver.Image.class);
                total = response.getTotal();
                images.addAll(response.getItems().stream().map(item -> item.toIdolImage(idol.getIdolUid())).collect(Collectors.toList()));
                start += size;
            } while(start < total && start < 1000);
            return images;
        }
    }

    private HttpEntity<?> getEntity(Object params) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("X-Naver-Client-Id", CLIENT_ID);
        httpHeaders.add("X-Naver-Client-Secret", CLIENT_SECRET);
        return params != null ? new HttpEntity<>(params, httpHeaders) : new HttpEntity<>(httpHeaders);
    }

    private <T> T execute(URI uri, Object params, Class<T> clazz) {
        ResponseEntity<T> response = restTemplate.exchange(uri, HttpMethod.GET, getEntity(params), clazz);
        return response != null ? response.getBody() : null;
    }



    @Override
    public void searchDaily() {
        loadExistedIdolImageUrlSet();
        List<Idol> idols = idolRepository.findAll();
        idols
//        .stream().filter(idol -> idol.getIdolUid().longValue() > 58L)
        .forEach(idol -> {
            search2(idol).forEach(image -> {
                try {
                    idolImageRepository.insert(image);
                    existedIdolImageUrlSet.add(image.getUrl());
                } catch (Exception e) {
                }
            });
        });
    }

    private void loadExistedIdolImageUrlSet() {
        existedIdolImageUrlSet = idolImageRepository.findAllIdolImagesUrl();
        if (existedIdolImageUrlSet == null) {
            existedIdolImageUrlSet = Collections.emptySet();
        }
    }

    private List<IdolImage> search2(Idol idol) {
        List<IdolImage> images = new ArrayList<>();
        for(String keyword : getKeywords(idol)) {
            int start = 1;
            int size = 50;
            String str = String.format(URL_FORMAT, keyword, size, start, "sim", "large");
            Naver.Image response = execute(URI.create(str), null, Naver.Image.class);
            images.addAll(response.getItems().stream().map(item -> item.toIdolImage(idol.getIdolUid())).filter(image -> !existedIdolImageUrlSet.contains(image.getUrl())).collect(Collectors.toList()));
        }
        return images;
    }

    private List<String> getKeywords(Idol idol) {
        List<String> keywords = new ArrayList<>();
        try {
            keywords.add(URLEncoder.encode("\"HD포토\" \"" + idol.getSearchName() + "\"", "utf-8"));
        } catch (UnsupportedEncodingException e) {
        }

        try {
            keywords.add(URLEncoder.encode("\"화보\" \"" + idol.getSearchName() + "\"", "utf-8"));
        } catch (UnsupportedEncodingException e) {
        }

        return keywords;
    }
}
