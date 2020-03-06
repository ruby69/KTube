package com.appskimo.ktube.web.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.appskimo.ktube.domain.model.Idol;
import com.appskimo.ktube.domain.model.IdolInfo;
import com.appskimo.ktube.domain.model.KeyEncryptor;
import com.appskimo.ktube.domain.model.Page;
import com.appskimo.ktube.domain.model.YouTube;
import com.appskimo.ktube.service.V1Service;

@RestController
@RequestMapping(value = "api", produces = MediaType.APPLICATION_JSON_VALUE)
public class V1Controller {
    @Autowired private V1Service v1Service;

    private ResponseEntity<Object> responseCache(Object body, long seconds) {
        CacheControl cacheControl = CacheControl.maxAge(seconds, TimeUnit.SECONDS).cachePublic();
        return ResponseEntity.ok().cacheControl(cacheControl).body(body);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping(value = "promotions/{category}", method = RequestMethod.GET)
    public Object getPromotions(@PathVariable String category, @RequestParam(value = "lang", defaultValue = "en", required = false) String lang) {
        category = category.toLowerCase();
        if("idol".equals(category)) {
            return responseCache(getPromotionIdols(lang), 180L);
        }else if("fancam".equals(category)) {
            return responseCache(getPromotionFancamVideos(), 10800L);
        } else {
            return responseCache(getPromotionVideos(), 10800L);
        }
    }

    private List<Idol> getPromotionIdols(String lang) {
        List<Idol> idols = v1Service.findAll(IdolInfo.Lang.resolveCode(lang));
        Collections.shuffle(idols);
        return idols.subList(0, 5);
    }

    @SuppressWarnings("unchecked")
    private List<YouTube.V1Video> getPromotionVideos() {
        List<YouTube.V1Video> newVideos = (List<YouTube.V1Video>) v1Service.populateVideos(new Page().param("order", "time")).getContents();
        List<YouTube.V1Video> dailyBest = (List<YouTube.V1Video>) v1Service.populateBestsDaily(new Page()).getContents();
        List<YouTube.V1Video> weeklyBest = (List<YouTube.V1Video>) v1Service.populateBestsWeekly(new Page()).getContents();
        List<YouTube.V1Video> monthlyBest = (List<YouTube.V1Video>) v1Service.populateBestsMonthly(new Page()).getContents();
        List<YouTube.V1Video> wholeBest = (List<YouTube.V1Video>) v1Service.populateVideos(new Page().param("order", "view")).getContents();

        List<YouTube.V1Video> list = new ArrayList<>();
        if(newVideos.size() > 0) {
            list.add(newVideos.get(0));
        }
        if(dailyBest.size() > 0) {
            list.add(dailyBest.get(0));
        }
        if(weeklyBest.size() > 0) {
            list.add(weeklyBest.get(0));
        }
        if(monthlyBest.size() > 0) {
            list.add(monthlyBest.get(0));
        }
        if(wholeBest.size() > 0) {
            list.add(wholeBest.get(0));
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private List<YouTube.V1Video> getPromotionFancamVideos() {
        List<YouTube.V1Video> newVideos = (List<YouTube.V1Video>) v1Service.populateFancamVideos(new Page().param("order", "time")).getContents();
        List<YouTube.V1Video> dailyBest = (List<YouTube.V1Video>) v1Service.populateFancamBestsDaily(new Page()).getContents();
        List<YouTube.V1Video> weeklyBest = (List<YouTube.V1Video>) v1Service.populateFancamBestsWeekly(new Page()).getContents();
        List<YouTube.V1Video> monthlyBest = (List<YouTube.V1Video>) v1Service.populateFancamBestsMonthly(new Page()).getContents();
        List<YouTube.V1Video> wholeBest = (List<YouTube.V1Video>) v1Service.populateFancamVideos(new Page().param("order", "view")).getContents();

        List<YouTube.V1Video> list = new ArrayList<>();
        if(newVideos.size() > 0) {
            list.add(newVideos.get(0));
        }
        if(dailyBest.size() > 0) {
            list.add(dailyBest.get(0));
        }
        if(weeklyBest.size() > 0) {
            list.add(weeklyBest.get(0));
        }
        if(monthlyBest.size() > 0) {
            list.add(monthlyBest.get(0));
        }
        if(wholeBest.size() > 0) {
            list.add(wholeBest.get(0));
        }
        return list;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @RequestMapping(value = "idols", method = RequestMethod.GET)
    public Object idols(@RequestParam(value = "lang", defaultValue = "en", required = false) String lang) {
        return responseCache(v1Service.findAll(IdolInfo.Lang.resolveCode(lang)), 86400L);
    }

    @RequestMapping(value = "idols/{idolKey}", method = RequestMethod.GET)
    public Object idols(@PathVariable String idolKey, @RequestParam(value = "lang", defaultValue = "en", required = false) String lang) {
        Long idolUid = KeyEncryptor.getUid(KeyEncryptor.Target.IDOL, idolKey);
        return responseCache(v1Service.findIdol(idolUid, IdolInfo.Lang.resolveCode(lang)), 86400L);
    }

    @RequestMapping(value = "images/{idolKey}", method = RequestMethod.GET)
    public Object getImages(@PathVariable String idolKey, Page page) {
        Long idolUid = KeyEncryptor.getUid(KeyEncryptor.Target.IDOL, idolKey);
        return responseCache(v1Service.populateImages(idolUid, page), 86400L);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping(value = "videos/{idolKey}", method = RequestMethod.GET)
    public Object getVideosByIdol(@PathVariable String idolKey, Page page) {
        Long idolUid = KeyEncryptor.getUid(KeyEncryptor.Target.IDOL, idolKey);
        return responseCache(v1Service.populateVideosByIdol(idolUid, page), 10800L);
    }

    @RequestMapping(value = "videos", method = RequestMethod.GET)
    public ResponseEntity<Object> getVideos(Page page) {
        return responseCache(v1Service.populateVideos(page), 10800L);
    }

    @RequestMapping(value = "video/{videoKey}", method = RequestMethod.GET)
    public Object getVideoByUid(@PathVariable String videoKey) {
        Long videoUid = KeyEncryptor.getUid(KeyEncryptor.Target.VIDEO, videoKey);
        return responseCache(v1Service.getVideoByUid(videoUid), 10800L);
    }

    @RequestMapping(value = "bests/{category}", method = RequestMethod.GET)
    public Object getBestsByCategory(@PathVariable String category, Page page) {
        category = category.toLowerCase();

        if ("h".equals(category)) {
            page = v1Service.populateGratestHits(page).clear();

        } else if ("w".equals(category)) {
            page = v1Service.populateBestsWeekly(page).clear();

        } else if ("m".equals(category)) {
            page = v1Service.populateBestsMonthly(page).clear();

//        } else if ("7d".equals(category)) {
//            videoService.populateBestsLast7Days(page).clear();
//
//        } else if ("30d".equals(category)) {
//            videoService.populateBestsLast30Days(page).clear();

        } else {
            page = v1Service.populateBestsDaily(page).clear();
        }
        return responseCache(page, 86400L);
    }

    @RequestMapping(value = "fancams", method = RequestMethod.GET)
    public Object getFancamVideos(Page page) {
        return responseCache(v1Service.populateFancamVideos(page), 10800L);
    }

    @RequestMapping(value = "fancams/{videoKey}", method = RequestMethod.GET)
    public Object getFancamVideoByUid(@PathVariable String videoKey) {
        Long videoUid = KeyEncryptor.getUid(KeyEncryptor.Target.VIDEO, videoKey);
        return responseCache(v1Service.getFancamVideoByUid(videoUid), 10800L);
    }

    @RequestMapping(value = "fbests/{category}", method = RequestMethod.GET)
    public Object getFancamBestsByCategory(@PathVariable String category, Page page) {
        category = category.toLowerCase();
        if ("w".equals(category)) {
            page = v1Service.populateFancamBestsWeekly(page).clear();

        } else if ("m".equals(category)) {
            page = v1Service.populateFancamBestsMonthly(page).clear();

//        } else if ("7d".equals(category)) {
//            videoService.populateFancamBestsLast7Days(page).clear();
//
//        } else if ("30d".equals(category)) {
//            videoService.populateFancamBestsLast30Days(page).clear();

        } else {
            page = v1Service.populateFancamBestsDaily(page).clear();
        }
        return responseCache(page, 86400L);
    }

    @RequestMapping(value = "shows", method = RequestMethod.GET)
    public Object getShowVideos(Page page) {
        return responseCache(v1Service.populateShowVideos(page), 345600L);
    }

    @RequestMapping(value = "shows/{videoKey}", method = RequestMethod.GET)
    public Object getShowVideoByUid(@PathVariable String videoKey) {
        Long videoUid = KeyEncryptor.getUid(KeyEncryptor.Target.VIDEO, videoKey);
        return responseCache(v1Service.getShowVideoByUid(videoUid), 345600L);
    }

    @RequestMapping(value = "lyrics", method = RequestMethod.GET)
    public Object getLyricsVideos(Page page) {
        return responseCache(v1Service.populateLyricsVideos(page), 10800L);
    }

    @RequestMapping(value = "lyrics/{videoKey}", method = RequestMethod.GET)
    public Object getLyricsVideoByUid(@PathVariable String videoKey) {
        Long videoUid = KeyEncryptor.getUid(KeyEncryptor.Target.VIDEO, videoKey);
        return responseCache(v1Service.getLyricsVideoByUid(videoUid), 10800L);
    }

    @RequestMapping(value = "karaokes/{idolKey}", method = RequestMethod.GET)
    public Object getKaraokeVideosByIdol(@PathVariable String idolKey, Page page) {
        Long idolUid = KeyEncryptor.getUid(KeyEncryptor.Target.IDOL, idolKey);
        return responseCache(v1Service.populateKaraokeVideosByIdol(idolUid, page), 10800L);
    }

    @RequestMapping(value = "karaokes", method = RequestMethod.GET)
    public Object getKaraokeVideos(Page page) {
        return responseCache(v1Service.populateKaraokeVideos(page), 10800L);
    }

    @RequestMapping(value = "karaoke/{videoKey}", method = RequestMethod.GET)
    public Object getKaraokeVideoByUid(@PathVariable String videoKey) {
        Long videoUid = KeyEncryptor.getUid(KeyEncryptor.Target.VIDEO, videoKey);
        return responseCache(v1Service.getKaraokeVideoByUid(videoUid), 10800L);
    }

    @RequestMapping(value = "karabests/{category}", method = RequestMethod.GET)
    public Object getKaraokeBestsByCategory(@PathVariable String category, Page page) {
        category = category.toLowerCase();
        if ("w".equals(category)) {
            page = v1Service.populateKaraokeBestsWeekly(page).clear();

        } else if ("m".equals(category)) {
            page = v1Service.populateKaraokeBestsMonthly(page).clear();

        } else {
            page = v1Service.populateKaraokeBestsDaily(page).clear();
        }
        return responseCache(page, 86400L);
    }

    @RequestMapping(value = "search/{category}", method = RequestMethod.GET)
    public Object search(@PathVariable String category, Page page) {
        category = category.toLowerCase();

        if (!"video".equals(category) && !"lyric".equals(category) && !"fancam".equals(category) && !"karaoke".equals(category)) {
            return page.clear();
        }

        if ("video".equals(category)) {
            page.param("tableName", "Video");

        } else if ("lyric".equals(category)) {
            page.param("tableName", "LyricsVideo");

        } else if ("fancam".equals(category)) {
            page.param("tableName", "FancamVideo");

        } else if ("karaoke".equals(category)) {
            page.param("tableName", "KaraokeVideo");
        }

        return responseCache(v1Service.search(page).clear(), 180L);
    }

}
