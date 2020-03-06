package com.appskimo.ktube.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.appskimo.ktube.domain.model.Idol;
import com.appskimo.ktube.domain.model.IdolImage;
import com.appskimo.ktube.domain.persist.IdolImageRepository;
import com.appskimo.ktube.domain.persist.IdolRepository;
import com.appskimo.ktube.service.GoogleSearchable;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;

@Component
public class GoogleImageSearcher implements GoogleSearchable {
    // query limits = 100query per day
    @Autowired private IdolRepository idolRepository;
    @Autowired private IdolImageRepository idolImageRepository;

    private static final String apiKey = "xxxxxxxxxx";
    private static final String cseKey = "xxxxxxxxxx";
    protected static final String[] SITES = {"topstarnews.net"}; // , "sportsseoul.com", "sports.donga.com", "sports.chosun.co.kr"

    @Override
    public void search() {
        List<Idol> idols = idolRepository.findAll();
        idols.stream()
        .filter(idol -> idol.getIdolUid().longValue() == 116L)
        .forEach(idol -> {
            idolImageRepository.deleteNoMainByIdolUid(idol.getIdolUid());
            search(idol).forEach(image -> {
                try {
                    idolImageRepository.insert(image);
                } catch (Exception e) {
                }
            });
        });
    }

    private List<IdolImage> search(Idol idol) {
        com.google.api.services.customsearch.Customsearch.Cse.List list = getCesList(idol);
        if (list == null) {
            return Collections.emptyList();
        }

        List<IdolImage> images = new ArrayList<>();
        for (String site : SITES) {
            list.setSiteSearch(site);
            for (long start : new long[] { 1L, 11L, 21L }) {
                list.setStart(start);
                images.addAll(extractImages(idol, list));
            }
        }
        return images;
    }

    protected com.google.api.services.customsearch.Customsearch.Cse.List getCesList(Idol idol) {
        Customsearch customsearch = new Customsearch.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
            @Override public void initialize(HttpRequest request) throws IOException { }
        }).setApplicationName("googleImageSearcher").build();
        com.google.api.services.customsearch.Customsearch.Cse.List list = null;
        try {
            list = customsearch.cse().list(idol.getSearchName());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (list == null) {
            return null;
        }

        list.setKey(apiKey);
        list.setCx(cseKey);
        list.setSearchType("image");
        list.setImgType("photo");
        list.setImgSize("xxlarge");

        return list;
    }

    protected List<IdolImage> extractImages(Idol idol, com.google.api.services.customsearch.Customsearch.Cse.List list) {
        Search results = null;
        try {
            results = list.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (results == null) {
            return Collections.emptyList();
        }

        List<Result> items = results.getItems();
        if (items == null) {
            return Collections.emptyList();
        }

        List<IdolImage> images = new ArrayList<>();
        for (Result result : items) {
            com.google.api.services.customsearch.model.Result.Image gImage = result.getImage();
            int height = gImage.getHeight();
            int width = gImage.getWidth();
            String url = result.getLink();

            Integer tHeight = gImage.getThumbnailHeight();
            Integer tWidth = gImage.getThumbnailWidth();
            String tLink = gImage.getThumbnailLink();

            String contextLink = gImage.getContextLink();

            images.add(new IdolImage(idol.getIdolUid(), contextLink, url, width, height, tLink, tWidth, tHeight));
        }

        return images;
    }
}

