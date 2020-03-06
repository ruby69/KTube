package com.appskimo.ktube.service;

import java.util.List;

import com.appskimo.ktube.domain.model.Idol;
import com.appskimo.ktube.domain.model.Overview;
import com.appskimo.ktube.domain.model.Page;

public interface V2Service {

    Page populateVideos(Page page);

    Page populateMillions(Page page);

    Page populateVideosByIdol(Page page);

    Page populateLastNDaysBestVideos(Page page);

    Page populateFancams(Page page);

    Page populateLastNDaysBestFancams(Page page);

    Page populateKaraokes(Page page);

    Page populateKaraokesByIdol(Page page);

    Page populateLastNDaysBestKaraokes(Page page);

    Page populateLyricsVideos(Page page);

    Page search(Page page);

    List<Idol> getBestIdols(String lang);

    Overview getOverview(String lang);

    @Deprecated
    Object getMeta();

}
