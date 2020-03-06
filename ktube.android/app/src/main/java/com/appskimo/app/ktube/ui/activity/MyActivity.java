package com.appskimo.app.ktube.ui.activity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import com.appskimo.app.ktube.ui.frags.MyArtistFragment_;
import com.appskimo.app.ktube.ui.frags.MyVideoFragment_;
import com.google.android.material.tabs.TabLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;
import org.androidannotations.annotations.res.DrawableRes;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@EActivity(R.layout.activity_my)
public class MyActivity extends AppCompatActivity {
    @ViewById(R.id.toolbar) Toolbar toolbar;
    @ViewById(R.id.mainViewPager) ViewPager mainViewPager;
    @ViewById(R.id.tabLayout) TabLayout tabLayout;

    @Pref PrefsService_ prefs;
    @Bean MiscService miscService;

    @DrawableRes(R.drawable.ic_playlist_play_white_24dp) Drawable playlistIcon;
    @DrawableRes(R.drawable.ic_favorite_white_24dp) Drawable favoriteIcon;
    @DrawableRes(R.drawable.ic_face_white_24dp) Drawable faceIcon;

    @ColorRes(android.R.color.white) int whiteColor;

    private PagerAdapter pagerAdapter;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(Constants.applyLanguage(base, new PrefsService_(base)));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
    }

    @AfterViews
    void afterViews() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.title_my);

        mainViewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(mainViewPager);
        setupTabIcons();
        showImgSvcStopNoticeDialog();
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(playlistIcon);
        tabLayout.getTabAt(1).setIcon(favoriteIcon);
        tabLayout.getTabAt(2).setIcon(faceIcon);
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
                    MyVideoFragment_.builder().arg("videoDomain", Constants.VideoDomain.myPlaylist()).build(),
                    MyVideoFragment_.builder().arg("videoDomain", Constants.VideoDomain.myFavorite()).build(),
                    MyArtistFragment_.builder().build()
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

        private void clearReference() {
            if (items != null) {
                items.clear();
            }
        }
    }


    @UiThread
    void showImgSvcStopNoticeDialog() {
        final int noticeCount = prefs.imgSvcStopNoticeCount().get();
        if (noticeCount < 3) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setMessage(R.string.message_notice_about_image_service)
                    .setPositiveButton(R.string.label_continue, (dialog1, which) -> dialog1.dismiss())
                    .setOnDismissListener(dialog12 -> prefs.imgSvcStopNoticeCount().put(noticeCount + 1))
                    .create();

            if (!this.isFinishing()) {
                dialog.show();
            }
        }
    }

}
