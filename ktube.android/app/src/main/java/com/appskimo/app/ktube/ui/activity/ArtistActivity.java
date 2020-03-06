package com.appskimo.app.ktube.ui.activity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.appskimo.app.ktube.Constants;
import com.appskimo.app.ktube.On;
import com.appskimo.app.ktube.R;
import com.appskimo.app.ktube.domain.Artist;
import com.appskimo.app.ktube.event.OnMyArtist;
import com.appskimo.app.ktube.service.KtubeService;
import com.appskimo.app.ktube.service.MiscService;
import com.appskimo.app.ktube.service.PrefsService_;
import com.appskimo.app.ktube.support.KtubeGlideModule;
import com.appskimo.app.ktube.ui.frags.VideoItemsFragment_;
import com.google.android.material.tabs.TabLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;
import org.androidannotations.annotations.res.DrawableRes;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@EActivity(R.layout.activity_artist)
public class ArtistActivity extends AppCompatActivity {
    @ViewById(R.id.toolbar) Toolbar toolbar;
    @ViewById(R.id.mainViewPager) ViewPager mainViewPager;
    @ViewById(R.id.tabLayout) TabLayout tabLayout;
    @ViewById(R.id.like) ImageButton likeView;

    @Bean KtubeService ktubeService;
    @Bean MiscService miscService;
    @Pref PrefsService_ prefs;

    @DrawableRes(R.drawable.ic_timeline_white_24dp) Drawable timelineIcon;
    @DrawableRes(R.drawable.ic_trending_up_white_24dp) Drawable trendIcon;
    @DrawableRes(R.drawable.ic_mic_white_24dp) Drawable karaokeIcon;

    @ColorRes(android.R.color.white) int whiteColor;
    @ColorRes(R.color.colorAccent) int accentColor;

    @Extra("artist") Artist artist;

    private PagerAdapter pagerAdapter;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(Constants.applyLanguage(base, new PrefsService_(base)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLifecycle().addObserver(new KtubeGlideModule.OnActivity(this));
    }

    @AfterViews
    void afterViews() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        if (artist != null) {
            getSupportActionBar().setTitle(artist.getName1() == null ? artist.getName2() : artist.getName1());
        }

        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        mainViewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(mainViewPager);
        setupTabIcons();

        checkLiked();
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(timelineIcon);
        tabLayout.getTabAt(1).setIcon(trendIcon);
        tabLayout.getTabAt(2).setIcon(karaokeIcon);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Click(R.id.like)
    void onClickLike() {
        final Long oldSeq = artist.getFavoriteSeq();
        ktubeService.toggleFavoriteArtist(artist, new On<Void>().addSuccessListener(aVoid -> {
            checkLiked();
            EventBus.getDefault().post(new OnMyArtist(oldSeq));
        }));
    }

    private void checkLiked() {
        ktubeService.findFavoriteArtistByName2(artist.getName2(), new On<Artist>().addSuccessListener(artist -> adjustLikeButtonTint(artist != null)));
    }

    @UiThread
    void adjustLikeButtonTint(boolean exist) {
        likeView.setColorFilter(exist ? accentColor : whiteColor);
    }

    @Override
    public void onDestroy() {
        if (pagerAdapter != null) {
            pagerAdapter.clearReference();
            pagerAdapter = null;
        }
        super.onDestroy();
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {
        private List<Fragment> items;

        public PagerAdapter(FragmentManager fm) {
            super(fm);
            items = new ArrayList<>(Arrays.asList(
                    VideoItemsFragment_.builder().arg("videoDomain", Constants.VideoDomain.artistVideoNew(artist.getArtistKey())).build(),
                    VideoItemsFragment_.builder().arg("videoDomain", Constants.VideoDomain.artistVideoAll(artist.getArtistKey())).build(),
                    VideoItemsFragment_.builder().arg("videoDomain", Constants.VideoDomain.artistKaraokeAll(artist.getArtistKey())).build()
            ));
        }

        @Override
        public Fragment getItem(int position) {
            return items.get(position);
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }

        private void clearReference() {
            if (items != null) {
                items.clear();
            }
        }
    }
}
