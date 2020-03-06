package com.appskimo.app.ktube.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.appskimo.app.ktube.R;
import com.appskimo.app.ktube.event.OnYtMusic;
import com.appskimo.app.ktube.support.GlideApp;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;

@EViewGroup(R.layout.view_notice)
public class NoticeView extends RelativeLayout {
    @ViewById(R.id.imageView) ImageView imageView;

    public NoticeView(Context context) {
        super(context);
    }

    public NoticeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @AfterViews
    void afterViews() {
        loadImage();
    }

    @UiThread
    void loadImage() {
        GlideApp.with(this)
                .load(R.drawable.ytmusic)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.empty)
                .into(imageView);
    }

    @Click(R.id.imageView)
    void onClickImageView() {
        EventBus.getDefault().post(new OnYtMusic());
    }
}
