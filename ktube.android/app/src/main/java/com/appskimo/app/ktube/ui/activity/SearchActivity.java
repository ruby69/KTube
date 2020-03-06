package com.appskimo.app.ktube.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.appskimo.app.ktube.Constants;
import com.appskimo.app.ktube.R;
import com.appskimo.app.ktube.event.OnSearch;
import com.appskimo.app.ktube.service.MiscService;
import com.appskimo.app.ktube.service.PrefsService_;
import com.appskimo.app.ktube.ui.frags.SearchFragment;
import com.appskimo.app.ktube.ui.frags.SearchFragment_;
import com.google.android.material.tabs.TabLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.List;

@EActivity(R.layout.activity_search)
@OptionsMenu(R.menu.menu_search)
public class SearchActivity extends AppCompatActivity {
    @ViewById(R.id.toolbar) Toolbar toolbar;
    @ViewById(R.id.mainViewPager) ViewPager mainViewPager;
    @ViewById(R.id.tabLayout) TabLayout tabLayout;

    @Bean MiscService miscService;

    @OptionsMenuItem(R.id.menuSearch) MenuItem menuSearch;

    private PagerAdapter pagerAdapter;
    private String currentQuery = "";

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
        getSupportActionBar().setTitle(R.string.title_search);

        mainViewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(mainViewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        SearchView searchView = (SearchView) menuSearch.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                query = query.toLowerCase();
                if (query.trim().length() > 0 && !query.equals(currentQuery)) {
                    currentQuery = query;
                    getIntent().putExtra("query", query);
                    EventBus.getDefault().post(new OnSearch());
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
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

    private class PagerAdapter extends FragmentStatePagerAdapter {
        private List<SearchFragment> items;
        private int[] titles;

        public PagerAdapter(FragmentManager fm) {
            super(fm);

            items = Arrays.asList(
                    SearchFragment_.builder().arg("videoDomain", Constants.VideoDomain.searchVideo()).build(),
                    SearchFragment_.builder().arg("videoDomain", Constants.VideoDomain.searchFancam()).build(),
                    SearchFragment_.builder().arg("videoDomain", Constants.VideoDomain.searchLyric()).build(),
                    SearchFragment_.builder().arg("videoDomain", Constants.VideoDomain.searchKaraoke()).build()
            );
            titles = new int[] {R.string.title_music_video, R.string.title_fancam, R.string.title_lyrics, R.string.title_karaoke};
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
            return getResources().getText(titles[position]);
        }
    }
}
