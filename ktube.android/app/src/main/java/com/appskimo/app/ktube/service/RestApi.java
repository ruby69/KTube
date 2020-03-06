package com.appskimo.app.ktube.service;

import com.appskimo.app.ktube.domain.Artist;
import com.appskimo.app.ktube.domain.Meta;
import com.appskimo.app.ktube.domain.Overview;
import com.appskimo.app.ktube.domain.Page;
import com.appskimo.app.ktube.domain.RadioStation;
import com.appskimo.app.ktube.domain.Video;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RestApi {

    @GET("/api/meta/ktube")
    Call<Meta> meta();

    @GET("/api/v2/overview")
    Call<Overview> overview(@Query("lang") String lang);

    @GET("/api/v2/videos?scale=10")
    Call<Page<Video>> videos(@Query("page") int page, @Query("p[order]") String order);

    @GET("/api/v2/videos/{artistKey}?scale=10")
    Call<Page<Video>> videos(@Path("artistKey") String artistKey, @Query("page") int page, @Query("p[order]") String order);

    @GET("/api/v2/videos/best/{category}?scale=10")
    Call<Page<Video>> videosBest(@Path("category") String category, @Query("page") int page);

    @GET("/api/v2/videos/million/{category}?scale=10")
    Call<Page<Video>> videosMillionView(@Path("category") String category, @Query("page") int page);

    @GET("/api/v2/fancams?scale=10")
    Call<Page<Video>> fancams(@Query("page") int page, @Query("p[order]") String order);

    @GET("/api/v2/fancams/best/{category}?scale=10")
    Call<Page<Video>> fancamsBest(@Path("category") String category, @Query("page") int page);

    @GET("/api/v2/karaokes?scale=10")
    Call<Page<Video>> karaokes(@Query("page") int page, @Query("p[order]") String order);

    @GET("/api/v2/karaokes/{artistKey}?scale=10")
    Call<Page<Video>> karaokes(@Path("artistKey") String artistKey, @Query("page") int page, @Query("p[order]") String order);

    @GET("/api/v2/karaokes/best/{category}?scale=10")
    Call<Page<Video>> karaokeBest(@Path("category") String category, @Query("page") int page);

    @GET("/api/v2/lyrics?scale=10")
    Call<Page<Video>> lyrics(@Query("page") int page, @Query("p[order]") String order);




    @GET("/api/idols?scale=200")
    Call<Artist.Collection> artists(@Query("lang") String lang);

    @GET("/api/radios")
    Call<List<RadioStation>> radioStations();

    @GET("/api/search/{category}?scale=10")
    Call<Page<Video>> search(@Path("category") String category, @Query("page") int page, @Query("p[q]") String query);

}
