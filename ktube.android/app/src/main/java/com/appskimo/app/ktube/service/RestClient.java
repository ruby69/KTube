package com.appskimo.app.ktube.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.appskimo.app.ktube.BuildConfig;
import com.appskimo.app.ktube.Constants;
import com.appskimo.app.ktube.On;
import com.appskimo.app.ktube.R;
import com.appskimo.app.ktube.domain.Artist;
import com.appskimo.app.ktube.domain.Meta;
import com.appskimo.app.ktube.domain.Overview;
import com.appskimo.app.ktube.domain.Page;
import com.appskimo.app.ktube.domain.RadioStation;
import com.appskimo.app.ktube.domain.Video;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@EBean(scope = EBean.Scope.Singleton)
public class RestClient {
    @RootContext Context context;
    @Pref PrefsService_ prefs;
    @SystemService ConnectivityManager connectivityManager;

    private RestApi restApi;

    @AfterInject
    void afterInject() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(context.getResources().getString(R.string.base_url)).addConverterFactory(JacksonConverterFactory.create()).build();
//        Retrofit retrofit = new Retrofit.Builder().baseUrl(context.getResources().getString(BuildConfig.DEBUG ? R.string.base_url_dev : R.string.base_url)).addConverterFactory(JacksonConverterFactory.create()).build();
        restApi = retrofit.create(RestApi.class);
    }

    private void log(final Call call, Throwable e) {
        if (BuildConfig.DEBUG) {
            Log.d("RestClient", call.request().url().toString(), e);
        }
    }

    private <T> void request(final Call<T> call, final On<T> on) {
        if (isConnected()) {
            call.enqueue(new retrofit2.Callback<T>() {
                @Override
                public void onResponse(Call<T> call, Response<T> response) {
                    if (on != null) {
                        T body = null;
                        if (response.isSuccessful()) {
                            body = response.body();
                            on.success(body);
                        } else {
                            toast(response.message());
                            on.failure(new RuntimeException(response.message()));
                        }
                        on.complete(body);
                    }
                }

                @Override
                public void onFailure(Call<T> call, Throwable t) {
                    log(call, t);
                    if (on != null) {
                        on.failure(t);
                        on.complete(null);
                    }
                    toast(R.string.message_common_net_error);
                }
            });
        } else {
            Throwable t = new RuntimeException("not connected");
            log(call, t);
            if (on != null) {
                on.failure(t);
                on.complete(null);
            }
            toast(R.string.message_common_net_error);
        }
    }


    private boolean isConnected() {
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE);
        } else {
            return false;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void fetchMeta(On<Meta> on) {
        request(restApi.meta(), on);
    }

    public void fetchOverview(String lang, On<Overview> on) {
        request(restApi.overview(lang), on);
    }

    public void fetchYoutubeVideos(Constants.VideoDomain videoDomain, int page, On<Page<Video>> on) {
        if (videoDomain.isVideo() || videoDomain.isMillionVideo()) {
            videos(videoDomain.getFetchCategory(), videoDomain.getArtistKey(), page, on);
        } else if (videoDomain.isFancam()) {
            fancams(videoDomain.getFetchCategory(), page, on);
        } else if (videoDomain.isKaraoke()) {
            karaokes(videoDomain.getFetchCategory(), videoDomain.getArtistKey(), page, on);
        } else if (videoDomain.isLyric()) {
            lyrics(videoDomain.getFetchCategory(), page, on);
        }
    }

    private void videos(Constants.FetchCategory fetchCategory, String artistKey, int page, On<Page<Video>> on) {
        if (fetchCategory.isBest()) {
            request(restApi.videosBest(fetchCategory.getCode(), page), on);
        } else if (fetchCategory.isMillionView()) {
            request(restApi.videosMillionView(fetchCategory.getCode(), page), on);
        } else {
            if(artistKey == null) {
                request(restApi.videos(page, fetchCategory.getCode()), on);
            } else {
                request(restApi.videos(artistKey, page, fetchCategory.getCode()), on);
            }
        }
    }

    private void fancams(Constants.FetchCategory fetchCategory, int page, On<Page<Video>> on) {
        if (fetchCategory.isBest()) {
            request(restApi.fancamsBest(fetchCategory.getCode(), page), on);
        } else {
            request(restApi.fancams(page, fetchCategory.getCode()), on);
        }
    }

    private void karaokes(Constants.FetchCategory fetchCategory, String artistKey, int page, On<Page<Video>> on) {
        if (fetchCategory.isBest()) {
            request(restApi.karaokeBest(fetchCategory.getCode(), page), on);
        } else {
            if(artistKey == null) {
                request(restApi.karaokes(page, fetchCategory.getCode()), on);
            } else {
                request(restApi.karaokes(artistKey, page, fetchCategory.getCode()), on);
            }
        }
    }

    private void lyrics(Constants.FetchCategory fetchCategory, int page, On<Page<Video>> on) {
        request(restApi.lyrics(page, fetchCategory.getCode()), on);
    }





    public void fetchArtists(On<Artist.Collection> on) {
        request(restApi.artists(prefs.userLanguage().get()), on);
    }

    public void fetchRadioStations(On<List<RadioStation>> on) {
        request(restApi.radioStations(), on);
    }

    public void fetchSearch(Constants.VideoDomain videoDomain, int page, String query, On<Page<Video>> on) {
        request(restApi.search(videoDomain.getFetchCategory().getCode(), page, query), on);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @UiThread
    void toast(int resId) {
        Toast.makeText(context, resId, Toast.LENGTH_LONG).show();
    }

    @UiThread
    void toast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

}
