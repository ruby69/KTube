package com.appskimo.ktube.service.impl;

import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.appskimo.ktube.config.CacheKeyHolder;
import com.appskimo.ktube.domain.model.Idol;
import com.appskimo.ktube.domain.model.Page;
import com.appskimo.ktube.domain.model.YouTube.V1Video;
import com.appskimo.ktube.domain.persist.IdolImageRepository;
import com.appskimo.ktube.domain.persist.IdolRepository;
import com.appskimo.ktube.domain.persist.V1YouTubeRepository;
import com.appskimo.ktube.service.V1Service;

@Component
@Transactional(readOnly = true)
public class V1ServiceImpl implements V1Service {
    @Autowired private V1YouTubeRepository v1YouTubeRepository;
    @Autowired private IdolRepository idolRepository;
    @Autowired private IdolImageRepository idolImageRepository;

    private static final FastDateFormat fdf = FastDateFormat.getInstance("yyyyMMdd");
    private static final FastDateFormat fdf2 = FastDateFormat.getInstance("yyyyMM");

    @Override
    @Cacheable(value = CacheKeyHolder.V1_KEY_VIDEOS, key = "#root.methodName + '_' + #idolUid + '_' + #page.cacheKey")
    public Page populateVideosByIdol(Long idolUid, Page page) {
        page.getP().put("idolUid", idolUid);
        page.setTotal(v1YouTubeRepository.countIdolVideosByPage(page));
        page.setContents(v1YouTubeRepository.findIdolVideosByPage(page));
        return page;
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V1_KEY_VIDEOS, key = "#root.methodName + '_' + #videoUid")
    public V1Video getVideoByUid(Long videoUid) {
        return v1YouTubeRepository.findVideoByUid(videoUid);
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V1_KEY_VIDEOS, key = "#root.methodName + '_' + #page.cacheKey")
    public Page populateVideos(Page page) {
        page.setTotal(v1YouTubeRepository.countVideosByPage(page));
        page.setContents(v1YouTubeRepository.findVideosByPage(page));
        return page;
    }

    private Page populateBests(Page page) {
      page.setTotal(v1YouTubeRepository.countBestsByPage(page));
      page.setContents(v1YouTubeRepository.findBestsByPage(page));
      return page;
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V1_KEY_RANK, key = "#root.methodName + '_' + #page.cacheKey")
    public Page populateBestsDaily(Page page) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        page.param("tableName", "VideoRankDaily").param("dateIdx", Integer.parseInt(fdf.format(calendar)));
        return populateBests(page);
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V1_KEY_RANK, key = "#root.methodName + '_' + #page.cacheKey")
    public Page populateBestsWeekly(Page page) {
        return populateBestsWeekly(Calendar.getInstance(), page, 0);
    }

    private Page populateBestsWeekly(Calendar calendar, Page page, int recurseCount) {
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        calendar.add(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek() - calendar.get(Calendar.DAY_OF_WEEK));
        page.param("tableName", "VideoRankWeekly").param("dateIdx", Integer.parseInt(fdf.format(calendar)));
        page = populateBests(page);

        if(page.getContents().size() > 0) {
            return page;
        } else if(recurseCount < 3) {
            return populateBestsWeekly(calendar, page, ++recurseCount);
        } else {
            return populateBestsDaily(page);
        }
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V1_KEY_RANK, key = "#root.methodName + '_' + #page.cacheKey")
    public Page populateBestsMonthly(Page page) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        page.param("tableName", "VideoRankMonthly").param("dateIdx", Integer.parseInt(fdf2.format(calendar)));
        page = populateBests(page);
        if(page.getContents().size() < 1) {
            page = sampleMonthly(page);
        }
        return page;
    }

    private Page sampleMonthly(Page page) {
        Calendar calendar = Calendar.getInstance();
        int toIdx = Integer.parseInt(fdf.format(calendar.getTime()));
        page.param("toIdx", toIdx);

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int fromIdx = Integer.parseInt(fdf.format(calendar.getTime()));
        page.param("fromIdx", fromIdx);

        page.setTotal(v1YouTubeRepository.countSampleMonthly(page));
        page.setContents(v1YouTubeRepository.findSampleMonthly(page));
        return page;
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V1_KEY_RANK, key = "#root.methodName + '_' + #page.cacheKey")
    public Page populateBestsLast7Days(Page page) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -8);
        page.param("dateIdx", Integer.parseInt(fdf.format(calendar)));
        return populateLasts(page);
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V1_KEY_RANK, key = "#root.methodName + '_' + #page.cacheKey")
    public Page populateBestsLast30Days(Page page) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -31);
        page.param("dateIdx", Integer.parseInt(fdf.format(calendar)));
        return populateLasts(page);
    }

    private Page populateLasts(Page page) {
      page.setTotal(v1YouTubeRepository.countLastsByPage(page));
      page.setContents(v1YouTubeRepository.findLastsByPage(page));
      return page;
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V1_KEY_RANK, key = "#root.methodName + '_' + #page.cacheKey")
    public Page populateGratestHits(Page page) {
        page.setTotal(v1YouTubeRepository.countOver50000000ByPage(page));
        page.setContents(v1YouTubeRepository.findOver50000000ByPage(page));
        return page;
    }
















    @Override
    @Cacheable(value = CacheKeyHolder.V1_KEY_FANCAMS, key = "#root.methodName + '_' + #page.cacheKey")
    public Page populateFancamVideos(Page page) {
        page.setTotal(v1YouTubeRepository.countFancamVideosByPage(page));
        page.setContents(v1YouTubeRepository.findFancamVideosByPage(page));
        return page;
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V1_KEY_FANCAMS, key = "#root.methodName + '_' + #videoUid")
    public V1Video getFancamVideoByUid(Long videoUid) {
        return v1YouTubeRepository.findFancamVideoByUid(videoUid);
    }

    private Page populateFancamBests(Page page) {
      page.setTotal(v1YouTubeRepository.countFancamBestsByPage(page));
      page.setContents(v1YouTubeRepository.findFancamBestsByPage(page));
      return page;
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V1_KEY_FANCAM_RANK, key = "#root.methodName + '_' + #page.cacheKey")
    public Page populateFancamBestsDaily(Page page) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        page.param("tableName", "FancamVideoRankDaily").param("dateIdx", Integer.parseInt(fdf.format(calendar)));
        return populateFancamBests(page);
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V1_KEY_FANCAM_RANK, key = "#root.methodName + '_' + #page.cacheKey")
    public Page populateFancamBestsWeekly(Page page) {
        return populateFancamBestsWeekly(Calendar.getInstance(), page, 0);
    }

    private Page populateFancamBestsWeekly(Calendar calendar, Page page, int recurseCount) {
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        calendar.add(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek() - calendar.get(Calendar.DAY_OF_WEEK));
        page.param("tableName", "FancamVideoRankWeekly").param("dateIdx", Integer.parseInt(fdf.format(calendar)));
        page = populateFancamBests(page);

        if(page.getContents().size() > 0) {
            return page;
        } else if(recurseCount < 3) {
            return populateFancamBestsWeekly(calendar, page, ++recurseCount);
        } else {
            return populateFancamBestsDaily(page);
        }
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V1_KEY_FANCAM_RANK, key = "#root.methodName + '_' + #page.cacheKey")
    public Page populateFancamBestsMonthly(Page page) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        page.param("tableName", "FancamVideoRankMonthly").param("dateIdx", Integer.parseInt(fdf2.format(calendar)));
        return populateFancamBests(page);
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V1_KEY_FANCAM_RANK, key = "#root.methodName + '_' + #page.cacheKey")
    public Page populateFancamBestsLast7Days(Page page) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -8);
        page.param("dateIdx", Integer.parseInt(fdf.format(calendar)));
        return populateFancamLasts(page);
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V1_KEY_FANCAM_RANK, key = "#root.methodName + '_' + #page.cacheKey")
    public Page populateFancamBestsLast30Days(Page page) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -31);
        page.param("dateIdx", Integer.parseInt(fdf.format(calendar)));
        return populateFancamLasts(page);
    }

    private Page populateFancamLasts(Page page) {
      page.setTotal(v1YouTubeRepository.countFancamLastsByPage(page));
      page.setContents(v1YouTubeRepository.findFancamLastsByPage(page));
      return page;
    }



    @Override
    @Cacheable(value = CacheKeyHolder.V1_KEY_TVSHOWS, key = "#root.methodName + '_' + #page.cacheKey")
    public Page populateShowVideos(Page page) {
        page.setTotal(v1YouTubeRepository.countShowVideosByPage(page));
        page.setContents(v1YouTubeRepository.findShowVideosByPage(page));
        return page;
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V1_KEY_TVSHOWS, key = "#root.methodName + '_' + #videoUid")
    public V1Video getShowVideoByUid(Long videoUid) {
        return v1YouTubeRepository.findShowVideoByUid(videoUid);
    }




    @Override
    @Cacheable(value = CacheKeyHolder.V1_KEY_LYRICS, key = "#root.methodName + '_' + #page.cacheKey")
    public Page populateLyricsVideos(Page page) {
        page.setTotal(v1YouTubeRepository.countLyricsVideosByPage(page));
        page.setContents(v1YouTubeRepository.findLyricsVideosByPage(page));
        return page;
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V1_KEY_LYRICS, key = "#root.methodName + '_' + #videoUid")
    public V1Video getLyricsVideoByUid(Long videoUid) {
        return v1YouTubeRepository.findLyricsVideoByUid(videoUid);
    }






    @Override
    @Cacheable(value = CacheKeyHolder.V1_KEY_KARAOKE, key = "#root.methodName + '_' + #idolUid + '_' + #page.cacheKey")
    public Page populateKaraokeVideosByIdol(Long idolUid, Page page) {
        page.getP().put("idolUid", idolUid);
        page.setTotal(v1YouTubeRepository.countIdolKaraokeVideosByPage(page));
        page.setContents(v1YouTubeRepository.findIdolKaraokeVideosByPage(page));
        return page;
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V1_KEY_KARAOKE, key = "#root.methodName + '_' + #page.cacheKey")
    public Page populateKaraokeVideos(Page page) {
        page.setTotal(v1YouTubeRepository.countKaraokeVideosByPage(page));
        page.setContents(v1YouTubeRepository.findKaraokeVideosByPage(page));
        return page;
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V1_KEY_KARAOKE, key = "#root.methodName + '_' + #videoUid")
    public V1Video getKaraokeVideoByUid(Long videoUid) {
        return v1YouTubeRepository.findKaraokeVideoByUid(videoUid);
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V1_KEY_KARAOKE_RANK, key = "#root.methodName + '_' + #page.cacheKey")
    public Page populateKaraokeBestsDaily(Page page) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        page.param("tableName", "KaraokeVideoRankDaily").param("dateIdx", Integer.parseInt(fdf.format(calendar)));
        return populateKaraokeBests(page);
    }

    private Page populateKaraokeBests(Page page) {
      page.setTotal(v1YouTubeRepository.countKaraokeBestsByPage(page));
      page.setContents(v1YouTubeRepository.findKaraokeBestsByPage(page));
      return page;
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V1_KEY_KARAOKE_RANK, key = "#root.methodName + '_' + #page.cacheKey")
    public Page populateKaraokeBestsWeekly(Page page) {
        return populateKaraokeBestsWeekly(Calendar.getInstance(), page, 0);
    }

    private Page populateKaraokeBestsWeekly(Calendar calendar, Page page, int recurseCount) {
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        calendar.add(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek() - calendar.get(Calendar.DAY_OF_WEEK));
        page.param("tableName", "KaraokeVideoRankWeekly").param("dateIdx", Integer.parseInt(fdf.format(calendar)));
        page = populateKaraokeBests(page);

        if(page.getContents().size() > 0) {
            return page;
        } else if(recurseCount < 3) {
            return populateKaraokeBestsWeekly(calendar, page, ++recurseCount);
        } else {
            return populateKaraokeBestsDaily(page);
        }
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V1_KEY_KARAOKE_RANK, key = "#root.methodName + '_' + #page.cacheKey")
    public Page populateKaraokeBestsMonthly(Page page) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        page.param("tableName", "KaraokeVideoRankMonthly").param("dateIdx", Integer.parseInt(fdf2.format(calendar)));
        return populateKaraokeBests(page);
    }



    @Override
    @Cacheable(value = CacheKeyHolder.V1_KEY_SEARCH, key = "#root.methodName + '_' + #page.cacheKey")
    public Page search(Page page) {
        page.setTotal(v1YouTubeRepository.countLikeTitle(page));
        page.setContents(v1YouTubeRepository.findLikeTitle(page));
        return page;
    }













    @Override
    @Cacheable(value = CacheKeyHolder.V1_KEY_IDOLS, key = "#root.methodName + '_' + #lang")
    public List<Idol> findAll(String lang) {
        return idolRepository.findByLang(lang);
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V1_KEY_IDOLS, key = "#root.methodName + '_' + #idolUid + '_' + #lang")
    public Object findIdol(Long idolUid, String lang) {
        return idolRepository.findByUid(idolUid, lang);
    }

    @Override
    @Cacheable(value = CacheKeyHolder.V1_KEY_IMAGES, key = "#root.methodName + '_' + #idolUid + '_' + #page.cacheKey")
    public Page populateImages(Long idolUid, Page page) {
        page.getP().put("idolUid", idolUid);
        page.setTotal(idolImageRepository.countIdolImagesByPage(page));
        page.setContents(idolImageRepository.findIdolImagesByPage(page));
        return page;
    }
}
