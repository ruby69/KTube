package com.appskimo.app.ktube.service;

import org.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import org.androidannotations.annotations.sharedpreferences.DefaultInt;
import org.androidannotations.annotations.sharedpreferences.DefaultLong;
import org.androidannotations.annotations.sharedpreferences.DefaultString;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref(value = SharedPref.Scope.UNIQUE)
public interface PrefsService {

    @DefaultInt(0)
    int appLaunchedCount();

    @DefaultString("en")
    String userLanguage();

    @DefaultLong(0)
    long lastRadioStationUid();

    @DefaultLong(0L)
    long launchedTime();

    @DefaultInt(0)
    int noticeCount();

    @DefaultInt(0)
    int floatingFrom();     // 0 : Rest , 1 : My

    int lockClockPosX();
    int lockClockPosY();

    @DefaultLong(0L)
    long lastUpdateCheckTime();

    @DefaultInt(0)
    int imgSvcStopNoticeCount();

    @DefaultLong(0)
    long musicChartNotiTime();

    @DefaultInt(0)
    int musicChartNotiCount();

}
