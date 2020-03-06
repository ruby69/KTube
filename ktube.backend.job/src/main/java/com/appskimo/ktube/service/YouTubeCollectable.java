package com.appskimo.ktube.service;

import com.appskimo.ktube.domain.model.YouTube;

public interface YouTubeCollectable {

    void collectVideoIds();

    void collectVideoInfos();

    void collectNewVideos();

    void updateViewCount();



    void collectFancamVideoIds();

    void collectFancamVideoInfos();

    void collectNewFancamVideos();

    void updateFancamViewCount();

    String getUploadsPlayListId(YouTube.FancamChannel fancamChannel);



    void collectShowVideoIds();

    void collectShowVideoInfos();

    void collectNewShowVideos();



    void updateLyricsUploadsPlaylistId();

    void collectLyricsVideoIds();

    void collectLyricsVideoInfos();

    void collectNewLyricsVideos();



    void cleanByTitleIsNull();



    void collectKaraokeVideoIds();

    void collectKaraokeVideoInfos();

    void collectNewKaraokeVideos();

    void updateKaraokeViewCount();


}
