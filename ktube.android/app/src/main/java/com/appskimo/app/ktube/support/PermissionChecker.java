package com.appskimo.app.ktube.support;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

public final class PermissionChecker {
    public final static int REQUIRED_PERMISSION_REQUEST_CODE = 2121;

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean isRequiredPermissionGranted(Context context) {
        if (isOverLollipop()) {
            return Settings.canDrawOverlays(context);
        }
        return true;
    }

    private static boolean isOverLollipop() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    public static boolean isOverKitkat() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static Intent createRequiredPermissionIntent(Context context) {
        if (isOverLollipop()) {
            return new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.fromParts("package", context.getPackageName(), null));
        }
        return null;
    }
}
