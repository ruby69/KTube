package com.appskimo.app.ktube.ui.frags;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appskimo.app.ktube.Constants;
import com.appskimo.app.ktube.R;
import com.appskimo.app.ktube.domain.Video;
import com.appskimo.app.ktube.service.MiscService;
import com.appskimo.app.ktube.support.GlideRequests;
import com.appskimo.app.ktube.support.KtubeGlideModule;
import com.appskimo.app.ktube.ui.activity.PlayerRestActivity_;
import com.appskimo.app.ktube.ui.activity.VideoActivity_;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.ViewsById;
import org.androidannotations.annotations.res.StringRes;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

@EFragment(R.layout.fragment_overview_card6)
public class OverviewCard6Fragment extends Fragment {
    @ViewById(R.id.title) TextView titleView;
    @ViewsById({R.id.image1, R.id.image2, R.id.image3, R.id.image4, R.id.image5, R.id.image6}) List<ImageView> imageViews;
    @ViewsById({R.id.title1, R.id.title2, R.id.title3, R.id.title4, R.id.title5, R.id.title6}) List<TextView> titleViews;
    @ViewsById({R.id.info1, R.id.info2, R.id.info3, R.id.info4, R.id.info5, R.id.info6}) List<TextView> infoViews;

    @Bean MiscService miscService;
    @StringRes(R.string.video_img_medium) String imageMedium;
    @StringRes(R.string.video_img_thumb) String imageThumb;

    @FragmentArg("items") ArrayList<Video> items;
    @FragmentArg("videoDomain") Constants.VideoDomain videoDomain;
    @FragmentArg("titleResId") int titleResId;

    private static final NumberFormat numberFormat = NumberFormat.getInstance();
    private DateFormat dateFormat;
    private GlideRequests glideRequests;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        KtubeGlideModule.OnFragment observer = new KtubeGlideModule.OnFragment(this);
        getLifecycle().addObserver(observer);
        glideRequests = observer.getGlideRequests();
        dateFormat = android.text.format.DateFormat.getDateFormat(getContext());
    }

    @AfterViews
    void afterViews() {
        titleView.setText(titleResId);
        manipulate();
    }

    @UiThread
    void manipulate() {
        if (items != null && items.size() > 5) {
            for (int i = 0; i < 6; i++) {
                manipulate(items.get(i), i);
            }
        }
    }

    private void manipulate(Video item, int index) {
        ImageView imageView = imageViews.get(index);
        TextView titleView = titleViews.get(index);
        TextView infoView = infoViews.get(index);
        titleView.setText(item.getTitle());

        infoView.setVisibility(View.VISIBLE);
        if (videoDomain.isType1()) {
            infoView.setText(dateFormat.format(item.getPublishedAt()));
        } else if (videoDomain.isType2()) {
            infoView.setText(numberFormat.format(item.getViewCount()));
        } else if (videoDomain.isType3()) {
            infoView.setText(numberFormat.format(item.getRankViewCount()));
        } else {
            infoView.setVisibility(View.GONE);
        }

        if (index < 3) {
            loadImage(imageView, item.getVideoId(), imageMedium, 320, 180);
        } else {
            loadImage(imageView, item.getVideoId(), imageThumb, 120, 90);
        }
    }

    @UiThread
    void loadImage(ImageView imageView, String videoId, String urlFormat, int width, int height) {
        if (glideRequests != null && imageView != null) {
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

    @Click({R.id.card1, R.id.card2, R.id.card3, R.id.card4, R.id.card5, R.id.card6})
    void clickItemSelector(View view) {
        try {
            Video video = items.get(Integer.parseInt((String) view.getTag()));
            miscService.showAdDialog(getActivity(), video.getTitle(), R.string.label_continue, (dialog, i) -> PlayerRestActivity_.intent(getActivity()).video(video).videoDomain(videoDomain.clone(Constants.ViewStyle.MEDIUM)).start());
        } catch (Exception e) {
        }
    }

    @Click(R.id.more)
    void onClickMore() {
        VideoActivity_.intent(getActivity()).videoGroup(videoDomain.getVideoGroup()).overview(true).start();
    }

    @Override
    public void onDestroyView() {
        for(ImageView imageView : imageViews) {
            if (glideRequests != null) {
                glideRequests.clear(imageView);
            }
            imageView.setImageDrawable(null);
        }

        if (items != null) {
            items.clear();
            items = null;
        }
        super.onDestroyView();
    }
}