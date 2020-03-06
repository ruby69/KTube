package com.appskimo.ktube.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.appskimo.ktube.domain.model.Idol;
import com.appskimo.ktube.domain.model.IdolImage;
import com.appskimo.ktube.domain.persist.IdolImageRepository;
import com.appskimo.ktube.domain.persist.IdolRepository;

@Component
public class GoogleImageRecentlySearcher extends GoogleImageSearcher {
    @Autowired private IdolRepository idolRepository;
    @Autowired private IdolImageRepository idolImageRepository;

    private Set<String> existedIdolImageUrlSet;

    @Override
    public void search() {
        loadExistedIdolImageUrlSet();

        List<Idol> idols = idolRepository.findAll();
        idols.stream().forEach(idol -> {
            search(idol).forEach(image -> {
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

    private List<IdolImage> search(Idol idol) {
        com.google.api.services.customsearch.Customsearch.Cse.List list = getCesList(idol);
        if (list == null) {
            return Collections.emptyList();
        }

        return extractImages(idol, list).stream().filter(image -> !existedIdolImageUrlSet.contains(image.getUrl())).collect(Collectors.toList());
    }

    @Override
    protected com.google.api.services.customsearch.Customsearch.Cse.List getCesList(Idol group) {
        com.google.api.services.customsearch.Customsearch.Cse.List list = super.getCesList(group);
        if (list == null) {
            return null;
        }
        list.setExcludeTerms("YouTube vlive lyrics");
        list.setDateRestrict("d10");
        list.setImgSize(null);
        return list;
    }

}

