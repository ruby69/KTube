package com.appskimo.app.ktube.support;

import android.annotation.SuppressLint;
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
import androidx.core.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.appskimo.app.ktube.R;
import com.appskimo.app.ktube.event.OnPlayPause;
import com.appskimo.app.ktube.ui.view.FloatingPlayerView;
import com.appskimo.app.ktube.ui.view.FloatingPlayerView_;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.greenrobot.eventbus.EventBus;

@EService
public class FloatingService extends Service {
    private static final int NOTIFICATION_ID = 999999992;
    private static final String NOTI_CHANNEL_ID = "ktube_noti_floating";
    private static final String ACTION_STOP_SERVICE = "com.appskimo.app.ktube.support.FloatingService.ACTION_STOP_SERVICE";

    @SystemService NotificationManager notificationManager;
    @SystemService WifiManager wifiManager;
    @SystemService PowerManager powerManager;
    @SystemService TelephonyManager telephonyManager;

    private WifiManager.WifiLock wifiLock = null;
    private PowerManager.WakeLock wakeLock = null;
    private FloatingPlayerView floatingPlayerView;

    private PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if(state == TelephonyManager.CALL_STATE_RINGING && floatingPlayerView != null) {
                floatingPlayerView.pauseVideo();
            }
        }
    };

    @AfterInject
    void afterInject() {
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("WakelockTimeout")
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
        if (wifiLock != null) {
            wifiLock.release();
            wifiLock = null;
        }

        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }

        destroyFloatingPlayerView();
        stopForeground(true);

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            if (ACTION_STOP_SERVICE.equals(intent.getAction())) {
                notificationManager.cancel(NOTIFICATION_ID);
                stopSelf();

            } else {
                if (floatingPlayerView == null) {
                    floatingPlayerView = FloatingPlayerView_.build(this);
                    startForeground(NOTIFICATION_ID, getNotification());
                } else {
                    floatingPlayerView.readyVideo();
                }
            }
        }

        return START_STICKY;
    }

    private void destroyFloatingPlayerView() {
        if (floatingPlayerView != null) {
            floatingPlayerView.destroy();
            floatingPlayerView = null;
        }
    }

    private Notification getNotification() {
        Intent intent = FloatingService_.intent(getApplicationContext()).get();
        intent.setAction(ACTION_STOP_SERVICE);
        PendingIntent stopPendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = new Notification.Builder(getApplicationContext(), getNotificationChannel())
                    .setContentTitle(getText(R.string.label_noti_floating_title))
                    .setContentText(getText(R.string.label_noti_floating_text))
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentIntent(stopPendingIntent)
                    .setSound(null)
                    .build();
        } else {
            notification = new NotificationCompat.Builder(getApplicationContext())
                    .setContentTitle(getText(R.string.label_noti_floating_title))
                    .setContentText(getText(R.string.label_noti_floating_text))
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
            notificationChannel = new NotificationChannel(NOTI_CHANNEL_ID, getText(R.string.label_noti_channel_floating), NotificationManager.IMPORTANCE_MIN);
            notificationChannel.setDescription(getString(R.string.label_noti_channel_floating));
            notificationChannel.setSound(null, null);
            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);
            notificationChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        return NOTI_CHANNEL_ID;
    }

    @UiThread
    void toast(int resId) {
        Toast.makeText(getApplicationContext(), resId, Toast.LENGTH_LONG).show();
    }

    @Receiver(actions = {Intent.ACTION_SCREEN_OFF}, registerAt = Receiver.RegisterAt.OnCreateOnDestroy)
    void onActionScreenOnOff(Context context, Intent intent) {
        EventBus.getDefault().post(new OnPlayPause());
    }

    public static void start(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(FloatingService_.intent(context).get());
        } else {
            FloatingService_.intent(context).start();
        }
    }
}
