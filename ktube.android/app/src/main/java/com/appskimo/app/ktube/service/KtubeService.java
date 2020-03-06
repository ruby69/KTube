package com.appskimo.app.ktube.service;

import android.util.Log;

import com.appskimo.app.ktube.BuildConfig;
import com.appskimo.app.ktube.Constants;
import com.appskimo.app.ktube.On;
import com.appskimo.app.ktube.domain.Artist;
import com.appskimo.app.ktube.domain.More;
import com.appskimo.app.ktube.domain.VideoFavorite;
import com.appskimo.app.ktube.domain.VideoListed;
import com.appskimo.app.ktube.domain.YoutubeVideo;
import com.appskimo.app.ktube.support.Query;
import com.appskimo.app.ktube.support.SQLiteOpenHelper;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.ormlite.annotations.OrmLiteDao;

import java.util.List;

@EBean(scope = EBean.Scope.Singleton)
public class KtubeService {
    @OrmLiteDao(helper = SQLiteOpenHelper.class) SQLiteOpenHelper.ListedVideoDao listedVideoDao;
    @OrmLiteDao(helper = SQLiteOpenHelper.class) SQLiteOpenHelper.FavoriteVideoDao favoriteVideoDao;
    @OrmLiteDao(helper = SQLiteOpenHelper.class) SQLiteOpenHelper.FavoriteArtistDao favoriteArtistDao;

    public void findFavoriteArtistByName2(String name2, On<Artist> on) {
        execute(() -> favoriteArtistDao.queryBuilder().where().eq(Artist.FIELD_name2, new SelectArg(name2)).queryForFirst(), on);
    }

    public void findFavoriteArtists(On<List<Artist>> on) {
        execute(() -> favoriteArtistDao.queryBuilder().orderBy(Artist.FIELD_favoriteSeq, false).query(), on);
    }

    public void toggleFavoriteArtist(Artist artist, On<Void> on) {
        execute(() -> {
            List<Artist> exists = favoriteArtistDao.queryForEq(Artist.FIELD_name2, new SelectArg(artist.getName2()));
            if(exists.size() < 1) {
                favoriteArtistDao.createIfNotExists(artist);
            } else {
                favoriteArtistDao.delete(exists.get(0));
                artist.setFavoriteSeq(null);
            }
            return null;
        }, on);
    }

    public void findFavoriteVideoByVideoId(String videoId, On<YoutubeVideo> on) {
        execute(() -> favoriteVideoDao.queryBuilder().where().eq(YoutubeVideo.FIELD_videoId, new SelectArg(videoId)).queryForFirst(), on);
    }

    public void toggleFavoriteVideo(YoutubeVideo video, On<Void> on) {
        execute(() -> {
            List<VideoFavorite> exists = favoriteVideoDao.queryForEq(YoutubeVideo.FIELD_videoId, new SelectArg(video.getVideoId()));
            if(exists.size() < 1) {
                favoriteVideoDao.createIfNotExists(new VideoFavorite(video));
            } else {
                favoriteVideoDao.delete(exists.get(0));
                video.setSeq(null);
            }
            return null;
        }, on);
    }

    public void findListedVideoByVideoId(String videoId, On<YoutubeVideo> on) {
        execute(() -> listedVideoDao.queryBuilder().where().eq(YoutubeVideo.FIELD_videoId, new SelectArg(videoId)).queryForFirst(), on);
    }

    public void toggleListedVideo(YoutubeVideo video, On<Void> on) {
        execute(() -> {
            List<VideoListed> exists = listedVideoDao.queryForEq(YoutubeVideo.FIELD_videoId, new SelectArg(video.getVideoId()));
            if(exists.size() < 1) {
                listedVideoDao.createIfNotExists(new VideoListed(video));
            } else {
                listedVideoDao.delete(exists.get(0));
                video.setSeq(null);
            }
            return null;
        }, on);
    }

    public void retrieveMyVideo(Constants.VideoDomain videoDomain, More more, On<More> on) {
        if (videoDomain.isFavorite()) {
            retrieveFavorite(more, on);
        } else if (videoDomain.isPlaylist()) {
            retrieveListed(more, on);
        }
    }

    private void retrieveFavorite(More more, On<More> on) {
        execute(() -> {
            QueryBuilder<VideoFavorite, Long> qb1 = favoriteVideoDao.queryBuilder().orderBy(VideoFavorite.FIELD_seq, false).limit(more.getScale());
            if (more.getLastSeq() != null) {
                qb1.where().lt(VideoFavorite.FIELD_seq, more.getLastSeq());
            }
            List<? extends YoutubeVideo> list = qb1.query();
            more.setContent(list);

            if (list != null && !list.isEmpty()) {
                YoutubeVideo last = favoriteVideoDao.queryBuilder().orderBy(VideoFavorite.FIELD_seq, true).queryForFirst();
                YoutubeVideo lastContent = list.get(list.size() - 1);
                more.setHasMore(last.getSeq().longValue() < lastContent.getSeq().intValue());
            } else {
                more.setHasMore(false);
            }
            return more;
        }, on);
    }

    private void retrieveListed(More more, On<More> on) {
        execute(() -> {
            QueryBuilder<VideoListed, Long> qb1 = listedVideoDao.queryBuilder().orderBy(VideoListed.FIELD_seq, false).limit(more.getScale());
            if (more.getLastSeq() != null) {
                qb1.where().lt(VideoListed.FIELD_seq, more.getLastSeq());
            }

            List<? extends YoutubeVideo> list = qb1.query();
            more.setContent(list);

            if (list != null && !list.isEmpty()) {
                YoutubeVideo last = listedVideoDao.queryBuilder().orderBy(VideoListed.FIELD_seq, true).queryForFirst();
                YoutubeVideo lastContent = list.get(list.size() - 1);
                more.setHasMore(last.getSeq().longValue() < lastContent.getSeq().intValue());
            } else {
                more.setHasMore(false);
            }
            return more;
        }, on);
    }

    public void deleteMyVideo(Constants.VideoDomain videoDomain, On<Void> on) {
        if (videoDomain.isFavorite()) {
            deleteFavorite(on);
        } else if (videoDomain.isPlaylist()) {
            deleteListed(on);
        }
    }

    private void deleteFavorite(On<Void> on) {
        execute(() -> {
            favoriteVideoDao.deleteBuilder().delete();
            return null;
        }, on);
    }

    private void deleteListed(On<Void> on) {
        execute(() -> {
            listedVideoDao.deleteBuilder().delete();
            return null;
        }, on);
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Background
    <T> void execute(Query<T> query, On<T> on) {
        on.ready();
        T result = null;
        try {
            result = query.execute();
            on.success(result);
        } catch(Exception e) {
            if(BuildConfig.DEBUG) {
                Log.e(getClass().getName(), e.getMessage(), e);
            }
            on.failure(e);
        } finally {
            on.complete(result);
        }
    }

    @Background
    <T> void execute(Query<T> query) {
        try {
            query.execute();
        } catch(Exception e) {
            if(BuildConfig.DEBUG) {
                Log.e(getClass().getName(), e.getMessage(), e);
            }
        }
    }

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
