package com.appskimo.app.ktube.service;

import android.content.Context;
import android.util.Log;

import com.appskimo.app.ktube.BuildConfig;
import com.appskimo.app.ktube.On;
import com.appskimo.app.ktube.domain.VideoFavorite;
import com.appskimo.app.ktube.domain.VideoListed;
import com.appskimo.app.ktube.domain.YoutubeVideo;
import com.appskimo.app.ktube.support.Query;
import com.appskimo.app.ktube.support.SQLiteOpenHelper;
import com.j256.ormlite.stmt.QueryBuilder;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.ormlite.annotations.OrmLiteDao;

@EBean(scope = EBean.Scope.Singleton)
public class PlayerMyBean extends PlayerBean {
    @RootContext Context context;
    @OrmLiteDao(helper = SQLiteOpenHelper.class) SQLiteOpenHelper.ListedVideoDao listedVideoDao;
    @OrmLiteDao(helper = SQLiteOpenHelper.class) SQLiteOpenHelper.FavoriteVideoDao favoriteVideoDao;

    @Override
    public void clear() {
        // do nothing
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
        if (videoDomain.isFavorite()) {
            loadNextFavoriteVideo(force, on);

        } else if (videoDomain.isPlaylist()) {
            loadNextListedVideo(force, on);
        }
    }

    private void loadNextFavoriteVideo(boolean force, On<YoutubeVideo> on) {
        if (countOfFavoriteVideo() > 1L) {
            if (force) {
                currentVideo = currentMode.isRandom() ? findRandomFavoriteVideo(currentVideo) : findNextFavoriteVideo(currentVideo);
                on.success(currentVideo);
                return;
            }

            if (currentMode.isStandard()) {
                on.success(null);
                return;
            }

            if (currentMode.isRandom()) {
                currentVideo = findRandomFavoriteVideo(currentVideo);
            } else if (currentMode.isRepeatAll()) {
                currentVideo = findNextFavoriteVideo(currentVideo);
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

    private void loadNextListedVideo(boolean force, On<YoutubeVideo> on) {
        if (countOfListedVideo() > 1L) {
            if (force) {
                currentVideo = currentMode.isRandom() ? findRandomListedVideo(currentVideo) : findNextListedVideo(currentVideo);
                on.success(currentVideo);
                return;
            }

            if (currentMode.isStandard()) {
                on.success(null);
                return;
            }

            if (currentMode.isRandom()) {
                currentVideo = findRandomListedVideo(currentVideo);
            } else if (currentMode.isRepeatAll()) {
                currentVideo = findNextListedVideo(currentVideo);
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

    @Override
    @Background
    public void loadPrevVideo(boolean force, On<YoutubeVideo> on) {
        if (videoDomain.isFavorite()) {
            loadPrevFavoriteVideo(force, on);

        } else if (videoDomain.isPlaylist()) {
            loadPrevListedVideo(force, on);
        }
    }

    private void loadPrevFavoriteVideo(boolean force, On<YoutubeVideo> on) {
        if (countOfFavoriteVideo() > 1L) {
            if (force) {
                currentVideo = currentMode.isRandom() ? findRandomFavoriteVideo(currentVideo) : findPrevFavoriteVideo(currentVideo);
                on.success(currentVideo);
                return;
            }

            if (currentMode.isRandom()) {
                currentVideo = findRandomFavoriteVideo(currentVideo);
            } else if (currentMode.isRepeatAll()) {
                currentVideo = findPrevFavoriteVideo(currentVideo);
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

    private void loadPrevListedVideo(boolean force, On<YoutubeVideo> on) {
        if (countOfListedVideo() > 1L) {
            if (force) {
                currentVideo = currentMode.isRandom() ? findRandomListedVideo(currentVideo) : findPrevListedVideo(currentVideo);
                on.success(currentVideo);
                return;
            }

            if (currentMode.isRandom()) {
                currentVideo = findRandomListedVideo(currentVideo);
            } else if (currentMode.isRepeatAll()) {
                currentVideo = findPrevListedVideo(currentVideo);
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

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private YoutubeVideo findFirstFavoriteVideo() {
        return executeSync(() -> favoriteVideoDao.queryBuilder().orderBy(VideoFavorite.FIELD_seq, false).queryForFirst());
    }

    private YoutubeVideo findLastFavoriteVideo() {
        return executeSync(() -> favoriteVideoDao.queryBuilder().orderBy(VideoFavorite.FIELD_seq, true).queryForFirst());
    }

    private YoutubeVideo findNextFavoriteVideo(YoutubeVideo video) {
        return executeSync(() -> {
            QueryBuilder<VideoFavorite, Long> qb = favoriteVideoDao.queryBuilder().orderBy(VideoFavorite.FIELD_seq, false);
            qb.where().lt(VideoFavorite.FIELD_seq, video.getSeq());
            YoutubeVideo result = qb.queryForFirst();
            return result != null ? result : findFirstFavoriteVideo();
        });
    }

    private YoutubeVideo findPrevFavoriteVideo(YoutubeVideo video) {
        return executeSync(() -> {
            QueryBuilder<VideoFavorite, Long> qb = favoriteVideoDao.queryBuilder().orderBy(VideoFavorite.FIELD_seq, true);
            qb.where().gt(VideoFavorite.FIELD_seq, video.getSeq());
            YoutubeVideo result = qb.queryForFirst();
            return result != null ? result : findLastFavoriteVideo();
        });
    }

    private YoutubeVideo findRandomFavoriteVideo(YoutubeVideo video) {
        return executeSync(() -> {
            QueryBuilder<VideoFavorite, Long> qb = favoriteVideoDao.queryBuilder().orderByRaw("RANDOM()");
            YoutubeVideo randomVideo = null;
            do {
                randomVideo = qb.queryForFirst();
            } while(video != null && randomVideo != null && video.getVideoId().equals(randomVideo.getVideoId()));

            return randomVideo;
        });
    }

    private long countOfFavoriteVideo() {
        Long result = executeSync(() -> favoriteVideoDao.countOf());
        return result == null ? 0L : result;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private YoutubeVideo findFirstListedVideo() {
        return executeSync(() -> listedVideoDao.queryBuilder().orderBy(VideoListed.FIELD_seq, false).queryForFirst());
    }

    private YoutubeVideo findLastListedVideo() {
        return executeSync(() -> listedVideoDao.queryBuilder().orderBy(VideoListed.FIELD_seq, true).queryForFirst());
    }

    private YoutubeVideo findNextListedVideo(YoutubeVideo video) {
        return executeSync(() -> {
            QueryBuilder<VideoListed, Long> qb = listedVideoDao.queryBuilder().orderBy(VideoListed.FIELD_seq, false);
            qb.where().lt(VideoListed.FIELD_seq, video.getSeq());
            YoutubeVideo result = qb.queryForFirst();
            return result != null ? result : findFirstListedVideo();
        });
    }

    private YoutubeVideo findPrevListedVideo(YoutubeVideo video) {
        return executeSync(() -> {
            QueryBuilder<VideoListed, Long> qb = listedVideoDao.queryBuilder().orderBy(VideoListed.FIELD_seq, true);
            qb.where().gt(VideoListed.FIELD_seq, video.getSeq());
            YoutubeVideo result = qb.queryForFirst();
            return result != null ? result : findLastListedVideo();
        });
    }

    private YoutubeVideo findRandomListedVideo(YoutubeVideo video) {
        return executeSync(() -> {
            QueryBuilder<VideoListed, Long> qb = listedVideoDao.queryBuilder().orderByRaw("RANDOM()");
            YoutubeVideo randomVideo = null;
            do {
                randomVideo = qb.queryForFirst();
            } while(video != null && randomVideo != null && video.getVideoId().equals(randomVideo.getVideoId()));

            return randomVideo;
        });
    }

    private long countOfListedVideo() {
        Long result = executeSync(() -> listedVideoDao.countOf());
        return result == null ? 0L : result;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private <T> T executeSync(Query<T> query) {
        try {
            return query.execute();
        } catch(Exception e) {
            if(BuildConfig.DEBUG) {
                Log.e(getClass().getName(), e.getMessage(), e);
            }
            return null;
        }
    }

}
