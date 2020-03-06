package com.appskimo.app.ktube.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.appskimo.app.ktube.Constants;
import com.appskimo.app.ktube.On;
import com.appskimo.app.ktube.R;
import com.appskimo.app.ktube.domain.Page;
import com.appskimo.app.ktube.domain.Video;
import com.appskimo.app.ktube.domain.YoutubeVideo;
import com.appskimo.app.ktube.event.OnCueOrLoadVideo;
import com.appskimo.app.ktube.event.OnPlayMode;
import com.appskimo.app.ktube.event.OnSelectVideo;
import com.appskimo.app.ktube.service.KtubeService;
import com.appskimo.app.ktube.service.MiscService;
import com.appskimo.app.ktube.service.PlayerBean;
import com.appskimo.app.ktube.service.PlayerMyBean;
import com.appskimo.app.ktube.service.PlayerRestBean;
import com.appskimo.app.ktube.service.PrefsService_;
import com.appskimo.app.ktube.support.EventBusObserver;
import com.appskimo.app.ktube.support.FloatingService_;
import com.appskimo.app.ktube.support.PermissionChecker;
import com.appskimo.app.ktube.ui.adapter.VideoRecyclerViewAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayerView;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.ui.DefaultPlayerUIController;
import com.pierfrancescosoffritti.androidyoutubeplayer.ui.PlayerUIController;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.Fullscreen;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;
import org.androidannotations.annotations.res.DrawableRes;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.androidannotations.api.BackgroundExecutor;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

@Fullscreen
@EActivity(R.layout.activity_player_list)
public class PlayerRestActivity extends AppCompatActivity {
    @ViewById(R.id.youtubeView) YouTubePlayerView youtubeView;
    @ViewById(R.id.playlist) FloatingActionButton playlist;
    @ViewById(R.id.favorite) FloatingActionButton favorite;
    @ViewById(R.id.floating) FloatingActionButton floating;
    @ViewById(R.id.playMode) FloatingActionButton playMode;
    @ViewById(R.id.refreshLayout) SwipeRefreshLayout refreshLayout;
    @ViewById(R.id.recyclerView) RecyclerView recyclerView;

    @Bean MiscService miscService;
    @Bean KtubeService ktubeService;
    @Bean PlayerRestBean playerBean;
    @Bean VideoRecyclerViewAdapter recyclerViewAdapter;

    @Pref PrefsService_ prefs;
    @SystemService AudioManager audioManager;

    @DrawableRes(R.drawable.ic_fast_rewind_black_24dp) Drawable rewIcon;
    @DrawableRes(R.drawable.ic_fast_forward_black_24dp) Drawable ffIcon;
    @DrawableRes(R.drawable.ic_skip_previous_black_24dp) Drawable prevIcon;
    @DrawableRes(R.drawable.ic_skip_next_black_24dp) Drawable nextIcon;
    @DrawableRes(R.drawable.ic_repeat_black_24dp) Drawable standardIcon;
    @DrawableRes(R.drawable.ic_repeat_black_24dp) Drawable repeatIcon;
    @DrawableRes(R.drawable.ic_repeat_one_black_24dp) Drawable repeatOneIcon;
    @DrawableRes(R.drawable.ic_shuffle_black_24dp) Drawable randomIcon;

    @ColorRes(R.color.colorAccent) int accentColor;
    @ColorRes(R.color.grey) int greyColor;
    @ColorRes(android.R.color.white) int whiteColor;

    @Extra("video") YoutubeVideo video;
    @Extra("videoDomain") Constants.VideoDomain videoDomain;
    @Extra("searchQuery") String searchQuery;

    private YouTubePlayer youtubePlayer;
    private PlayerConstants.PlayerState youtubePlayerState;
    private float currentSecond;
    private float currentDuration;
    private On<Page<Video>> on;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(Constants.applyLanguage(base, new PrefsService_(base)));
    }

    @AfterInject
    void afterInject() {
        playerBean.setVideoDomain(videoDomain);
        playerBean.setPlayerRestActivity(this);
        playerBean.setSearchQuery(searchQuery);
        recyclerViewAdapter.setVideoDomain(videoDomain);
        recyclerViewAdapter.setPageLoader(playerBean);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLifecycle().addObserver(new EventBusObserver.AtResumePause(this));
        FloatingService_.intent(getApplicationContext()).stop();
    }

    @AfterViews
    void afterViews() {
        initIcons();
        updateModeView();
        initYouTubePlayerView();
        checkFavorite();
        checkListed();
        if (PermissionChecker.isOverKitkat()) {
            floating.show();
        } else {
            floating.hide();
        }

        recyclerView.setAdapter(recyclerViewAdapter);
        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(true);
            playerBean.setPageOn(on);
            playerBean.load();
        });
        refreshLayout.setColorSchemeResources(R.color.red, R.color.green, R.color.blue, R.color.yellow);

        on = new On<Page<Video>>().addSuccessListener(this::manipulate);
        playerBean.setPageOn(on);
        playerBean.load();

        showYoutubeNoticeDialog();
    }

    private void initIcons() {
        prevIcon.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        nextIcon.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        rewIcon.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        ffIcon.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);

        standardIcon.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        repeatIcon.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        repeatOneIcon.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        randomIcon.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void onPause() {
        if (youtubePlayer != null && youtubePlayerState == PlayerConstants.PlayerState.PLAYING) {
            youtubePlayer.pause();
        }
        super.onPause();
    }

    private void initYouTubePlayerView() {
        getLifecycle().addObserver(youtubeView);
        postInitYouTubePlayerView();
        youtubeView.initialize(initializedYouTubePlayer -> initializedYouTubePlayer.addListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady() {
                youtubePlayer = initializedYouTubePlayer;
                postInitYouTubePlayerView();
                playerBean.loadVideo(video, new On<YoutubeVideo>().addSuccessListener(video -> cueVideo(video)));
            }

            @Override
            public void onStateChange(PlayerConstants.PlayerState state) {
                youtubePlayerState = state;
                if (state == PlayerConstants.PlayerState.VIDEO_CUED) {
                    manipulatePrevNextActions();
                    manipulateVideoTitle();

                } else if (state == PlayerConstants.PlayerState.PAUSED) {
                    manipulatePrevNextActions();

                } else if (state == PlayerConstants.PlayerState.PLAYING) {
                    manipulateFastActions();
                    manipulateVideoTitle();
                    onErrorCount = 0;

                } else if (state == PlayerConstants.PlayerState.UNSTARTED) {
                    manipulatePrevNextActions();
                    manipulateVideoTitle();

                } else if (state == PlayerConstants.PlayerState.ENDED) {
                    manipulatePrevNextActions();
                    loadNextVideo(false, new On<YoutubeVideo>().addSuccessListener(video -> loadVideo(video)));
                }
            }

            @Override
            public void onCurrentSecond(float second) {
                super.onCurrentSecond(second);
                currentSecond = second;
            }

            @Override
            public void onVideoDuration(float duration) {
                super.onVideoDuration(duration);
                currentDuration = duration;
            }

            int onErrorCount = 0;
            @Override
            public void onError(@NonNull PlayerConstants.PlayerError error) {
                super.onError(error);
                onErrorCount++;
                switch (error) {
                    case VIDEO_NOT_PLAYABLE_IN_EMBEDDED_PLAYER:
                        toast(R.string.message_video_unavailable);
                        break;
                    case VIDEO_NOT_FOUND:
                        toast(R.string.message_video_not_found);
                        break;
                    case UNKNOWN:
                    case INVALID_PARAMETER_IN_REQUEST:
                    case HTML_5_PLAYER:
                        toast(R.string.message_video_wrong);
                        break;
                }
                if (onErrorCount < 6) {
                    nextVideoOnError();
                } else {
                    onErrorCount = 0;
                }
            }
        }), true);
    }

    @Background(delay = 3000L, id = "nextVideoOnError", serial = "nextVideoOnError")
    void nextVideoOnError() {
        PlayerBean.PlayMode mode = playerBean.getCurrentMode();
        if (mode.isStandard() || mode.isRepeatOne()) {
            return;
        }
        loadNextVideo(true, new On<YoutubeVideo>().addSuccessListener(video -> loadVideo(video)));
    }

    void loadNextVideo(boolean force,  On<YoutubeVideo> on) {
        BackgroundExecutor.cancelAll("nextVideoOnError", true);
        playerBean.loadNextVideo(force, on);
    }

    @UiThread
    void postInitYouTubePlayerView() {
        findViewById(R.id.youtube_button).setVisibility(View.GONE);
    }

    @UiThread
    void manipulatePrevNextActions() {
        youtubeView.getPlayerUIController().setCustomAction1(prevIcon, v -> playerBean.loadPrevVideo(true, new On<YoutubeVideo>().addSuccessListener(this::cueOrLoadVideo)));
        youtubeView.getPlayerUIController().setCustomAction2(nextIcon, v -> loadNextVideo(true, new On<YoutubeVideo>().addSuccessListener(this::cueOrLoadVideo)));
    }

    @UiThread
    void manipulateFastActions() {
        youtubeView.getPlayerUIController().setCustomAction1(rewIcon, v -> seekTo(-10F));
        youtubeView.getPlayerUIController().setCustomAction2(ffIcon, v -> seekTo(10F));
    }

    @UiThread
    void manipulateVideoTitle() {
        youtubeView.getPlayerUIController().setVideoTitle(video.getTitle());
    }

    private int playTouchCount = 0;
    @Click(com.pierfrancescosoffritti.androidyoutubeplayer.R.id.play_pause_button)
    void onClickPlayPause(View view) {
        PlayerUIController playerUIController = youtubeView.getPlayerUIController();
        if (playerUIController instanceof DefaultPlayerUIController) {
            DefaultPlayerUIController defaultPlayerUIController = (DefaultPlayerUIController) playerUIController;

            if (youtubePlayerState != PlayerConstants.PlayerState.VIDEO_CUED) {
                defaultPlayerUIController.onClick(view);
                return;
            }

            playTouchCount++;
            if (playTouchCount % 5 == 0) {
                miscService.showAdDialog(this, new On<Void>().addSuccessListener(aVoid -> defaultPlayerUIController.onClick(view)));
            } else {
                defaultPlayerUIController.onClick(view);
            }
        }
    }

    private void seekTo(float second) {
        float time = currentSecond + second;
        if (time < 0F) {
            youtubePlayer.seekTo(0F);
        } else if (time >= currentDuration) {
            youtubePlayer.seekTo(currentDuration - 3F);
        } else {
            youtubePlayer.seekTo(time);
        }
    }

    private void cueOrLoadVideo(YoutubeVideo video) {
        if (youtubePlayerState == PlayerConstants.PlayerState.PLAYING) {
            loadVideo(video);
        } else {
            cueVideo(video);
        }
        EventBus.getDefault().post(new OnCueOrLoadVideo(video, OnCueOrLoadVideo.From.ACTIVITY));
    }

    private void cueVideo(YoutubeVideo video) {
        if (youtubePlayer != null && video != null) {
            resetVideo(video);
            pauseVideo();
            youtubePlayer.cueVideo(video.getVideoId(), 0);
        }
    }

    private void loadVideo(YoutubeVideo video) {
        if (youtubePlayer != null && video != null) {
            resetVideo(video);
            pauseVideo();
            youtubePlayer.loadVideo(video.getVideoId(), 0);
        }
    }

    private void resetVideo(YoutubeVideo video) {
        this.video = video;
        checkFavorite();
        checkListed();
        scrollTo(video);
    }

    @UiThread
    void scrollTo(YoutubeVideo video) {
        recyclerView.scrollToPosition(recyclerViewAdapter.getItemPosition(video));
    }

    @Click(R.id.floating)
    void onClickFloating() {
        if (!PermissionChecker.isRequiredPermissionGranted(this)) {
            Intent intent = PermissionChecker.createRequiredPermissionIntent(this);
            startActivityForResult(intent, PermissionChecker.REQUIRED_PERMISSION_REQUEST_CODE);
        } else {
            miscService.showAdDialog(this, new On<Void>().addSuccessListener(aVoid -> {
                prefs.floatingFrom().put(Constants.FLOATING_FROM_REST);
                FloatingService_.start(getApplicationContext());
            }));
        }
    }

    @UiThread
    void updateModeView() {
        PlayerMyBean.PlayMode currentMode = playerBean.getCurrentMode();
        playMode.setImageDrawable(getDrawableByMode(currentMode));
        playMode.setBackgroundTintList(ColorStateList.valueOf(currentMode.isStandard() ? greyColor : accentColor));
    }

    @Click(R.id.playMode)
    @UiThread
    void onClickPlayMode() {
        playerBean.nextMode();
        EventBus.getDefault().post(new OnPlayMode());
    }

    private Drawable getDrawableByMode(PlayerMyBean.PlayMode mode) {
        Drawable drawable = standardIcon;
        switch (mode) {
            case STANDARD:
                drawable = standardIcon;
                break;
            case REPEAT_ALL:
                drawable = repeatIcon;
                break;
            case REPEAT_ONE:
                drawable = repeatOneIcon;
                break;
            case RANDOM:
                drawable = randomIcon;
                break;
        }
        return drawable;
    }

    private void pauseVideo() {
        if (youtubePlayer != null && youtubePlayerState == PlayerConstants.PlayerState.PLAYING) {
            youtubePlayer.pause();
        }
    }

    @Subscribe
    public void onEvent(OnSelectVideo event) {
        playerBean.setCurrentVideo(event.getVideo());
        cueOrLoadVideo(event.getVideo());
    }

    @Subscribe
    public void onEvent(OnPlayMode event) {
        updateModeView();
    }

    @Subscribe
    public void onEvent(OnCueOrLoadVideo event) {
        if (event.fromView()) {
            playerBean.loadVideo(new On<YoutubeVideo>().addSuccessListener(video -> cueVideo(video)));
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Click(R.id.playlist)
    void onClickPlaylist() {
        ktubeService.toggleListedVideo(video, new On<Void>().addSuccessListener(aVoid -> checkListed()));
    }

    @Click(R.id.favorite)
    void onClickFavorite() {
        ktubeService.toggleFavoriteVideo(video, new On<Void>().addSuccessListener(aVoid -> checkFavorite()));
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

    @UiThread
    void toggle(FloatingActionButton floatingActionButton, boolean exist) {
        if (floatingActionButton != null) {
            floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(exist ? accentColor : greyColor));
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @UiThread
    void showYoutubeNoticeDialog() {
        final int noticeCount = prefs.noticeCount().get();
        if (noticeCount < 5) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setMessage(PermissionChecker.isOverKitkat() ? R.string.message_notice_about_youtube_terms : R.string.message_notice_about_youtube_terms_u21)
                    .setPositiveButton(R.string.label_continue, (dialog1, which) -> dialog1.dismiss())
                    .setOnDismissListener(dialog12 -> prefs.noticeCount().put(noticeCount + 1))
                    .create();

            if (!this.isFinishing()) {
                dialog.show();
            }
        }
    }







    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @UiThread
    void manipulate(Page<Video> response) {
        if (recyclerView != null && recyclerViewAdapter != null && response != null) {
            List<Video> items = response.getContents();
            if (response.getPage() == 1) {
                recyclerViewAdapter.reset(items);
                recyclerView.scrollToPosition(0);
            } else {
                recyclerViewAdapter.add(items);
            }
        }

        if (refreshLayout != null) {
            refreshLayout.setRefreshing(false);
        }
    }




    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @UiThread
    void toast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        if(youtubeView != null && youtubeView.isFullScreen()) {
            youtubeView.exitFullScreen();
            return;
        }

        if (youtubePlayer != null && youtubePlayerState == PlayerConstants.PlayerState.PLAYING) {
            youtubePlayer.pause();
            return;
        }

        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        on = null;
        playerBean.releasePageOn();
        playerBean.setPlayerRestActivity(null);

        if (recyclerViewAdapter != null) {
            recyclerViewAdapter.clear();
            recyclerViewAdapter.setPageLoader(null);
        }

        if (recyclerView != null) {
            recyclerView.setAdapter(null);
        }
        super.onDestroy();
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
                onBackPressed();
            default:
                return false;
        }
    }
}
