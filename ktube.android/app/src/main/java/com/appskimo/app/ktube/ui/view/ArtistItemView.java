package com.appskimo.app.ktube.ui.view;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appskimo.app.ktube.Constants;
import com.appskimo.app.ktube.R;
import com.appskimo.app.ktube.domain.Artist;
import com.appskimo.app.ktube.event.OnSelectArtist;
import com.appskimo.app.ktube.support.GlideApp;
import com.appskimo.app.ktube.support.GlideRequests;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;

@EViewGroup(R.layout.view_artist_item)
public class ArtistItemView extends RelativeLayout {
    @ViewById(R.id.typeLView) View typeLView;
    @ViewById(R.id.typeLTitleView) TextView typeLTitleView;
    @ViewById(R.id.typeLImageView) ImageView typeLImageView;

    @ViewById(R.id.typeSView) View typeSView;
    @ViewById(R.id.typeSTitleView) TextView typeSTitleView;

    @ViewById(R.id.itemSelector) View itemSelector;

    private Artist artist;
    private GlideRequests glideRequests;

    public ArtistItemView(Context context) {
        super(context);
        glideRequests = GlideApp.with(this);
    }

    public void setData(Artist artist, Constants.ViewStyle type) {
        this.artist = artist;
        if (artist != null) {
            if (type.isSmall()) {
                typeLView.setVisibility(View.GONE);
                typeSView.setVisibility(View.VISIBLE);
                typeSTitleView.setText(artist.getName1() == null ? artist.getName2() : artist.getName1());
            } else {
                typeLView.setVisibility(View.VISIBLE);
                typeSView.setVisibility(View.GONE);
                typeLTitleView.setText(artist.getName1() == null ? artist.getName2() : artist.getName1());
                loadImage(typeLImageView, artist.getImageUrl(), artist.getThumbUrl());
            }
        }
    }

    @UiThread
    void loadImage(ImageView imageView, String target, String thumbnail) {
        if (glideRequests != null) {
            glideRequests
                    .load(target)
                    .fitCenter()
                    .format(DecodeFormat.PREFER_RGB_565)
                    .skipMemoryCache(true)
                    .override(320, 180)
                    .thumbnail(glideRequests.load(thumbnail).fitCenter().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.empty)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView);
        }
    }

    @Click(R.id.itemSelector)
    void clickItemSelector() {
        if (artist != null) {
            EventBus.getDefault().post(new OnSelectArtist(artist));
        }
    }

    public void releaseImageView() {
        if (glideRequests != null) {
            glideRequests.clear(typeLImageView);
        }
        typeLImageView.setImageDrawable(null);
    }

}