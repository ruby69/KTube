package com.appskimo.app.ktube;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import com.appskimo.app.ktube.domain.SupportLanguage;
import com.appskimo.app.ktube.service.PrefsService_;

import java.io.Serializable;
import java.util.Locale;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public interface Constants {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class VideoDomain implements Serializable, Cloneable {
        private static final long serialVersionUID = -5692956657479012576L;

        private VideoGroup videoGroup;
        private FetchCategory fetchCategory;
        private ViewStyle viewStyle;
        private InfoType infoType;
        private String artistKey;

        public VideoDomain(VideoGroup videoGroup, FetchCategory fetchCategory, ViewStyle viewStyle, InfoType infoType) {
            this(videoGroup, fetchCategory, viewStyle, infoType, null);
        }

        public VideoDomain(VideoGroup videoGroup, ViewStyle viewStyle, InfoType infoType) {
            this(videoGroup, null, viewStyle, infoType);
        }

        public VideoDomain(VideoGroup videoGroup, FetchCategory fetchCategory) {
            this(videoGroup, fetchCategory, null, null);
        }

        public VideoDomain(VideoGroup videoGroup) {
            this(videoGroup, null, null, null);
        }

        public VideoDomain clone(ViewStyle viewStyle) {
            try {
                VideoDomain clone = (VideoDomain) super.clone();
                clone.setViewStyle(viewStyle);
                return clone;
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                return null;
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        public boolean isVideo() {
            return videoGroup.isVideo();
        }

        public boolean isMillionVideo() {
            return videoGroup.isMillionVideo();
        }

        public boolean isFancam() {
            return videoGroup.isFancam();
        }

        public boolean isLyric() {
            return videoGroup.isLyric();
        }

        public boolean isKaraoke() {
            return videoGroup.isKaraoke();
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        public boolean isBest() {
            return fetchCategory.isBest();
        }

        public boolean isMillionView() {
            return fetchCategory.isMillionView();
        }

        public boolean isSearch() {
            return fetchCategory.isSearch();
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        public boolean isLarge() {
            return viewStyle.isLarge();
        }

        public boolean isMedium() {
            return viewStyle.isMedium();
        }

        public boolean isSmall() {
            return viewStyle.isSmall();
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        public boolean isType1() {
            return infoType.isType1();
        }

        public boolean isType2() {
            return infoType.isType2();
        }

        public boolean isType3() {
            return infoType.isType3();
        }

        ////////////////////////////////////////////////////////////////////////////////////////////

        public boolean isPlaylist() {
            return fetchCategory.isPlaylist();
        }

        public boolean isFavorite() {
            return fetchCategory.isFavorite();
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        public static VideoDomain karaokeNew() {
            return new VideoDomain(VideoGroup.KARAOKE, FetchCategory.TIME, ViewStyle.LARGE, InfoType.TYPE1);
        }
        public static VideoDomain karaokeBestD1() {
            return new VideoDomain(VideoGroup.KARAOKE, FetchCategory.BEST_D1, ViewStyle.LARGE, InfoType.TYPE3);
        }
        public static VideoDomain karaokeBestD7() {
            return new VideoDomain(VideoGroup.KARAOKE, FetchCategory.BEST_D7, ViewStyle.LARGE, InfoType.TYPE3);
        }
        public static VideoDomain karaokeBestD30() {
            return new VideoDomain(VideoGroup.KARAOKE, FetchCategory.BEST_D30, ViewStyle.LARGE, InfoType.TYPE3);
        }
        public static VideoDomain karaokeAll() {
            return new VideoDomain(VideoGroup.KARAOKE, FetchCategory.VIEW, ViewStyle.LARGE, InfoType.TYPE2);
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        public static VideoDomain lyricNew() {
            return new VideoDomain(VideoGroup.LYRIC, FetchCategory.TIME, ViewStyle.LARGE, InfoType.TYPE1);
        }
        public static VideoDomain lyricAll() {
            return new VideoDomain(VideoGroup.LYRIC, FetchCategory.VIEW, ViewStyle.LARGE, InfoType.TYPE2);
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        public static VideoDomain fancamNew() {
            return new VideoDomain(VideoGroup.FANCAM, FetchCategory.TIME, ViewStyle.LARGE, InfoType.TYPE1);
        }
        public static VideoDomain fancamBestD1() {
            return new VideoDomain(VideoGroup.FANCAM, FetchCategory.BEST_D1, ViewStyle.LARGE, InfoType.TYPE3);
        }
        public static VideoDomain fancamBestD7() {
            return new VideoDomain(VideoGroup.FANCAM, FetchCategory.BEST_D7, ViewStyle.LARGE, InfoType.TYPE3);
        }
        public static VideoDomain fancamBestD30() {
            return new VideoDomain(VideoGroup.FANCAM, FetchCategory.BEST_D30, ViewStyle.LARGE, InfoType.TYPE3);
        }
        public static VideoDomain fancamAll() {
            return new VideoDomain(VideoGroup.FANCAM, FetchCategory.VIEW, ViewStyle.LARGE, InfoType.TYPE2);
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        public static VideoDomain million10() {
            return new VideoDomain(VideoGroup.MILLION, FetchCategory.MILLION_10, ViewStyle.LARGE, InfoType.TYPE2);
        }
        public static VideoDomain million15() {
            return new VideoDomain(VideoGroup.MILLION, FetchCategory.MILLION_15, ViewStyle.LARGE, InfoType.TYPE2);
        }
        public static VideoDomain million30() {
            return new VideoDomain(VideoGroup.MILLION, FetchCategory.MILLION_30M, ViewStyle.LARGE, InfoType.TYPE2);
        }
        public static VideoDomain million50() {
            return new VideoDomain(VideoGroup.MILLION, FetchCategory.MILLION_50M, ViewStyle.LARGE, InfoType.TYPE2);
        }
        public static VideoDomain millionAll() {
            return new VideoDomain(VideoGroup.MILLION, FetchCategory.VIEW, ViewStyle.LARGE, InfoType.TYPE2);
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        public static VideoDomain videoNew() {
            return new VideoDomain(VideoGroup.VIDEO, FetchCategory.TIME, ViewStyle.LARGE, InfoType.TYPE1);
        }
        public static VideoDomain videoBestD1() {
            return new VideoDomain(VideoGroup.VIDEO, FetchCategory.BEST_D1, ViewStyle.LARGE, InfoType.TYPE3);
        }
        public static VideoDomain videoBestD7() {
            return new VideoDomain(VideoGroup.VIDEO, FetchCategory.BEST_D7, ViewStyle.LARGE, InfoType.TYPE3);
        }
        public static VideoDomain videoBestD30() {
            return new VideoDomain(VideoGroup.VIDEO, FetchCategory.BEST_D30, ViewStyle.LARGE, InfoType.TYPE3);
        }
        public static VideoDomain videoAll() {
            return new VideoDomain(VideoGroup.VIDEO, FetchCategory.VIEW, ViewStyle.LARGE, InfoType.TYPE2);
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        public static VideoDomain artistVideoNew(String artistKey) {
            return new VideoDomain(VideoGroup.VIDEO, FetchCategory.TIME, ViewStyle.LARGE, InfoType.TYPE1, artistKey);
        }
        public static VideoDomain artistVideoAll(String artistKey) {
            return new VideoDomain(VideoGroup.VIDEO, FetchCategory.VIEW, ViewStyle.LARGE, InfoType.TYPE2, artistKey);
        }
        public static VideoDomain artistKaraokeAll(String artistKey) {
            return new VideoDomain(VideoGroup.KARAOKE, FetchCategory.VIEW, ViewStyle.LARGE, InfoType.TYPE2, artistKey);
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        public static VideoDomain overviewVideoNew() {
            return new VideoDomain(VideoGroup.VIDEO, FetchCategory.TIME, ViewStyle.SMALL, InfoType.TYPE1);
        }
        public static VideoDomain overviewVideoBestD1() {
            return new VideoDomain(VideoGroup.VIDEO, FetchCategory.BEST_D1, ViewStyle.NONE, InfoType.TYPE3);
        }
        public static VideoDomain overviewMillion15() {
            return new VideoDomain(VideoGroup.MILLION, FetchCategory.MILLION_15, ViewStyle.SMALL, InfoType.TYPE2);
        }
        public static VideoDomain overviewFancamBestD1() {
            return new VideoDomain(VideoGroup.FANCAM, FetchCategory.BEST_D1, ViewStyle.NONE, InfoType.TYPE3);
        }
        public static VideoDomain overviewLyricAll() {
            return new VideoDomain(VideoGroup.LYRIC, FetchCategory.VIEW, ViewStyle.NONE, InfoType.TYPE2);
        }
        public static VideoDomain overviewKaraokeBestD1() {
            return new VideoDomain(VideoGroup.KARAOKE, FetchCategory.BEST_D1, ViewStyle.NONE, InfoType.TYPE3);
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        public static VideoDomain searchVideo() {
            return new VideoDomain(VideoGroup.SEARCH, FetchCategory.SEARCH_V, ViewStyle.LARGE, InfoType.TYPE2);
        }
        public static VideoDomain searchFancam() {
            return new VideoDomain(VideoGroup.SEARCH, FetchCategory.SEARCH_F, ViewStyle.LARGE, InfoType.TYPE2);
        }
        public static VideoDomain searchLyric() {
            return new VideoDomain(VideoGroup.SEARCH, FetchCategory.SEARCH_L, ViewStyle.LARGE, InfoType.TYPE2);
        }
        public static VideoDomain searchKaraoke() {
            return new VideoDomain(VideoGroup.SEARCH, FetchCategory.SEARCH_K, ViewStyle.LARGE, InfoType.TYPE2);
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        public static VideoDomain myPlaylist() {
            return new VideoDomain(VideoGroup.MY, FetchCategory.MY_PLAYLIST, ViewStyle.LARGE, InfoType.TYPE1);
        }
        public static VideoDomain myFavorite() {
            return new VideoDomain(VideoGroup.MY, FetchCategory.MY_FAVORITE, ViewStyle.LARGE, InfoType.TYPE1);
        }
    }

    enum FetchCategory {
        TIME("time"), VIEW("view"),
        BEST_D1("1"), BEST_D7("7"), BEST_D30("30"), BEST_D90("90"), BEST_D180("180"), BEST_D365("365"),
        MILLION_10("10"), MILLION_15("15"), MILLION_30M("30"), MILLION_50M("50"),
        SEARCH_V("video"), SEARCH_L("lyric"), SEARCH_F("fancam"), SEARCH_K("karaoke"),
        MY_PLAYLIST("playlist"), MY_FAVORITE("favorite")
        ;


        private String code;

        FetchCategory(String code) {
            this.code = code;
        }

        public String getCode() {
            return this.code;
        }

        public boolean isBest() {
            return this == BEST_D1 || this == BEST_D7 || this == BEST_D30 || this == BEST_D90 || this == BEST_D180 || this == BEST_D365;
        }

        public boolean isMillionView() {
            return this == MILLION_10 || this == MILLION_15 || this == MILLION_30M || this == MILLION_50M;
        }

        public boolean isSearch() {
            return this == SEARCH_V || this == SEARCH_L || this == SEARCH_F || this == SEARCH_K;
        }

        public boolean isPlaylist() {
            return this == MY_PLAYLIST;
        }

        public boolean isFavorite() {
            return this == MY_FAVORITE;
        }
    }

    enum VideoGroup {
        VIDEO, MILLION, FANCAM, LYRIC, KARAOKE, SEARCH, MY;

        public boolean isVideo() {
            return this == VIDEO;
        }

        public boolean isMillionVideo() {
            return this == MILLION;
        }

        public boolean isFancam() {
            return this == FANCAM;
        }

        public boolean isLyric() {
            return this == LYRIC;
        }

        public boolean isKaraoke() {
            return this == KARAOKE;
        }

        public boolean isSearch() {
            return this == SEARCH;
        }

        public boolean isMy() {
            return this == MY;
        }

    }

    enum ViewStyle {
        LARGE, MEDIUM, SMALL, NONE;

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

    enum InfoType {
        TYPE1,  // publishedAt
        TYPE2,  // viewCount or (publishedAt + viewCount)
        TYPE3,  // rankViewCount or (publishedAt + rankViewCount)
        NONE;

        public boolean isType1() {
            return this == TYPE1;
        }

        public boolean isType2() {
            return this == TYPE2;
        }

        public boolean isType3() {
            return this == TYPE3;
        }
    }

    static Context applyLanguage(Context context, PrefsService_ prefs) {
        if (!prefs.userLanguage().exists()) {
            Locale locale = Locale.getDefault();
            String language = locale.getLanguage().toLowerCase();
            if (language.startsWith("zh")) {
                language = ("zh_hk".equals(language) || "zh_tw".equals(language)) ? "zh_Hant" : "zh_Hans";
            }

            if (SupportLanguage.isSupportLanguage(language)) {
                prefs.userLanguage().put(SupportLanguage.valueOf(language).name());
            } else {
                prefs.userLanguage().put(SupportLanguage.en.name());
            }
        }

        String languageCode = prefs.userLanguage().get();
        Locale locale = new Locale(languageCode);
        if ("zh_Hant".equals(languageCode)) {
            locale = new Locale("zh", "TW");
        } else if ("zh_Hans".equals(languageCode)) {
            locale = new Locale("zh", "CN");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Configuration configuration = context.getResources().getConfiguration();
            configuration.setLocale(locale);
            return context.createConfigurationContext(configuration);

        } else {
            Resources resources = context.getResources();
            Configuration configuration = resources.getConfiguration();
            configuration.locale = locale;
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            return context;
        }
    }

    int FLOATING_FROM_REST = 0;
    int FLOATING_FROM_MY = 1;

    long SEC_1 = 1000L;
    long SEC_3 = 3 * SEC_1;
    long SEC_5 = 5 * SEC_1;
    long SEC_10 = 10 * SEC_1;
    long SEC_30 = 30L * SEC_1;
    long MIN_1 = 60L * SEC_1;
    long MIN_3 = 3L * MIN_1;
    long MIN_4 = 4L * MIN_1;
    long MIN_15 = 15L * MIN_1;
    long MIN_30 = 30L * MIN_1;
    long HOUR_1 = 60L * MIN_1;
    long HOUR_10 = 10L * HOUR_1;
    long DAY_1 = 24L * HOUR_1;
}
