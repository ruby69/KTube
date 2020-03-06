package com.appskimo.ktube.service;

import java.util.List;

import com.appskimo.ktube.domain.model.Idol;
import com.appskimo.ktube.domain.model.Page;
import com.appskimo.ktube.domain.model.YouTube;

public interface V1Service {

    Page populateVideosByIdol(Long idolUid, Page page);

    Page populateVideos(Page page);

    YouTube.V1Video getVideoByUid(Long videoUid);

    Page populateBestsDaily(Page page);

    Page populateBestsWeekly(Page page);

    Page populateBestsMonthly(Page page);

    Page populateBestsLast7Days(Page page);

    Page populateBestsLast30Days(Page page);

    Page populateGratestHits(Page page);



    Page populateFancamBestsDaily(Page page);

    Page populateFancamBestsWeekly(Page page);

    Page populateFancamBestsMonthly(Page page);

    Page populateFancamBestsLast7Days(Page page);

    Page populateFancamBestsLast30Days(Page page);

    Page populateFancamVideos(Page page);

    YouTube.V1Video getFancamVideoByUid(Long videoUid);



    Page populateShowVideos(Page page);

    YouTube.V1Video getShowVideoByUid(Long videoUid);



    Page populateLyricsVideos(Page page);

    YouTube.V1Video getLyricsVideoByUid(Long videoUid);



    Page populateKaraokeVideosByIdol(Long idolUid, Page page);

    Page populateKaraokeVideos(Page page);

    YouTube.V1Video getKaraokeVideoByUid(Long videoUid);

    Page populateKaraokeBestsDaily(Page page);

    Page populateKaraokeBestsWeekly(Page page);

    Page populateKaraokeBestsMonthly(Page page);



    Page search(Page page);



    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IDOL

    List<Idol> findAll(String lang);

    Object findIdol(Long idolUid, String lang);

    Page populateImages(Long idolUid, Page page);
}
