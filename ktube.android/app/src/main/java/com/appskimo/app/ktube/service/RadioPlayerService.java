package com.appskimo.app.ktube.service;

import android.content.Context;
import android.net.Uri;

import com.appskimo.app.ktube.domain.RadioStation;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

@EBean(scope = EBean.Scope.Singleton)
public class RadioPlayerService {
    @RootContext Context context;

    private SimpleExoPlayer player;

    public RadioPlayerService init(Player.EventListener eventListener) {
        if(player != null) {
            release();
        }

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(trackSelectionFactory);
        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        player.addListener(eventListener);

        return this;
    }

    @Background(delay = 750L)
    public void onAir(RadioStation radioStation) {
        try {
            DefaultDataSourceFactory sourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "KPOP Radio"));
            MediaSource mediaSource = new ExtractorMediaSource.Factory(sourceFactory).createMediaSource(Uri.parse(radioStation.getStream()));

            if(player != null) {
                player.prepare(mediaSource);
                if(!player.getPlayWhenReady()) {
                    player.setPlayWhenReady(true);

                    if(controlCallback != null) {
                        controlCallback.playCall();
                    }
                }
            }

            if(controlCallback != null) {
                controlCallback.finishCall();
            }
        } catch (Exception e) {

        }
    }

    public void release() {
        try {
            if(player != null) {
                player.release();
                player = null;

                if(controlCallback != null) {
                    controlCallback.stopCall();
                }
            }
        } catch(Exception e) {
        } finally {
            if(controlCallback != null) {
                controlCallback.finishCall();
            }
        }
    }

    public boolean isPlaying() {
        return player != null && player.getPlayWhenReady();
    }

    public void setControlCallback(ControlCallable controlCallback) {
        this.controlCallback = controlCallback;
    }

    private ControlCallable controlCallback;

    public interface ControlCallable {
        void playCall();
        void stopCall();
        void finishCall();
    }
}
