package com.appskimo.app.ktube.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.appskimo.app.ktube.Constants;
import com.appskimo.app.ktube.On;
import com.appskimo.app.ktube.R;
import com.appskimo.app.ktube.domain.YoutubeVideo;
import com.appskimo.app.ktube.event.OnCueOrLoadVideo;
import com.appskimo.app.ktube.event.OnLock;
import com.appskimo.app.ktube.event.OnMask;
import com.appskimo.app.ktube.event.OnPlayMode;
import com.appskimo.app.ktube.event.OnPlayPause;
import com.appskimo.app.ktube.event.OnTouchPlayerMask;
import com.appskimo.app.ktube.event.OnUnlock;
import com.appskimo.app.ktube.service.PlayerBean;
import com.appskimo.app.ktube.service.PlayerMyBean;
import com.appskimo.app.ktube.service.PlayerRestBean;
import com.appskimo.app.ktube.service.PrefsService_;
import com.appskimo.app.ktube.support.FloatingService_;
import com.appskimo.app.ktube.ui.activity.LockActivity_;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayerView;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.ui.DefaultPlayerUIController;
import com.pierfrancescosoffritti.androidyoutubeplayer.ui.PlayerUIController;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;
import org.androidannotations.annotations.res.DrawableRes;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.androidannotations.api.BackgroundExecutor;
import org.androidannotations.api.UiThreadExecutor;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

@EViewGroup(R.layout.view_floating_player)
public class FloatingPlayerView extends FrameLayout {
    @SystemService WindowManager windowManager;

    @ViewById(R.id.floatingLayer) View floatingLayer;
    @ViewById(R.id.youtubeView) YouTubePlayerView youtubeView;
    @ViewById(R.id.close) View close;
    @ViewById(R.id.lock) View lock;
    @ViewById(R.id.playMode) ImageButton playMode;
    @ViewById(R.id.resize) ImageButton resize;
    @ViewById(R.id.mask) View mask;

    @Pref PrefsService_ prefs;
    @Bean PlayerMyBean playerMyBean;
    @Bean PlayerRestBean playerRestBean;
    private PlayerBean playerBean;

    @DrawableRes(R.drawable.ic_fast_rewind_black_24dp) Drawable rewIcon;
    @DrawableRes(R.drawable.ic_fast_forward_black_24dp) Drawable ffIcon;
    @DrawableRes(R.drawable.ic_skip_previous_black_24dp) Drawable prevIcon;
    @DrawableRes(R.drawable.ic_skip_next_black_24dp) Drawable nextIcon;
    @DrawableRes(R.drawable.ic_repeat_black_24dp) Drawable standardIcon;
    @DrawableRes(R.drawable.ic_repeat_black_24dp) Drawable repeatIcon;
    @DrawableRes(R.drawable.ic_repeat_one_black_24dp) Drawable repeatOneIcon;
    @DrawableRes(R.drawable.ic_shuffle_black_24dp) Drawable randomIcon;
    @ColorRes(R.color.colorAccent) int accentColor;
    @ColorRes(R.color.white) int whiteColor;

    private YoutubeVideo video;
    private YouTubePlayer youtubePlayer;
    private PlayerConstants.PlayerState youtubePlayerState;
    private float currentSecond;
    private float currentDuration;
    private boolean readyYouTubePlayer;

    public FloatingPlayerView(Context context) {
        super(context);
    }

    public FloatingPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
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
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    @AfterViews
    void afterViews() {
        if (prefs.floatingFrom().get() == Constants.FLOATING_FROM_REST) {
            playerBean = playerRestBean;
        } else {
            playerBean = playerMyBean;
        }
        
        initIcons();
        updateModeView();
        initYouTubePlayerView();

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.CENTER;
        windowManager.addView(this, params);

        View panel = findViewById(R.id.panel);
        OnTouchListener onTouchListener = new OnTouchListener() {
            private int initX, initY;
            private int initTouchX, initTouchY;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int x = (int) event.getRawX();
                int y = (int) event.getRawY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initX = params.x;
                        initY = params.y;
                        initTouchX = x;
                        initTouchY = y;
                        return true;

                    case MotionEvent.ACTION_UP:
                        PlayerUIController playerUIController = youtubeView.getPlayerUIController();
                        if (playerUIController instanceof DefaultPlayerUIController) {
                            ((DefaultPlayerUIController) playerUIController).onClick(panel);
                            showScreenControllers();
                            delayHideScreenControllers();
                        }
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        params.x = initX + (x - initTouchX);
                        params.y = initY + (y - initTouchY);

                        windowManager.updateViewLayout(FloatingPlayerView.this, params);
                        return true;
                }
                return false;
            }
        };
        panel.setOnTouchListener(onTouchListener);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void initIcons() {
        prevIcon.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        nextIcon.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        rewIcon.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        ffIcon.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);

        standardIcon.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        repeatIcon.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN);
        repeatOneIcon.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN);
        randomIcon.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN);
    }

    private void initYouTubePlayerView() {
        postInitYouTubePlayerView();
        youtubeView.initialize(initializedYouTubePlayer -> initializedYouTubePlayer.addListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady() {
                readyYouTubePlayer = true;
                youtubePlayer = initializedYouTubePlayer;
                postInitYouTubePlayerView();
                playerBean.loadVideo(new On<YoutubeVideo>().addSuccessListener(video -> cueVideo(video)));
                keepViewOriginSize();
            }

            @Override
            public void onStateChange(@NonNull PlayerConstants.PlayerState state) {
                youtubePlayerState = state;
                if (state == PlayerConstants.PlayerState.VIDEO_CUED) {
                    manipulatePrevNextActions();
                    manipulateVideoTitle();
                    showScreenControllers();

                } else if (state == PlayerConstants.PlayerState.PAUSED) {
                    manipulatePrevNextActions();
                    showScreenControllers();

                } else if (state == PlayerConstants.PlayerState.PLAYING) {
                    manipulateFastActions();
                    manipulateVideoTitle();
                    onErrorCount = 0;

                    UiThreadExecutor.cancelAll("hideScreenControllers");
                    hideScreenControllers();

                } else if (state == PlayerConstants.PlayerState.UNSTARTED) {
                    showScreenControllers();
                    manipulateVideoTitle();

                } else if (state == PlayerConstants.PlayerState.ENDED) {
                    manipulatePrevNextActions();
                    showScreenControllers();
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
        youtubeView.getPlayerUIController().showFullscreenButton(false);
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
        if (video != null) {
            youtubeView.getPlayerUIController().setVideoTitle(video.getTitle());
        }
    }

    @UiThread(delay = 3000L, id = "hideScreenControllers")
    void hideScreenControllers() {
        close.setVisibility(View.GONE);
        lock.setVisibility(View.GONE);
        playMode.setVisibility(View.GONE);
        resize.setVisibility(View.GONE);
    }

    @UiThread
    void showScreenControllers() {
        UiThreadExecutor.cancelAll("hideScreenControllers");

        close.setVisibility(View.VISIBLE);
        lock.setVisibility(View.VISIBLE);
        playMode.setVisibility(View.VISIBLE);
        resize.setVisibility(View.VISIBLE);
    }

    @Background(delay = 300L)
    void delayHideScreenControllers() {
        hideScreenControllers();
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

    public void readyVideo() {
        YoutubeVideo video = playerBean.getCurrentVideo();
        if (video != null) {
            cueOrLoadVideo(video);
        } else {
            playerBean.loadVideo(new On<YoutubeVideo>().addSuccessListener(this::cueOrLoadVideo));
        }
    }

    private void cueOrLoadVideo(YoutubeVideo video) {
        if (youtubePlayerState == PlayerConstants.PlayerState.PLAYING) {
            loadVideo(video);
        } else {
            cueVideo(video);
        }
        EventBus.getDefault().post(new OnCueOrLoadVideo(video, OnCueOrLoadVideo.From.VIEW));
    }

    private void cueVideo(YoutubeVideo video) {
        if (youtubePlayer != null && video != null) {
            this.video = video;
            pauseVideo();
            youtubePlayer.cueVideo(video.getVideoId(), 0);
        }
    }

    private void loadVideo(YoutubeVideo video) {
        if (youtubePlayer != null && video != null) {
            this.video = video;
            pauseVideo();
            youtubePlayer.loadVideo(video.getVideoId(), 0);
        }
    }

    @UiThread
    void updateModeView() {
        PlayerBean.PlayMode currentMode = playerBean.getCurrentMode();
        playMode.setImageDrawable(getDrawableByMode(currentMode));
    }

    @Subscribe
    public void onEvent(OnCueOrLoadVideo event) {
        if (event.fromActivity()) {
            playerBean.loadVideo(new On<YoutubeVideo>().addSuccessListener(this::cueVideo));
        }
    }

    @Subscribe
    public void onEvent(OnPlayMode event) {
        updateModeView();
    }

    @Subscribe
    public void onEvent(OnPlayPause event) {
        pauseVideo();
    }

    @Subscribe
    public void onEvent(OnLock event) {
        mask.setVisibility(View.VISIBLE);
    }

    @Subscribe
    public void onEvent(OnUnlock event) {
        mask.setBackground(null);
        mask.setVisibility(View.GONE);
    }

    @Subscribe
    @UiThread
    public void onEvent(OnMask event) {
        if (event.isEnable()) {
            mask.setBackgroundResource(R.color.black_trans80);
        } else {
            mask.setBackground(null);
        }
    }

    @UiThread
    void alertWaitUntilInitializedPlayer() {
        toast(R.string.message_wait_player_init);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private int originWidth;
    private int originHeight;
    private boolean keepSize;

    @Click(R.id.playMode)
    void onClickPlayMode() {
        playerBean.nextMode();
        EventBus.getDefault().post(new OnPlayMode());
    }

    @Click(R.id.close)
    void onClickClose() {
        pauseVideo();
        FloatingService_.intent(getContext()).stop();
    }

    @Click(R.id.lock)
    void onClickLock() {
        if (!readyYouTubePlayer) {
            alertWaitUntilInitializedPlayer();
            return;
        }
        LockActivity_.intent(getContext()).flags(Intent.FLAG_ACTIVITY_NEW_TASK).start();
    }

    @Click(R.id.resize)
    void onClickResize() {
        if (!readyYouTubePlayer) {
            alertWaitUntilInitializedPlayer();
            return;
        }

        if (!keepSize) {
            keepViewOriginSize();
        }

        nextMode();
        FrameLayout.LayoutParams floatingLayerLayoutParams = (FrameLayout.LayoutParams)floatingLayer.getLayoutParams();
        if (currentMode.isMedium()) {
            floatingLayerLayoutParams.width = (int) (originWidth / 1.5F);
            floatingLayerLayoutParams.height = (int) (originHeight / 1.5F);

        } else if (currentMode.isSmall()) {
            floatingLayerLayoutParams.width = originWidth / 3;
            floatingLayerLayoutParams.height = originHeight / 3;

        } else {
            floatingLayerLayoutParams.width = originWidth;
            floatingLayerLayoutParams.height = originHeight;
        }

        floatingLayer.setLayoutParams(floatingLayerLayoutParams);

        ViewGroup.LayoutParams maskLayoutParams = mask.getLayoutParams();
        maskLayoutParams.width = floatingLayerLayoutParams.width;
        maskLayoutParams.height = floatingLayerLayoutParams.height;
        mask.setLayoutParams(maskLayoutParams);
    }

    private void keepViewOriginSize() {
        originWidth = floatingLayer.getWidth();
        originHeight = floatingLayer.getHeight();

        ViewGroup.LayoutParams maskLayoutParams = mask.getLayoutParams();
        maskLayoutParams.width = originWidth;
        maskLayoutParams.height = originHeight;
        mask.setLayoutParams(maskLayoutParams);

        keepSize = true;
    }

    private void nextMode() {
        currentMode = currentMode.next();
    }

    private ViewMode currentMode = ViewMode.LARGE;

    enum ViewMode {
        LARGE, MEDIUM, SMALL;

        ViewMode next() {
            ViewMode mode = MEDIUM;
            switch (this) {
                case LARGE:
                    mode = MEDIUM;
                    break;
                case MEDIUM:
                    mode = SMALL;
                    break;
                case SMALL:
                    mode = LARGE;
                    break;
            }
            return mode;
        }

        public boolean isLarge() {
            return this == LARGE;
        }

        public boolean isMedium() {
            return this == MEDIUM;
        }

        public boolean isSmall() {
            return this == SMALL;
        }
    }

    private Drawable getDrawableByMode(PlayerBean.PlayMode mode) {
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


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @UiThread
    void toast(int resId) {
        Toast.makeText(getContext(), resId, Toast.LENGTH_LONG).show();
    }

    @Click(R.id.mask)
    void onClickMask() {
        EventBus.getDefault().post(new OnTouchPlayerMask());
    }

    public void pauseVideo() {
        if (youtubePlayer != null && youtubePlayerState == PlayerConstants.PlayerState.PLAYING) {
            youtubePlayer.pause();
        }
    }

    public void destroy() {
        youtubeView.release();
        windowManager.removeView(this);
        playerBean.clear();
    }
}
