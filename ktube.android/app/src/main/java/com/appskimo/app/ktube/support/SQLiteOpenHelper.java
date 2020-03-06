package com.appskimo.app.ktube.support;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.appskimo.app.ktube.BuildConfig;
import com.appskimo.app.ktube.R;
import com.appskimo.app.ktube.domain.Artist;
import com.appskimo.app.ktube.domain.VideoFavorite;
import com.appskimo.app.ktube.domain.VideoListed;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class SQLiteOpenHelper extends OrmLiteSqliteOpenHelper {
    protected Context context;

    public SQLiteOpenHelper(Context context) {
        super(context, context.getString(R.string.db_name), null, context.getResources().getInteger(R.integer.db_version));
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            dropAndCreateTables(connectionSource);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(getClass().getName(), e.getMessage(), e);
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            dropAndCreateTables(connectionSource);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(getClass().getName(), e.getMessage(), e);
            }
        }
    }

    private void dropAndCreateTables(ConnectionSource connectionSource) throws Exception {
        TableUtils.createTable(connectionSource, Artist.class);
        TableUtils.createTable(connectionSource, VideoFavorite.class);
        TableUtils.createTable(connectionSource, VideoListed.class);
    }

    public static class FavoriteVideoDao extends RuntimeExceptionDao<VideoFavorite, Long> {
        public FavoriteVideoDao(Dao<VideoFavorite, Long> dao) {
            super(dao);
        }
    }

    public static class ListedVideoDao extends RuntimeExceptionDao<VideoListed, Long> {
        public ListedVideoDao(Dao<VideoListed, Long> dao) {
            super(dao);
        }
    }

    public static class FavoriteArtistDao extends RuntimeExceptionDao<Artist, Long> {
        public FavoriteArtistDao(Dao<Artist, Long> dao) {
            super(dao);
        }
    }

}