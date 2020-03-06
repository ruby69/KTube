package com.appskimo.app.ktube.support;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EReceiver;
import org.androidannotations.annotations.ReceiverAction;

@EReceiver
public class CommonReceiver extends BroadcastReceiver {
    @Bean NotiAlarmService notiAlarmService;

    @ReceiverAction(actions = Intent.ACTION_BOOT_COMPLETED)
    void onActionBootComplete(Context context) {
        notiAlarmService.reserve();
    }

    @ReceiverAction(actions = Intent.ACTION_PACKAGE_REPLACED, dataSchemes = "package")
    void onActionPackageReplaced(Context context) {
        notiAlarmService.reserve();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        notiAlarmService.reserve();
    }
}