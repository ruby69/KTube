package com.appskimo.app.ktube.service;

import com.appskimo.app.ktube.Constants;
import com.appskimo.app.ktube.On;
import com.appskimo.app.ktube.domain.Loadable;
import com.appskimo.app.ktube.domain.Page;
import com.appskimo.app.ktube.domain.Video;
import com.appskimo.app.ktube.domain.YoutubeVideo;
import com.appskimo.app.ktube.ui.activity.PlayerRestActivity;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;

@EBean(scope = EBean.Scope.Singleton)
public class PlayerRestBean extends PlayerBean implements Loadable {
    @Bean RestClient restClient;
    private List<YoutubeVideo> videos = new ArrayList<>();

    private Page<Video> lastPage = null;
    private On<Page<Video>> pageOn;
    @Setter private PlayerRestActivity playerRestActivity;
    @Setter private String searchQuery;

    @Override
    public void clear() {
        if (playerRestActivity == null || playerRestActivity.isDestroyed() || playerRestActivity.isFinishing()) {
            videos.clear();
            lastPage = null;
            pageOn = null;
        }
    }

    public void setPageOn(On<Page<Video>> paramPageOn) {
        lastPage = null;
        pageOn = new On<Page<Video>>().addSuccessListener(response -> {
            List<Video> items = response.getContents();
            if (response.getPage() == 1) {
                resetItems(items);
            } else {
                addItems(items);
            }

            if (paramPageOn != null) {
                paramPageOn.success(response);
            }
            lastPage = response;
        }).addCompleteListener(response -> lockLoad = false);
    }

    public void releasePageOn() {
        pageOn = new On<Page<Video>>().addSuccessListener(response -> {
            List<Video> items = response.getContents();
            if (response.getPage() == 1) {
                resetItems(items);
            } else {
                addItems(items);
            }

            lastPage = response;
        }).addCompleteListener(response -> lockLoad = false);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean lockLoad;

    @Override
    public void load() {
        if (!lockLoad) {
            if (restClient != null) {
                lockLoad = true;

                Constants.VideoGroup videoGroup = videoDomain.getVideoGroup();
                if (lastPage == null) {
                    if (videoGroup != null && videoGroup.isSearch()) {
                        restClient.fetchSearch(videoDomain, 1, searchQuery, pageOn);
                    } else {
                        restClient.fetchYoutubeVideos(videoDomain, 1, pageOn);
                    }
                    return;
                }

                if (lastPage.getPage() < lastPage.getTotalPages()) {
                    if (videoGroup != null && videoGroup.isSearch()) {
                        restClient.fetchSearch(videoDomain, lastPage.getPage() + 1, searchQuery, pageOn);
                    } else {
                        restClient.fetchYoutubeVideos(videoDomain, lastPage.getPage() + 1, pageOn);
                    }

                }
            }
        }
    }

    private void clearItems() {
        videos.clear();
    }

    private void addItems(List<? extends Video> items) {
        if (items != null) {
            videos.addAll(items);
        }
    }

    private void resetItems(List<? extends Video> items) {
        clearItems();
        addItems(items);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    @Background
    public void loadVideo(YoutubeVideo video, On<YoutubeVideo> on) {
        currentVideo = video;
        on.success(currentVideo);
    }

    @Override
    @Background
    public void loadVideo(On<YoutubeVideo> on) {
        on.success(currentVideo);
    }

    @Override
    @Background
    public void loadNextVideo(boolean force, On<YoutubeVideo> on) {
        if (countOf() > 1) {
            if (force) {
                currentVideo = currentMode.isRandom() ? findRandomVideo(currentVideo) : findNextVideo(currentVideo);
                on.success(currentVideo);
                return;
            }

            if (currentMode.isStandard()) {
                on.success(null);
                return;
            }

            if (currentMode.isRandom()) {
                currentVideo = findRandomVideo(currentVideo);
            } else if (currentMode.isRepeatAll()) {
                currentVideo = findNextVideo(currentVideo);
            }
            on.success(currentVideo);

        } else {
            if (currentMode.isStandard()) {
                on.success(null);
            } else {
                on.success(currentVideo);
            }
        }
    }

    private YoutubeVideo findNextVideo(YoutubeVideo video) {
        int index = indexOf(video);
        int tryCount = 0;

        YoutubeVideo gettingVideo = null;
        do {
            tryCount++;
            index++;
            if (index >= videos.size()) index = 0;
            gettingVideo = videos.get(index);

            if (index > countOf() - 5) {
               load();
            }
        } while(tryCount < 5 && video != null && gettingVideo != null && video.getVideoId().equals(gettingVideo.getVideoId()));

        return gettingVideo;
    }

    private YoutubeVideo findPrevVideo(YoutubeVideo video) {
        int index = indexOf(video);
        int tryCount = 0;

        YoutubeVideo gettingVideo = null;
        do {
            tryCount++;
            index--;
            if (index < 0) index = videos.size() - 1;
            gettingVideo = videos.get(index);
        } while(tryCount < 5 && video != null && gettingVideo != null && video.getVideoId().equals(gettingVideo.getVideoId()));

        return gettingVideo;
    }

    private int indexOf(YoutubeVideo video) {
        if (video == null) {
            return -1;
        }

        int index = videos.indexOf(video);
        if (index < 0) {
            for (int i = 0; i < videos.size(); i++) {
                if (video.getVideoId().equals(videos.get(i).getVideoId())) {
                    return i;
                }
            }
            return -1;
        } else {
            return index;
        }
    }

    private YoutubeVideo findRandomVideo(YoutubeVideo video) {
        YoutubeVideo randomVideo = null;
        do {
            randomVideo = videos.get(RandomUtils.nextInt(0, videos.size()));
        } while(video != null && randomVideo != null && video.getVideoId().equals(randomVideo.getVideoId()));
        return randomVideo;
    }

    private int countOf() {
        return videos == null ? 0 : videos.size();
    }

    @Override
    @Background
    public void loadPrevVideo(boolean force, On<YoutubeVideo> on) {
        if (countOf() > 1L) {
            if (force) {
                currentVideo = currentMode.isRandom() ? findRandomVideo(currentVideo) : findPrevVideo(currentVideo);
                on.success(currentVideo);
                return;
            }

            if (currentMode.isRandom()) {
                currentVideo = findRandomVideo(currentVideo);
            } else if (currentMode.isRepeatAll()) {
                currentVideo = findPrevVideo(currentVideo);
            }
            on.success(currentVideo);

        } else {
            if (currentMode.isStandard()) {
                on.success(null);
            } else {
                on.success(currentVideo);
            }
        }
    }


}
