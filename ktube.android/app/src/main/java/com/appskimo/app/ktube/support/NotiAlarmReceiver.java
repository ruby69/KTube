package com.appskimo.app.ktube.support;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;

import com.appskimo.app.ktube.BuildConfig;
import com.appskimo.app.ktube.Constants;
import com.appskimo.app.ktube.On;
import com.appskimo.app.ktube.R;
import com.appskimo.app.ktube.domain.Page;
import com.appskimo.app.ktube.domain.SupportLanguage;
import com.appskimo.app.ktube.domain.Video;
import com.appskimo.app.ktube.service.PrefsService_;
import com.appskimo.app.ktube.service.RestClient;
import com.appskimo.app.ktube.ui.activity.MainActivity_;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EReceiver;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

@EReceiver
public class NotiAlarmReceiver extends BroadcastReceiver {
    private static final int NOTIFICATION_ID = 999999991;
    private static final String NOTI_CHANNEL_ID = "ktube_noti_alarm";
    private static final long COMPARED_TIME = 3600L * 30L * 1000L; // 30 hour
    private static final long ADDED_TIME = 3600L * 6L * 1000L; // 6 hour

    @SystemService NotificationManager notificationManager;
    @Pref PrefsService_ prefs;
    @Bean NotiAlarmService notiAlarmService;
    @Bean RestClient restClient;

    @Override
    public void onReceive(Context context, Intent intent) {
        notiAlarmService.reserve();
        if (notAllowedTime() || notOverTime()) {

        } else {
            applyLanguage(context);
            noti(context);
            updateLaunchedTime();
        }

        setResultCode(Activity.RESULT_OK);
    }

    private boolean notAllowedTime() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return !(hour == 8 || hour == 12 || hour == 18 || hour == 19 || hour == 20);
    }

    private boolean notOverTime() {
        long time = prefs.launchedTime().getOr(System.currentTimeMillis());
        return System.currentTimeMillis() - time <= COMPARED_TIME;
    }

    private void updateLaunchedTime() {
        long time = prefs.launchedTime().getOr(System.currentTimeMillis());
        prefs.launchedTime().put(time + ADDED_TIME);
    }

    private void noti(Context context) {
        restClient.fetchYoutubeVideos(Constants.VideoDomain.videoBestD1(), 1, new On<Page<Video>>().addSuccessListener(response -> {
            if (response != null) {
                try {
                    List<Video> videos = response.getContents();
                    if (videos != null && !videos.isEmpty()) {
                        Video video = videos.get(0);
                        if (video != null) {
                            notify(context, video);
                        }
                    }
                } catch (Exception e) {
                    if (BuildConfig.DEBUG) {
                        Log.e(getClass().getName(), e.getMessage(), e);
                    }
                }
            }
        }));
    }

    private Notification notify(Context context, Video video) {
        Intent openIntent = MainActivity_.intent(context).flags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP).get();
        PendingIntent openPendingIntent = PendingIntent.getActivity(context, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? new NotificationCompat.Builder(context, getNotificationChannel(context)) : new NotificationCompat.Builder(context);
        Notification notification = builder
                .setContentTitle(video.getTitle())
                .setContentText(context.getString(R.string.noti_message))
                .setTicker(video.getTitle())
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(openPendingIntent)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .build();

        notificationManager.notify(NOTIFICATION_ID, notification);
        return notification;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private String getNotificationChannel(Context context) {
        NotificationChannel notificationChannel = notificationManager.getNotificationChannel(NOTI_CHANNEL_ID);
        if (notificationChannel == null) {
            notificationChannel = new NotificationChannel(NOTI_CHANNEL_ID, context.getText(R.string.label_noti_channel_alarm), NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription(context.getString(R.string.label_noti_channel_alarm));
            notificationChannel.setSound(null, null);
            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);
            notificationChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        return NOTI_CHANNEL_ID;
    }

    private void applyLanguage(Context context) {
        if (!prefs.userLanguage().exists()) {
            Locale locale = Locale.getDefault();
            String language = locale.getLanguage().toLowerCase();
            if (language.startsWith("zh")) {
                language = ("zh_hk".equals(language) || "zh_tw".equals(language)) ? "zh_Hant" : "zh_Hans";
            }

            if (SupportLanguage.isSupportLanguage(language)) {
                prefs.userLanguage().put(SupportLanguage.valueOf(language).name());
            } else {
                prefs.userLanguage().put(SupportLanguage.en.name());
            }
        }

        String languageCode = prefs.userLanguage().get();
        Resources resources = context.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();

        if ("zh_Hant".equals(languageCode)) {
            configuration.locale = new Locale("zh", "TW");
        } else if ("zh_Hans".equals(languageCode)) {
            configuration.locale = new Locale("zh", "CN");
        } else {
            configuration.locale = new Locale(languageCode);
        }

        resources.updateConfiguration(configuration, displayMetrics);
    }
}
