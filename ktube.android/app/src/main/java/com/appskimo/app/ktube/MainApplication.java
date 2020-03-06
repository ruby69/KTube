package com.appskimo.app.ktube;

import android.content.Context;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.appskimo.app.ktube.support.GlideApp;
import com.appskimo.app.ktube.support.SQLiteOpenHelper;
import com.crashlytics.android.Crashlytics;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EApplication;
import org.greenrobot.eventbus.EventBus;

import io.fabric.sdk.android.Fabric;

@EApplication
public class MainApplication extends MultiDexApplication {
    @AfterInject
    void afterInject() {
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
        EventBus.builder().logNoSubscriberMessages(false).sendNoSubscriberEvent(false).installDefaultEventBus();
        initializeDatabase();
    }

    @Background
    void initializeDatabase() {
        SQLiteOpenHelper sqliteOpenHelper = new SQLiteOpenHelper(this);
        try {
            sqliteOpenHelper.getWritableDatabase(); // invoke db initialize method after calling onCreate or onUpgrade.
        } catch (Exception e) {
            deleteDatabase(getString(R.string.db_name));

        } finally {
            sqliteOpenHelper.close();
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        GlideApp.get(this).clearMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        GlideApp.get(this).trimMemory(level);
    }
}
