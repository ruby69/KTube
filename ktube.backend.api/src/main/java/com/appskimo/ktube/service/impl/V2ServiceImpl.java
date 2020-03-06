package com.appskimo.ktube.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.appskimo.ktube.config.CacheKeyHolder;
import com.appskimo.ktube.domain.model.Idol;
import com.appskimo.ktube.domain.model.IdolInfo;
import com.appskimo.ktube.domain.model.Overview;
import com.appskimo.ktube.domain.model.Page;
import com.appskimo.ktube.domain.persist.IdolRepository;
import com.appskimo.ktube.domain.persist.MetaRepository;
import com.appskimo.ktube.domain.persist.V2YouTubeRepository;
import com.appskimo.ktube.service.V2Service;

@Component
@Transactional(readOnly = true)
public class V2ServiceImpl implements V2Service {
    @Autowired private MetaRepository metaRepository;
    @Autowired private IdolRepository idolRepository;
    @Autowired private V2YouTubeRepository v2YouTubeRepository;

    @Override
    @Cacheable(value = CacheKeyHolder.V2_KEY_VIDEOS, key = "#root.methodName + '_' + #page.cacheKey")
    public Page populateVideos(Page page) {
        page.setTotal(v2YouTubeRepository.countVideosByPage(page));
        page.setContents(v2YouTubeRepository.findVideosByPage(page));
        return page;
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V2_KEY_VIDEOS, key = "#root.methodName + '_' + #page.cacheKey")
    public Page populateMillions(Page page) {
        page.setTotal(v2YouTubeRepository.countMillionVideosByPage(page));
        page.setContents(v2YouTubeRepository.findMillionVideosByPage(page));
        return page;
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V2_KEY_VIDEOS, key = "#root.methodName + '_' + #page.cacheKey")
    public Page populateVideosByIdol(Page page) {
        page.setTotal(v2YouTubeRepository.countIdolVideosByPage(page));
        page.setContents(v2YouTubeRepository.findIdolVideosByPage(page));
        return page;
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V2_KEY_RANK, key = "#root.methodName + '_' + #page.cacheKey")
    public Page populateLastNDaysBestVideos(Page page) {
        page.setTotal(v2YouTubeRepository.countLastNDaysBestVideosByPage(page));
        page.setContents(v2YouTubeRepository.findLastNDaysBestVideosByPage(page));
        return page;
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V2_KEY_FANCAMS, key = "#root.methodName + '_' + #page.cacheKey")
    public Page populateFancams(Page page) {
        page.setTotal(v2YouTubeRepository.countFancamVideosByPage(page));
        page.setContents(v2YouTubeRepository.findFancamVideosByPage(page));
        return page;
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V2_KEY_FANCAM_RANK, key = "#root.methodName + '_' + #page.cacheKey")
    public Page populateLastNDaysBestFancams(Page page) {
        page.setTotal(v2YouTubeRepository.countLastNDaysBestFancamsByPage(page));
        page.setContents(v2YouTubeRepository.findLastNDaysBestFancamsByPage(page));
        return page;
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V2_KEY_KARAOKE, key = "#root.methodName + '_' + #page.cacheKey")
    public Page populateKaraokes(Page page) {
        page.setTotal(v2YouTubeRepository.countKaraokeVideosByPage(page));
        page.setContents(v2YouTubeRepository.findKaraokeVideosByPage(page));
        return page;
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V2_KEY_KARAOKE, key = "#root.methodName + '_' + #page.cacheKey")
    public Page populateKaraokesByIdol(Page page) {
        page.setTotal(v2YouTubeRepository.countIdolKaraokeVideosByPage(page));
        page.setContents(v2YouTubeRepository.findIdolKaraokeVideosByPage(page));
        return page;
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V2_KEY_KARAOKE_RANK, key = "#root.methodName + '_' + #page.cacheKey")
    public Page populateLastNDaysBestKaraokes(Page page) {
        page.setTotal(v2YouTubeRepository.countLastNDaysBestKaraokesByPage(page));
        page.setContents(v2YouTubeRepository.findLastNDaysBestKaraokesByPage(page));
        return page;
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V2_KEY_LYRICS, key = "#root.methodName + '_' + #page.cacheKey")
    public Page populateLyricsVideos(Page page) {
        page.setTotal(v2YouTubeRepository.countLyricsVideosByPage(page));
        page.setContents(v2YouTubeRepository.findLyricsVideosByPage(page));
        return page;
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V2_KEY_SEARCH, key = "#root.methodName + '_' + #page.cacheKey")
    public Page search(Page page) {
        page.setTotal(v2YouTubeRepository.countLikeTitle(page));
        page.setContents(v2YouTubeRepository.findLikeTitle(page));
        return page;
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V2_KEY_IDOLS, key = "#root.methodName + '_' + #lang")
    public List<Idol> getBestIdols(String lang) {
        return idolRepository.findByBest(lang);
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V2_KEY_VIDEOS, key = "#root.methodName + '_' + #lang")
    public Overview getOverview(String lang) {
        Overview overview = new Overview();

        Page page = new Page();
        page.setScale(5);
        overview.setNews(populateVideos(page).getContents());

        page.clear().param("vc", 15);
        overview.setMillions(populateMillions(page).getContents());

        page.clear().param("day", 1);
        page.setScale(6);
        overview.setVideos(populateLastNDaysBestVideos(page).getContents());

        page.setScale(3);
        overview.setFancams(populateLastNDaysBestFancams(page).getContents());
        overview.setKaraokes(populateLastNDaysBestKaraokes(page).getContents());

        page.clear().param("order", "view");
        overview.setLyrics(populateLyricsVideos(page).getContents());

        overview.setIdols(getBestIdols(IdolInfo.Lang.resolveCode(lang)));

        return overview;
    }

    @Deprecated
    @Override
    public Object getMeta() {
        return metaRepository.findByLatest();
    }
}
