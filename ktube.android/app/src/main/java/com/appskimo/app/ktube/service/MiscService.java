package com.appskimo.app.ktube.service;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.appskimo.app.ktube.BuildConfig;
import com.appskimo.app.ktube.Constants;
import com.appskimo.app.ktube.On;
import com.appskimo.app.ktube.R;
import com.appskimo.app.ktube.domain.Meta;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.res.StringRes;
import org.androidannotations.annotations.sharedpreferences.Pref;

@EBean(scope = EBean.Scope.Singleton)
public class MiscService {
    @RootContext Context context;

    @Pref PrefsService_ prefs;
    @Bean RestClient restClient;
    @StringRes(R.string.admob_app_id) String admobAppId;
    @StringRes(R.string.admob_banner_unit_id) String bannerAdUnitId;

    private AdRequest adRequest;
    private AdView rectangleAdView;

    private boolean initializedMobileAds;
    public void initializeMobileAds() {
        if (!initializedMobileAds) {
            initializedMobileAds = true;
            MobileAds.initialize(context, admobAppId);
            generateRectangleAdView(context);
        }
    }

    @UiThread
    public void loadBannerAdView(AdView adView) {
        if (adView != null && !adView.isLoading()) {
            adView.loadAd(getAdRequest());
        }
    }

    private AdRequest getAdRequest() {
        if(adRequest == null) {
            AdRequest.Builder builder = new AdRequest.Builder();
            if(BuildConfig.DEBUG) {
                for(String device : context.getResources().getStringArray(R.array.t_devices)) {
                    builder.addTestDevice(device);
                }
            }
            adRequest = builder.build();
        }
        return adRequest;
    }

    private AdView generateRectangleAdView(Context context) {
        if (rectangleAdView == null) {
            rectangleAdView = new AdView(context);
            rectangleAdView.setAdSize(AdSize.MEDIUM_RECTANGLE);
            rectangleAdView.setAdUnitId(bannerAdUnitId);
            loadBannerAdView(rectangleAdView);
        }
        return rectangleAdView;
    }

    @UiThread
    public void showAdDialog(Activity activity, String title, int positiveLabelResId, DialogInterface.OnClickListener positiveListener) {
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(title)
                .setPositiveButton(positiveLabelResId, positiveListener)
                .create();

        AdView adView = generateRectangleAdView(activity);
        if (adView != null) {
            ViewParent parent = adView.getParent();
            if(parent != null) {
                ((ViewGroup) parent).removeView(adView);
            }
            dialog.setView(adView);
        }

        if (!activity.isFinishing() && !activity.isDestroyed()) {
            dialog.show();
        }
    }

    @UiThread
    public void showAdDialog(Activity activity, On<Void> on) {
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(" ")
                .setPositiveButton(R.string.label_continue, (d,i) -> on.success(null))
                .setOnDismissListener(d -> on.success(null))
                .create();

        AdView adView = generateRectangleAdView(activity);
        if (adView != null) {
            ViewParent parent = adView.getParent();
            if(parent != null) {
                ((ViewGroup) parent).removeView(adView);
            }
            dialog.setView(adView);
        }

        if (!activity.isFinishing()) {
            dialog.show();
        }
    }

    public void checkVersion(On<Void> on) {
        long now = System.currentTimeMillis();
        long gap = now - prefs.lastUpdateCheckTime().getOr(0L);
        if (gap < Constants.DAY_1) {
            return;
        }

        restClient.fetchMeta(new On<Meta>().addSuccessListener(meta -> {
            if (meta.getAndroidVersion() > versionCode()) {
                on.success(null);
            }
        }).addCompleteListener(meta -> prefs.lastUpdateCheckTime().put(now)));
    }

    private long versionCode() {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch(Exception e) {
            return 0;
        }
    }
}
