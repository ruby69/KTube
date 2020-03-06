package com.appskimo.ktube.web.controller;

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

import com.appskimo.ktube.domain.model.KeyEncryptor;
import com.appskimo.ktube.domain.model.Page;
import com.appskimo.ktube.service.V2Service;

@RestController
@RequestMapping(value = "api/v2", produces = MediaType.APPLICATION_JSON_VALUE)
public class V2Controller {
    @Autowired private V2Service v2Service;

    private ResponseEntity<Object> responseCache(Object body, long seconds) {
        CacheControl cacheControl = CacheControl.maxAge(seconds, TimeUnit.SECONDS).cachePublic();
        return ResponseEntity.ok().cacheControl(cacheControl).body(body);
    }

    @Deprecated
    @RequestMapping(value = "meta", method = RequestMethod.GET)
    public Object meta() {
        return v2Service.getMeta();
    }

    @RequestMapping(value = "overview", method = RequestMethod.GET)
    public ResponseEntity<Object> overview(@RequestParam(value = "lang", defaultValue = "en", required = false) String lang) {
        return responseCache(v2Service.getOverview(lang), 3600L);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping(value = "videos", method = RequestMethod.GET)
    public ResponseEntity<Object> videos(Page page) {
        return responseCache(v2Service.populateVideos(page).clear(), 3600L);
    }

    @RequestMapping(value = "videos/{idolKey}", method = RequestMethod.GET)
    public Object videosByIdol(@PathVariable String idolKey, Page page) {
        page.param("idolUid", KeyEncryptor.getUid(KeyEncryptor.Target.IDOL, idolKey));
        return responseCache(v2Service.populateVideosByIdol(page).clear(), 3600L);
    }

    @RequestMapping(value = "videos/best/{day}", method = RequestMethod.GET)
    public Object bestVideos(@PathVariable int day, Page page) {
        page.param("day", day < 1 ? 1 : day);
        return responseCache(v2Service.populateLastNDaysBestVideos(page).clear(), 3600L);
    }

    @RequestMapping(value = "videos/million/{count}", method = RequestMethod.GET)
    public Object millionVideos(@PathVariable int count, Page page) {
        page.param("vc", count);
        return responseCache(v2Service.populateMillions(page).clear(), 3600L);
    }

    @RequestMapping(value = "fancams", method = RequestMethod.GET)
    public Object fancams(Page page) {
        return responseCache(v2Service.populateFancams(page).clear(), 3600L);
    }

    @RequestMapping(value = "fancams/best/{day}", method = RequestMethod.GET)
    public Object bestFamcams(@PathVariable int day, Page page) {
        page.param("day", day < 1 ? 1 : day);
        return responseCache(v2Service.populateLastNDaysBestFancams(page).clear(), 3600L);
    }



    @RequestMapping(value = "karaokes", method = RequestMethod.GET)
    public Object karaokes(Page page) {
        return responseCache(v2Service.populateKaraokes(page).clear(), 3600L);
    }

    @RequestMapping(value = "karaokes/{idolKey}", method = RequestMethod.GET)
    public Object karaokesByIdol(@PathVariable String idolKey, Page page) {
        page.param("idolUid", KeyEncryptor.getUid(KeyEncryptor.Target.IDOL, idolKey));
        return responseCache(v2Service.populateKaraokesByIdol(page).clear(), 3600L);
    }

    @RequestMapping(value = "karaokes/best/{day}", method = RequestMethod.GET)
    public Object bestKaraokes(@PathVariable int day, Page page) {
        page.param("day", day < 1 ? 1 : day);
        return responseCache(v2Service.populateLastNDaysBestKaraokes(page).clear(), 3600L);
    }



    @RequestMapping(value = "lyrics", method = RequestMethod.GET)
    public Object lyrics(Page page) {
        return responseCache(v2Service.populateLyricsVideos(page).clear(), 3600L);
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

        return responseCache(v2Service.search(page).clear(), 180L);
    }
}
