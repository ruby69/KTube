package com.appskimo.app.ktube.support;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;

@EBean(scope = EBean.Scope.Singleton)
public class NotiAlarmService {
    private static final long ONE_HOUR = 3600L * 1000L;
    private static final long INTERVAL = ONE_HOUR;

    @RootContext Context context;
    @SystemService AlarmManager alarmManager;

    public void reserve() {
        Intent intent = new Intent(context, NotiAlarmReceiver_.class);
        reserve(PendingIntent.getBroadcast(context, 0, intent, 0));
    }

    private void reserve(PendingIntent pendingIntent) {
        long time = System.currentTimeMillis() + INTERVAL;
        reserve(pendingIntent, time);
    }

    private void reserve(PendingIntent pendingIntent, long time) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);

            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);

            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
            }
        } catch (Exception e) {

        }
    }

}
