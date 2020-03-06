package com.appskimo.app.ktube.ui.activity;

import android.content.Context;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.appskimo.app.ktube.Constants;
import com.appskimo.app.ktube.R;
import com.appskimo.app.ktube.service.MiscService;
import com.appskimo.app.ktube.service.PrefsService_;
import com.appskimo.app.ktube.ui.frags.VideoItemsFragment_;
import com.google.android.material.tabs.TabLayout;

import org.androidannotations.annotations.AfterExtras;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@EActivity(R.layout.activity_video)
public class VideoActivity extends AppCompatActivity {
    @ViewById(R.id.toolbar) Toolbar toolbar;
    @ViewById(R.id.mainViewPager) ViewPager mainViewPager;
    @ViewById(R.id.tabLayout) TabLayout tabLayout;

    @Bean MiscService miscService;

    @Extra("videoGroup") Constants.VideoGroup videoGroup;
    @Extra("overview") boolean overview;

    private int titleResId;
    private PagerAdapter pagerAdapter;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(Constants.applyLanguage(base, new PrefsService_(base)));
    }

    @AfterExtras
    void afterExtras() {
        if (videoGroup != null) {
            if (videoGroup.isKaraoke()) {
                titleResId = R.string.title_karaoke;
            } else if (videoGroup.isLyric()) {
                titleResId = R.string.title_lyrics;
            } else if (videoGroup.isFancam()) {
                titleResId = R.string.title_fancam;
            } else if (videoGroup.isMillionVideo()) {
                titleResId = R.string.title_million_video;
            } else {
                titleResId = R.string.title_music_video;
            }
        } else {
            titleResId = R.string.title_music_video;
        }
        pagerAdapter = new PagerAdapter(getSupportFragmentManager(), getFragments(), getTitles());
    }

    @AfterViews
    void afterViews() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(titleResId);

        mainViewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(mainViewPager);
        if (overview) {
            mainViewPager.setCurrentItem(1);
        }
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

    private List<Fragment> getFragments() {
        if (videoGroup.isKaraoke()) {
            return new ArrayList<>(Arrays.asList(
                    VideoItemsFragment_.builder().arg("videoDomain", Constants.VideoDomain.karaokeNew()).build(),
                    VideoItemsFragment_.builder().arg("videoDomain", Constants.VideoDomain.karaokeBestD1()).build(),
                    VideoItemsFragment_.builder().arg("videoDomain", Constants.VideoDomain.karaokeBestD7()).build(),
                    VideoItemsFragment_.builder().arg("videoDomain", Constants.VideoDomain.karaokeBestD30()).build(),
                    VideoItemsFragment_.builder().arg("videoDomain", Constants.VideoDomain.karaokeAll()).build()
            ));
        } else if (videoGroup.isLyric()) {
            return new ArrayList<>(Arrays.asList(
                    VideoItemsFragment_.builder().arg("videoDomain", Constants.VideoDomain.lyricNew()).build(),
                    VideoItemsFragment_.builder().arg("videoDomain", Constants.VideoDomain.lyricAll()).build()
            ));
        } else if (videoGroup.isFancam()) {
            return new ArrayList<>(Arrays.asList(
                    VideoItemsFragment_.builder().arg("videoDomain", Constants.VideoDomain.fancamNew()).build(),
                    VideoItemsFragment_.builder().arg("videoDomain", Constants.VideoDomain.fancamBestD1()).build(),
                    VideoItemsFragment_.builder().arg("videoDomain", Constants.VideoDomain.fancamBestD7()).build(),
                    VideoItemsFragment_.builder().arg("videoDomain", Constants.VideoDomain.fancamBestD30()).build(),
                    VideoItemsFragment_.builder().arg("videoDomain", Constants.VideoDomain.fancamAll()).build()
            ));
        } else if (videoGroup.isMillionVideo()) {
            return new ArrayList<>(Arrays.asList(
                    VideoItemsFragment_.builder().arg("videoDomain", Constants.VideoDomain.million10()).build(),
                    VideoItemsFragment_.builder().arg("videoDomain", Constants.VideoDomain.million15()).build(),
                    VideoItemsFragment_.builder().arg("videoDomain", Constants.VideoDomain.million30()).build(),
                    VideoItemsFragment_.builder().arg("videoDomain", Constants.VideoDomain.million50()).build(),
                    VideoItemsFragment_.builder().arg("videoDomain", Constants.VideoDomain.millionAll()).build()
            ));
        } else {
            return new ArrayList<>(Arrays.asList(
                    VideoItemsFragment_.builder().arg("videoDomain", Constants.VideoDomain.videoNew()).build(),
                    VideoItemsFragment_.builder().arg("videoDomain", Constants.VideoDomain.videoBestD1()).build(),
                    VideoItemsFragment_.builder().arg("videoDomain", Constants.VideoDomain.videoBestD7()).build(),
                    VideoItemsFragment_.builder().arg("videoDomain", Constants.VideoDomain.videoBestD30()).build(),
                    VideoItemsFragment_.builder().arg("videoDomain", Constants.VideoDomain.videoAll()).build()
            ));
        }
    }

    private List<Integer> getTitles() {
        if (videoGroup.isKaraoke()) {
            return new ArrayList<>(Arrays.asList(R.string.label_title_new, R.string.label_title_best_d1, R.string.label_title_best_d7, R.string.label_title_best_d30, R.string.label_title_lifetime));
        } else if (videoGroup.isLyric()) {
            return new ArrayList<>(Arrays.asList(R.string.label_title_new, R.string.label_title_lifetime));
        } else if (videoGroup.isFancam()) {
            return new ArrayList<>(Arrays.asList(R.string.label_title_new, R.string.label_title_best_d1, R.string.label_title_best_d7, R.string.label_title_best_d30, R.string.label_title_lifetime));
        } else if (videoGroup.isMillionVideo()) {
            return new ArrayList<>(Arrays.asList(R.string.label_title_over10m, R.string.label_title_over15m, R.string.label_title_over30m, R.string.label_title_over50m, R.string.label_title_lifetime));
        } else {
            return new ArrayList<>(Arrays.asList(R.string.label_title_new, R.string.label_title_best_d1, R.string.label_title_best_d7, R.string.label_title_best_d30, R.string.label_title_lifetime));
        }
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
        private List<Integer> titles;

        public PagerAdapter(FragmentManager fm, List<Fragment> items, List<Integer> titles) {
            super(fm);
            this.items = items;
            this.titles = titles;
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
            return getText(titles.get(position));
        }

        private void clearReference() {
            if (items != null) {
                items.clear();
            }

            if (titles != null) {
                titles.clear();
            }
        }
    }

}
