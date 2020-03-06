package com.appskimo.app.ktube.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.appskimo.app.ktube.BuildConfig;
import com.appskimo.app.ktube.Constants;
import com.appskimo.app.ktube.R;
import com.appskimo.app.ktube.event.OnLock;
import com.appskimo.app.ktube.event.OnMask;
import com.appskimo.app.ktube.event.OnTouchPlayerMask;
import com.appskimo.app.ktube.event.OnUnlock;
import com.appskimo.app.ktube.service.PrefsService_;
import com.appskimo.app.ktube.support.EventBusObserver;
import com.crashlytics.android.Crashlytics;
import com.ebanx.swipebtn.SwipeButton;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Fullscreen;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.androidannotations.api.UiThreadExecutor;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import io.fabric.sdk.android.Fabric;

@Fullscreen
@EActivity(R.layout.activity_lock)
public class LockActivity extends AppCompatActivity {
    @ViewById(R.id.layer) View layer;
    @ViewById(R.id.lockLayer) View lockLayer;
    @ViewById(R.id.clock) View clock;
    @ViewById(R.id.swipe) SwipeButton swipe;

    @Pref PrefsService_ prefs;
    @SystemService AudioManager audioManager;
    @SystemService PowerManager powerManager;
    @SystemService TelephonyManager telephonyManager;

    private PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if(state == TelephonyManager.CALL_STATE_RINGING) {
                finish();
            }
        }
    };

    private float initialBrightness;
    private boolean isMasked;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(Constants.applyLanguage(base, new PrefsService_(base)));
    }

    @AfterInject
    void afterInject() {
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLifecycle().addObserver(new EventBusObserver.AtCreateDestroy(this));
        if (Build.VERSION.SDK_INT > 26) {
            setShowWhenLocked(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }

        if (!BuildConfig.DEBUG) {
            FirebaseAnalytics.getInstance(this);
            Fabric.with(this, new Crashlytics());
        }
    }

    @AfterViews
    void afterViews() {
        swipe.setOnActiveListener(this::finish);
        initialBrightness = getWindow().getAttributes().screenBrightness;
        reserveDownBrightness();
        reserveMask();
        initClock();
    }

    private void initClock() {
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) clock.getLayoutParams();
        params.leftMargin = prefs.lockClockPosX().getOr(0);
        params.topMargin = prefs.lockClockPosY().getOr(0);
        clock.setLayoutParams(params);
        layer.invalidate();

        clock.setOnTouchListener(new View.OnTouchListener() {
            private int mXDelta;
            private int mYDelta;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int x = (int) event.getRawX();
                final int y = (int) event.getRawY();
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        mXDelta = x - params.leftMargin;
                        mYDelta = y - params.topMargin;
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_DOWN:
                    case MotionEvent.ACTION_POINTER_UP:
                        break;

                    case MotionEvent.ACTION_MOVE:
                        params.leftMargin = x - mXDelta;
                        params.topMargin = y - mYDelta;
                        prefs.lockClockPosX().put(params.leftMargin);
                        prefs.lockClockPosY().put(params.topMargin);
                        clock.setLayoutParams(params);
                        break;
                }

                layer.invalidate();
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().post(new OnLock());
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().post(new OnUnlock());
        finish();
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_BACK:
                return false;
            case KeyEvent.KEYCODE_MENU:
                return false;
            default:
                return false;
        }
    }

    @Receiver(actions = {Intent.ACTION_SCREEN_OFF}, registerAt = Receiver.RegisterAt.OnCreateOnDestroy)
    void onActionScreenOnOff(Context context, Intent intent) {
        finish();
    }

    @UiThread(delay = Constants.SEC_5, id = "reserveDownBrightness")
    void reserveDownBrightness() {
        applyBrightness(0.01F);
    }

    @UiThread(delay = Constants.SEC_30, id = "reserveMask")
    void reserveMask() {
        if (!isFinishing() && !isDestroyed()) {
            lockLayer.setVisibility(View.VISIBLE);
            clock.setVisibility(View.GONE);
            swipe.setVisibility(View.GONE);
            isMasked = true;
            EventBus.getDefault().post(new OnMask(true));
        }
    }

    @UiThread
    void cancelMask() {
        UiThreadExecutor.cancelAll("reserveDownBrightness");
        UiThreadExecutor.cancelAll("reserveMask");
        applyBrightness(initialBrightness);
        reserveDownBrightness();
        reserveMask();

        EventBus.getDefault().post(new OnMask(false));
        lockLayer.setVisibility(View.GONE);
        clock.setVisibility(View.VISIBLE);
        swipe.setVisibility(View.VISIBLE);
        isMasked = false;
    }

    @Click(R.id.lockLayer)
    void onClickLockLayer() {
        if (isMasked) {
            cancelMask();
        }
    }

    @Subscribe
    public void onEvent (OnTouchPlayerMask event) {
        if (isMasked) {
            cancelMask();
        } else {
            onClickLayer();
        }
    }

    @Click(R.id.layer)
    void onClickLayer() {
        UiThreadExecutor.cancelAll("reserveDownBrightness");
        UiThreadExecutor.cancelAll("reserveMask");
        applyBrightness(initialBrightness);
        reserveDownBrightness();
        reserveMask();
    }

    @UiThread
    void applyBrightness(float value) {
        if (!isFinishing()) {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.screenBrightness = value;
            getWindow().setAttributes(params);
        }
    }
}
