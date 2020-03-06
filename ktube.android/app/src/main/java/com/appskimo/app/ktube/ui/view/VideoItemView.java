package com.appskimo.app.ktube.ui.view;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appskimo.app.ktube.Constants;
import com.appskimo.app.ktube.On;
import com.appskimo.app.ktube.R;
import com.appskimo.app.ktube.domain.YoutubeVideo;
import com.appskimo.app.ktube.event.OnFavoriteVideo;
import com.appskimo.app.ktube.event.OnListedVideo;
import com.appskimo.app.ktube.event.OnSelectVideo;
import com.appskimo.app.ktube.service.KtubeService;
import com.appskimo.app.ktube.support.GlideApp;
import com.appskimo.app.ktube.support.GlideRequests;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;
import org.androidannotations.annotations.res.StringRes;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;

@EViewGroup(R.layout.view_video_item)
public class VideoItemView extends RelativeLayout {
    @ViewById(R.id.typeLView) View typeLView;
    @ViewById(R.id.typeLTitleView) TextView typeLTitleView;
    @ViewById(R.id.typeLImageView) ImageView typeLImageView;
    @ViewById(R.id.typeLPublishedAtView) TextView typeLPublishedAtView;
    @ViewById(R.id.typeLCountView) TextView typeLCountView;
    @ViewById(R.id.playlist) ImageButton playlist;
    @ViewById(R.id.favorite) ImageButton favorite;

    @ViewById(R.id.typeMView) View typeMView;
    @ViewById(R.id.typeMTitleView) TextView typeMTitleView;
    @ViewById(R.id.typeMImageView) ImageView typeMImageView;
    @ViewById(R.id.typeMPublishedAtView) TextView typeMPublishedAtView;
    @ViewById(R.id.typeMCountView) TextView typeMCountView;

    @ViewById(R.id.typeSView) View typeSView;
    @ViewById(R.id.typeSTitleView) TextView typeSTitleView;
    @ViewById(R.id.typeSImageView) ImageView typeSImageView;
    @ViewById(R.id.typeSInfoView) TextView typeSInfoView;

    @ViewById(R.id.itemSelector) View itemSelector;

    @Bean KtubeService ktubeService;

    @ColorRes(R.color.colorAccent) int accentColor;
    @ColorRes(R.color.white) int whiteColor;
    @StringRes(R.string.video_img_medium) String imageMedium;
    @StringRes(R.string.video_img_thumb) String imageThumb;

    private DateFormat dateFormat;
    private static final NumberFormat numberFormat = NumberFormat.getInstance();
    private GlideRequests glideRequests;

    private YoutubeVideo video;
    private Constants.VideoDomain videoDomain;

    public VideoItemView(Context context) {
        super(context);
        dateFormat = android.text.format.DateFormat.getDateFormat(context);
        glideRequests = GlideApp.with(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        EventBus.getDefault().unregister(this);
        super.onDetachedFromWindow();
    }

    @UiThread
    void loadImage(ImageView imageView, String videoId, String urlFormat, int width, int height) {
        if (glideRequests != null) {
            glideRequests
                    .load(String.format(urlFormat, videoId))
                    .centerCrop()
                    .format(DecodeFormat.PREFER_RGB_565)
                    .skipMemoryCache(true)
                    .override(width, height)
                    .thumbnail(0.1f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.empty)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView);
        }
    }

    public void setData(YoutubeVideo video, Constants.VideoDomain videoDomain) {
        this.video = video;
        this.videoDomain = videoDomain;

        typeLView.setVisibility(videoDomain.isLarge() ? View.VISIBLE : View.GONE);
        typeMView.setVisibility(videoDomain.isMedium() ? View.VISIBLE : View.GONE);
        typeSView.setVisibility(videoDomain.isSmall() ? View.VISIBLE : View.GONE);

        if (videoDomain.isLarge()) {
            manipulateTypeL();
        } else if (videoDomain.isMedium()) {
            manipulateTypeM();
        } else if (videoDomain.isSmall()) {
            manipulateTypeS();
        }
    }

    @UiThread
    void manipulateTypeL() {
        typeLTitleView.setText(video.getTitle());
        loadImage(typeLImageView, video.getVideoId(), imageMedium, 320, 180);
        checkFavorite();
        checkListed();

        if (videoDomain.isType1()) {
            typeLPublishedAtView.setVisibility(View.VISIBLE);
            typeLCountView.setVisibility(View.GONE);
            populatePublishedAt(typeLPublishedAtView, video.getPublishedAt());

        } else if (videoDomain.isType2()) {
            typeLPublishedAtView.setVisibility(View.VISIBLE);
            typeLCountView.setVisibility(View.VISIBLE);
            populatePublishedAt(typeLPublishedAtView, video.getPublishedAt());
            typeLCountView.setText(numberFormat.format(video.getViewCount()));

        } else if (videoDomain.isType3()) {
            typeLPublishedAtView.setVisibility(View.VISIBLE);
            typeLCountView.setVisibility(View.VISIBLE);
            populatePublishedAt(typeLPublishedAtView, video.getPublishedAt());
            typeLCountView.setText(numberFormat.format(video.getRankViewCount()));
        }
    }

    @UiThread
    void manipulateTypeM() {
        typeMTitleView.setText(video.getTitle());
        loadImage(typeMImageView, video.getVideoId(), imageThumb, 120, 90);

        if (videoDomain.isType1()) {
            typeMPublishedAtView.setVisibility(View.VISIBLE);
            typeMCountView.setVisibility(View.GONE);
            populatePublishedAt(typeMPublishedAtView, video.getPublishedAt());

        } else if (videoDomain.isType2()) {
            typeMPublishedAtView.setVisibility(View.VISIBLE);
            typeMCountView.setVisibility(View.VISIBLE);
            populatePublishedAt(typeMPublishedAtView, video.getPublishedAt());
            typeMCountView.setText(numberFormat.format(video.getViewCount()));

        } else if (videoDomain.isType3()) {
            typeMPublishedAtView.setVisibility(View.VISIBLE);
            typeMCountView.setVisibility(View.VISIBLE);
            populatePublishedAt(typeMPublishedAtView, video.getPublishedAt());
            typeMCountView.setText(numberFormat.format(video.getRankViewCount()));
        }
    }

    @UiThread
    void manipulateTypeS() {
        typeSTitleView.setText(video.getTitle());
        loadImage(typeSImageView, video.getVideoId(), imageThumb, 120, 90);

        typeSInfoView.setVisibility(View.VISIBLE);
        if (videoDomain.isType1()) {
            populatePublishedAt(typeSInfoView, video.getPublishedAt());
        } else if (videoDomain.isType2()) {
            typeSInfoView.setText(numberFormat.format(video.getViewCount()));
        } else if (videoDomain.isType3()) {
            typeSInfoView.setText(numberFormat.format(video.getRankViewCount()));
        } else {
            typeSInfoView.setVisibility(View.GONE);
        }
    }

    @UiThread
    void populatePublishedAt(TextView textView, Date publishedAt) {
        if (publishedAt != null) {
            textView.setText(dateFormat.format(publishedAt));
        }
    }

    @Click(R.id.playlist)
    void onClickPlaylist() {
        final Long oldSeq = video.getSeq();
        ktubeService.toggleListedVideo(video, new On<Void>().addSuccessListener(aVoid -> EventBus.getDefault().post(new OnListedVideo(oldSeq))));
    }

    @Click(R.id.favorite)
    void onClickFavorite() {
        final Long oldSeq = video.getSeq();
        ktubeService.toggleFavoriteVideo(video, new On<Void>().addSuccessListener(aVoid -> EventBus.getDefault().post(new OnFavoriteVideo(oldSeq))));
    }

    @UiThread
    void toggle(ImageButton button, boolean on) {
        button.setColorFilter(on ? accentColor : whiteColor);
    }

    private void checkFavorite() {
        if (video != null) {
            ktubeService.findFavoriteVideoByVideoId(video.getVideoId(), new On<YoutubeVideo>().addSuccessListener(video -> toggle(favorite, video != null)));
        }
    }

    private void checkListed() {
        if (video != null) {
            ktubeService.findListedVideoByVideoId(video.getVideoId(), new On<YoutubeVideo>().addSuccessListener(video -> toggle(playlist, video != null)));
        }
    }

    @Click(R.id.itemSelector)
    void clickItemSelector() {
        EventBus.getDefault().post(new OnSelectVideo(video, videoDomain));
    }

    @Subscribe
    public void onEvent(OnFavoriteVideo event) {
        checkFavorite();
    }

    @Subscribe
    public void onEvent(OnListedVideo event) {
        checkListed();
    }

    public void releaseImageView() {
        if (glideRequests != null) {
            glideRequests.clear(typeLImageView);
            glideRequests.clear(typeMImageView);
            glideRequests.clear(typeSImageView);
        }
        typeLImageView.setImageDrawable(null);
        typeMImageView.setImageDrawable(null);
        typeSImageView.setImageDrawable(null);
    }
}
