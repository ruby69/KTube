package com.appskimo.app.ktube.ui.frags;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.appskimo.app.ktube.Constants;
import com.appskimo.app.ktube.On;
import com.appskimo.app.ktube.R;
import com.appskimo.app.ktube.domain.Overview;
import com.appskimo.app.ktube.domain.SupportLanguage;
import com.appskimo.app.ktube.service.PrefsService_;
import com.appskimo.app.ktube.service.RestClient;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.sharedpreferences.Pref;

@EFragment(R.layout.fragment_overview)
public class OverviewFragment extends Fragment {
    @Bean RestClient restClient;
    @Pref PrefsService_ prefs;

    @AfterViews
    void afterViews() {
        restClient.fetchOverview(prefs.userLanguage().get(), new On<Overview>().addSuccessListener(this::manipulate));
    }

    @UiThread(delay = 300L)
    void delayedManipulate(Overview overview) {
        manipulate(overview);
    }

    @UiThread
    void manipulate(Overview overview) {
        if (!isAdded()) {
            delayedManipulate(overview);
            return;
        }

        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        String languageCode = prefs.userLanguage().get();
        if (languageCode.equals(SupportLanguage.ar.getCode())) {
            ft.add(R.id.container, OverviewCard3Fragment_.builder().arg("videoDomain", Constants.VideoDomain.overviewVideoNew()).arg("items", overview.getNews()).arg("titleResId", R.string.label_title_new).build());
            ft.add(R.id.container, OverviewCard6Fragment_.builder().arg("videoDomain", Constants.VideoDomain.overviewVideoBestD1()).arg("items", overview.getVideos()).arg("titleResId", R.string.label_video_best).build());
            ft.add(R.id.container, OverviewCard3Fragment_.builder().arg("videoDomain", Constants.VideoDomain.overviewMillion15()).arg("items", overview.getMillions()).arg("titleResId", R.string.label_video_million).build());
            ft.add(R.id.container, OverviewCard3Fragment_.builder().arg("videoDomain", Constants.VideoDomain.overviewFancamBestD1()).arg("items", overview.getFancams()).arg("titleResId", R.string.label_fancam_best).build());
            ft.add(R.id.container, OverviewCard3Fragment_.builder().arg("videoDomain", Constants.VideoDomain.overviewLyricAll()).arg("items", overview.getLyrics()).arg("titleResId", R.string.label_lyric_best).build());
            ft.add(R.id.container, OverviewCard3Fragment_.builder().arg("videoDomain", Constants.VideoDomain.overviewKaraokeBestD1()).arg("items", overview.getKaraokes()).arg("titleResId", R.string.label_karaoke_best).build());
        } else {
            ft.add(R.id.container, OverviewScrollVideoFragment_.builder().arg("videoDomain", Constants.VideoDomain.overviewVideoNew()).arg("items", overview.getNews()).build());
            ft.add(R.id.container, OverviewCard6Fragment_.builder().arg("videoDomain", Constants.VideoDomain.overviewVideoBestD1()).arg("items", overview.getVideos()).arg("titleResId", R.string.label_video_best).build());
            ft.add(R.id.container, OverviewScrollArtistFragment_.builder().arg("items", overview.getArtists()).arg("titleResId", R.string.label_artist_best).build());
            ft.add(R.id.container, OverviewCardScrollVideoFragment_.builder().arg("videoDomain", Constants.VideoDomain.overviewMillion15()).arg("items", overview.getMillions()).arg("titleResId", R.string.label_video_million).build());
            ft.add(R.id.container, OverviewCard3Fragment_.builder().arg("videoDomain", Constants.VideoDomain.overviewFancamBestD1()).arg("items", overview.getFancams()).arg("titleResId", R.string.label_fancam_best).build());
            ft.add(R.id.container, OverviewCard3Fragment_.builder().arg("videoDomain", Constants.VideoDomain.overviewLyricAll()).arg("items", overview.getLyrics()).arg("titleResId", R.string.label_lyric_best).build());
            ft.add(R.id.container, OverviewCard3Fragment_.builder().arg("videoDomain", Constants.VideoDomain.overviewKaraokeBestD1()).arg("items", overview.getKaraokes()).arg("titleResId", R.string.label_karaoke_best).build());
        }
        ft.commitAllowingStateLoss();
    }
}
