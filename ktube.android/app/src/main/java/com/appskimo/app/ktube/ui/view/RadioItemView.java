package com.appskimo.app.ktube.ui.view;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appskimo.app.ktube.R;
import com.appskimo.app.ktube.domain.RadioStation;
import com.appskimo.app.ktube.event.OnSelectRadioStation;
import com.appskimo.app.ktube.service.PrefsService_;
import com.appskimo.app.ktube.support.GlideApp;
import com.appskimo.app.ktube.support.GlideRequests;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.greenrobot.eventbus.EventBus;

@EViewGroup(R.layout.view_radio_item)
public class RadioItemView extends RelativeLayout {
    @ViewById(R.id.itemSelector) View itemSelector;
    @ViewById(R.id.title) TextView title;
    @ViewById(R.id.image) ImageView image;

    @Pref PrefsService_ prefs;

    private RadioStation radioStation;
    private GlideRequests glideRequests;

    public RadioItemView(Context context) {
        super(context);
        glideRequests = GlideApp.with(this);
    }

    public void setData(RadioStation radioStation) {
        this.radioStation = radioStation;
        itemSelector.setVisibility(View.VISIBLE);

        title.setText(radioStation.getTitle());
        loadImage(image, radioStation.getImage());
    }

    @UiThread
    void loadImage(ImageView imageView, String target) {
        if (glideRequests != null) {
            glideRequests
                    .load(target)
                    .fitCenter()
                    .format(DecodeFormat.PREFER_RGB_565)
                    .skipMemoryCache(true)
                    .override(160, 90)
                    .thumbnail(0.1f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.empty)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView);
        }
    }

    @Click(R.id.itemSelector)
    void clickItemSelector() {
        prefs.lastRadioStationUid().put(radioStation.getRadioStationUid());
        EventBus.getDefault().post(new OnSelectRadioStation(radioStation));
    }

    public void releaseImageView() {
        if (glideRequests != null) {
            glideRequests.clear(image);
        }
        image.setImageDrawable(null);
    }

}