package com.appskimo.app.ktube.support;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.appskimo.app.ktube.R;
import com.appskimo.app.ktube.domain.RadioStation;
import com.appskimo.app.ktube.service.RadioPlayerService;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.SystemService;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

@EService
public class RadioService extends Service {
    private static final int NOTIFICATION_ID = 999999993;
    private static final String NOTI_CHANNEL_ID = "ktube_noti_radio";
    private static final String ACTION_STOP_SERVICE = "com.appskimo.app.ktube.service.radio.stop";

    @SystemService NotificationManager notificationManager;
    @SystemService WifiManager wifiManager;
    @SystemService PowerManager powerManager;
    @SystemService TelephonyManager telephonyManager;
    @Bean RadioPlayerService radioPlayerService;

    private WifiManager.WifiLock wifiLock = null;
    private PowerManager.WakeLock wakeLock = null;

    private Player.EventListener listener = new Player.EventListener() {
        @Override public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {}
        @Override public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {}
        @Override public void onLoadingChanged(boolean isLoading) {}
        @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {}
        @Override public void onRepeatModeChanged(int repeatMode) {}
        @Override public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {}
        @Override public void onPlayerError(ExoPlaybackException error) {stopSelf();}
        @Override public void onPositionDiscontinuity(int reason) {}
        @Override public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {}
        @Override public void onSeekProcessed() {}
    };

    private PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if(state == TelephonyManager.CALL_STATE_RINGING) {
                stopSelf();
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (wifiLock == null) {
            wifiLock = wifiManager.createWifiLock(getResources().getText(R.string.title_service_radio).toString());
            wifiLock.setReferenceCounted(true);
            wifiLock.acquire();
        }

        if (wakeLock == null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getResources().getText(R.string.title_service_radio).toString());
            wakeLock.acquire();
        }
    }

    @Override
    public void onDestroy() {
        radioPlayerService.release();
        if (wifiLock != null) {
            wifiLock.release();
            wifiLock = null;
        }

        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }

        super.onDestroy();
    }

    @AfterInject
    void afterInject() {
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            if (ACTION_STOP_SERVICE.equals(intent.getAction())) {
                notificationManager.cancel(NOTIFICATION_ID);
                stopSelf();

            } else {
                RadioStation radioStation = (RadioStation) intent.getSerializableExtra("radioStation");
                if(radioStation != null) {
                    radioPlayerService.init(listener).onAir(radioStation);
                    startForeground(NOTIFICATION_ID, getNotification(radioStation));
                }
            }
        }
        return START_NOT_STICKY;
    }

    private Notification getNotification(RadioStation radioStation) {
        Intent intent = RadioService_.intent(getApplicationContext()).get();
        intent.setAction(ACTION_STOP_SERVICE);
        PendingIntent stopPendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = new Notification.Builder(getApplicationContext(), getNotificationChannel())
                    .setContentTitle(radioStation.getTitle())
                    .setContentText(getText(R.string.label_noti_radio_text))
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentIntent(stopPendingIntent)
                    .setSound(null)
                    .build();
        } else {
            notification = new NotificationCompat.Builder(getApplicationContext())
                    .setContentTitle(radioStation.getTitle())
                    .setContentText(getText(R.string.label_noti_radio_text))
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentIntent(stopPendingIntent)
                    .setPriority(Notification.PRIORITY_MIN)
                    .build();
        }

        notificationManager.notify(NOTIFICATION_ID, notification);
        return notification;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private String getNotificationChannel() {
        NotificationChannel notificationChannel = notificationManager.getNotificationChannel(NOTI_CHANNEL_ID);
        if (notificationChannel == null) {
            notificationChannel = new NotificationChannel(NOTI_CHANNEL_ID, getText(R.string.label_noti_channel_radio), NotificationManager.IMPORTANCE_MIN);
            notificationChannel.setDescription(getString(R.string.label_noti_channel_radio));
            notificationChannel.setSound(null, null);
            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);
            notificationChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        return NOTI_CHANNEL_ID;
    }

//    @Receiver(actions = {Intent.ACTION_HEADSET_PLUG})
//    void onHeadphonesPlug(Intent intent) {
//        int state = intent.getIntExtra("state", -1);
//        if(state == 0) {
//            stopSelf();
//        }
//    }


    public static void start(Context context, RadioStation radioStation) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(RadioService_.intent(context).extra("radioStation", radioStation).get());
        } else {
            RadioService_.intent(context).extra("radioStation", radioStation).start();
        }
    }
}
