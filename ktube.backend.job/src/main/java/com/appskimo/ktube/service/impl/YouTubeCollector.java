package com.appskimo.ktube.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.appskimo.ktube.domain.model.Idol;
import com.appskimo.ktube.domain.model.Page;
import com.appskimo.ktube.domain.model.YouTube;
import com.appskimo.ktube.domain.model.YouTube.LyricsChannel;
import com.appskimo.ktube.domain.persist.IdolRepository;
import com.appskimo.ktube.domain.persist.YouTubeRepository;
import com.appskimo.ktube.service.YouTubeCollectable;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelContentDetails.RelatedPlaylists;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.ThumbnailDetails;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoContentDetails;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.VideoPlayer;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatistics;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class YouTubeCollector implements YouTubeCollectable {
    @Autowired private YouTubeRepository youTubeRepository;
    @Autowired private IdolRepository idolRepository;

    @Value("#{taskExecutor}") private ThreadPoolTaskExecutor taskExecutor;

    private static final String apiKey = "xxxxxxxxxx";
    private static final FastDateFormat fdf = FastDateFormat.getInstance("yyyyMMdd");

    @Override
    public void collectVideoIds() {
        List<Idol> idols = idolRepository.findAll();
        idols.stream()
        .filter(idol -> idol.getIdolUid().longValue() == 116L)
        .sorted((e1, e2) -> Long.compare(e1.getIdolUid(), e2.getIdolUid())).forEach(idol -> {
            taskExecutor.execute(() -> youTubeRepository.findPlayListByIdolUid(idol.getIdolUid()).forEach(playList -> {
                persistVideoIds(playList, collectVideoIds(playList.getPlayListId()));
            }));
        });
    }

    private void persistVideoIds(YouTube.PlayList playList, Collection<String> collection) {
        collection.forEach(videoId -> {
            YouTube.Video video = youTubeRepository.findVideoByVideoId(videoId);
            if(video == null) {
                video = new YouTube.Video();
                video.setVideoId(videoId);
                youTubeRepository.insertVideo(video);
            }

            try { youTubeRepository.insertIdolVideo(playList.getIdolUid(), video.getVideoUid());} catch(Exception e) {}
            try { youTubeRepository.insertPlayListVideo(playList.getPlayListUid(), video.getVideoUid());} catch(Exception e) {}
        });
    }

    private Collection<String> collectVideoIds(String playListId) {
        try {
            com.google.api.services.youtube.YouTube.PlaylistItems.List list = buildYouTube().playlistItems().list("contentDetails,status");
            list.setKey(apiKey);
            list.setPlaylistId(playListId);
            list.setMaxResults(50L);

            Set<String> set = new HashSet<>();
            String nextPageToken = null;
            do {
                PlaylistItemListResponse response = list.execute();
                set.addAll(
                    response.getItems().stream()
                    .filter(item -> item.getStatus() != null && "public".equals(item.getStatus().getPrivacyStatus()) && item.getContentDetails() != null)
                    .map(item -> item.getContentDetails().getVideoId())
                    .collect(Collectors.toSet())
                );
                nextPageToken = response.getNextPageToken();
                list.setPageToken(nextPageToken);
            } while(nextPageToken != null);

            return set;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptySet();
        }
    }

    private com.google.api.services.youtube.YouTube buildYouTube() {
        return new com.google.api.services.youtube.YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
            @Override public void initialize(HttpRequest request) throws IOException { }
        }).setApplicationName("youtubeDataApi").build();
    }

    @Override
    public void collectVideoInfos() {
        List<YouTube.Video> videos = youTubeRepository.findAllVideo();
        videos.stream()
        .filter(video -> video.getVideoUid().longValue() > 235629L)
        .forEach(video -> taskExecutor.execute(() -> collectVideoInfos(video)));
    }

    private void collectVideoInfos(YouTube.Video video) {
        if (video != null) {
            try {
                com.google.api.services.youtube.YouTube.Videos.List list = buildYouTube().videos().list("snippet, player, contentDetails, statistics");
                if(list != null) {
                    list.setKey(apiKey);
                    list.setId(video.getVideoId());

                    List<Video> items = list.execute().getItems();
                    if (items != null && !items.isEmpty()) {
                        youTubeRepository.updateVideo(populate(video, items.get(0)));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private YouTube.Video populate(YouTube.Video video, com.google.api.services.youtube.model.Video item) {
        VideoSnippet snippet = item.getSnippet();
        VideoPlayer player = item.getPlayer();
        VideoContentDetails contentDetails = item.getContentDetails();
        VideoStatistics statistics = item.getStatistics();

        if(snippet != null) {
            video.setTitle(snippet.getTitle());
            video.setDescription(snippet.getDescription());
            Date publishedAt = new Date(snippet.getPublishedAt().getValue());
            video.setPublishedAt(publishedAt);
            video.setPublishedAtIdx(Integer.parseInt(fdf.format(publishedAt)));

            ThumbnailDetails thumbnails = snippet.getThumbnails();
            if(thumbnails != null) {
                Thumbnail default1 = thumbnails.getDefault();
                Thumbnail medium = thumbnails.getMedium();
                Thumbnail high = thumbnails.getHigh();
                Thumbnail standard = thumbnails.getStandard();
                Thumbnail maxres = thumbnails.getMaxres();

                if(default1 != null) {
                    video.setThumbnailDefault(default1.getUrl());
                }

                if(medium != null) {
                    video.setThumbnailMedium(medium.getUrl());
                }

                if(high != null) {
                    video.setThumbnailHigh(high.getUrl());
                }

                if(standard != null) {
                    video.setThumbnailStandard(standard.getUrl());
                }

                if(maxres != null) {
                    video.setThumbnailMaxres(maxres.getUrl());
                }
            }
        }

        if(player != null) {
            video.setEmbedHtml(player.getEmbedHtml());
        }

        if(contentDetails != null) {
            video.setDefinition(contentDetails.getDefinition());
            video.setDuration(convert(contentDetails.getDuration()));
        }

        if(statistics != null && statistics.getViewCount() != null) {
            video.setViewCount(statistics.getViewCount().longValue());
        }

        return video;
    }

    private long convert(String duration) {
        duration = duration.replace("PT", "");
        int s = getS(duration);
        duration = duration.replace(s + "S", "");
        int m = getM(duration);
        duration = duration.replace(m + "M", "");
        int h = getH(duration);

        return h * 3600L + m * 60L + s;
    }

    private int getH(String duration) {
        return duration.contains("H") ? Integer.parseInt(duration.substring(0, duration.indexOf("H"))) : 0;
    }

    private int getM(String duration) {
        if (!duration.contains("M")) {
            return 0;
        }

        int start = duration.contains("H") ? duration.indexOf("H") + 1 : 0;
        return Integer.parseInt(duration.substring(start, duration.indexOf("M")));
    }

    private int getS(String duration) {
        if (!duration.contains("S")) {
            return 0;
        }

        int start = 0;
        if (duration.contains("M")) {
            start = duration.indexOf("M") + 1;
        } else if (duration.contains("H")) {
            start = duration.indexOf("H") + 1;
        }

        return Integer.parseInt(duration.substring(start, duration.indexOf("S")));
    }

//    @Override
//    public void collectNewVideos() {
//        final Set<String> newIdSet = Collections.synchronizedSet(new HashSet<>());
//        final Set<String> existIdSet = youTubeRepository.findAllVideoIds();
//        List<Idol> idols = idolRepository.findAll();
//        Collections.sort(idols, (e1, e2) -> Long.compare(e1.getIdolUid(), e2.getIdolUid()));
//
//        for(Idol idol : idols) {
//            taskExecutor.execute(() -> {
//                youTubeRepository.findPlayListByIdolUid(idol.getIdolUid()).forEach(playList -> {
//                    Collection<String> set = collectNewVideoIds(existIdSet, playList.getPlayListId());
//                    persistVideoIds(playList, set);
//                    newIdSet.addAll(set);
//                });
//            });
//        }
//        waitUntilDone();
//
//        newIdSet.forEach(videoId -> taskExecutor.execute(() -> collectVideoInfos(youTubeRepository.findVideoByVideoId(videoId))));
//        waitUntilDone();
//    }

    @Override
    public void collectNewVideos() {
        final Set<String> newIdSet = Collections.synchronizedSet(new HashSet<>());
        final Set<String> existIdSet = youTubeRepository.findAllVideoIds();
        List<Idol> idols = idolRepository.findAll();
        Collections.sort(idols, (e1, e2) -> Long.compare(e1.getIdolUid(), e2.getIdolUid()));

        for(Idol idol : idols) {
            taskExecutor.execute(() -> {
                youTubeRepository.findPlayListByIdolUid(idol.getIdolUid()).forEach(playList -> {
                    Collection<String> set = collectNewVideoIds(existIdSet, playList.getPlayListId());
                    persistVideoIds(playList, set);
                    newIdSet.addAll(set);
                });
            });
        }
        waitUntilDone();
        collectVideoInfos(newIdSet);
    }

    private void collectVideoInfos(Set<String> newIdSet) {
        List<String> newList = new ArrayList<>(newIdSet);

        for (List<String> partial : Lists.partition(newList, 50)) {
            List<YouTube.Video> newVideos = new ArrayList<>();
            partial.forEach(newId -> newVideos.add(youTubeRepository.findVideoByVideoId(newId)));

            Map<String, YouTube.Video> videoMap = newVideos.stream().collect(Collectors.toMap(YouTube.Video::getVideoId, Function.identity(), (video1, video2) -> video1));

            try {
                com.google.api.services.youtube.YouTube.Videos.List list = buildYouTube().videos().list("snippet, player, contentDetails, statistics");
                if(list != null) {
                    list.setKey(apiKey);
                    list.setId(String.join(",", videoMap.keySet()));
                    list.setMaxResults(50L);

                    List<Video> items = list.execute().getItems();
                    items.forEach(item -> {
                        YouTube.Video video = videoMap.get(item.getId());
                        youTubeRepository.updateVideo(populate(video, item));
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void waitUntilDone() {
        do {
            try {
                Thread.sleep(10 * 3000L);
            } catch (InterruptedException e) {}
        } while(taskExecutor.getActiveCount() > 0);
    }

    private Collection<String> collectNewVideoIds(Set<String> existSet, String playListId) {
        try {
            com.google.api.services.youtube.YouTube.PlaylistItems.List list = buildYouTube().playlistItems().list("contentDetails,status");
            list.setKey(apiKey);
            list.setPlaylistId(playListId);
            list.setMaxResults(50L);

            PlaylistItemListResponse response = list.execute();
            return response.getItems().stream()
                    .filter(item -> item.getStatus() != null && "public".equals(item.getStatus().getPrivacyStatus()) && item.getContentDetails() != null)
                    .map(item -> item.getContentDetails().getVideoId())
                    .filter(videoId -> !existSet.contains(videoId))
                    .collect(Collectors.toSet());

        } catch (Exception e) {
            log.error("occured exception at playListId-" + playListId, e);
            return Collections.emptySet();
        }
    }

    @Override
    public void updateViewCount() {
        Calendar calendar = Calendar.getInstance(Locale.KOREA);
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        final Date date = calendar.getTime();
        final int dateIdx = Integer.parseInt(fdf.format(date));

        Page page = new Page();
        page.setScale(50);
        page.setTotal(youTubeRepository.countVideosByPage(page));
        int pages = page.getTotalPages() + 1;
        for(int i = 1; i <= pages; i++) {
            page.setPage(i);
            List<YouTube.Video> videos = youTubeRepository.findVideosByPage(page);
            Map<String, YouTube.Video> videoMap = videos.stream().collect(Collectors.toMap(YouTube.Video::getVideoId, Function.identity(), (video1, video2) -> video1));
            List<YouTube.Video.ViewCount> vcList = new ArrayList<>();
            try {
                com.google.api.services.youtube.YouTube.Videos.List list = buildYouTube().videos().list("id, statistics");
                list.setKey(apiKey);
                list.setId(String.join(",", videoMap.keySet()));
                list.setMaxResults(50L);

                List<Video> items = list.execute().getItems();
                items.stream()
                .filter(item -> item.getStatistics() != null && item.getStatistics().getViewCount() != null)
                .forEach(item -> {
                    YouTube.Video video = videoMap.remove(item.getId());
                    VideoStatistics statistics = item.getStatistics();

                    long totalViewCount = statistics.getViewCount().longValue();
                    long viewCount = totalViewCount - video.getViewCount();
                    if(viewCount > 0) {
                        vcList.add(new YouTube.Video.ViewCount(video.getVideoUid(), totalViewCount, viewCount, date, dateIdx));
                    }
                });
            } catch (Exception e) {
                videoMap.clear();
                e.printStackTrace();
            }

            for (YouTube.Video.ViewCount vc : vcList) {
                youTubeRepository.updateVideoViewCountByVideoUid(vc.getTotalViewCount(), vc.getVideoUid());
                youTubeRepository.insertVideoViewCount(vc);
            }
            // videoMap.values().forEach(video -> youTubeRepository.deleteVideoByUid(video.getVideoUid()));
        }
    }

//    @Override
//    public void updateViewCount() {
//        Calendar calendar = Calendar.getInstance(Locale.KOREA);
//        calendar.add(Calendar.DAY_OF_MONTH, -1);
//
//        final Date date = calendar.getTime();
//        final int dateIdx = Integer.parseInt(fdf.format(date));
//
//        Page page = new Page();
//        page.setScale(300);
//        page.setTotal(youTubeRepository.countVideosByPage(page));
//        int pages = page.getTotalPages() + 1;
//        for(int i = 1; i <= pages; i++) {
//            page.setPage(i);
//            for(YouTube.Video video : youTubeRepository.findVideosByPage(page)) {
//                taskExecutor.execute(() -> {
//                    try {
//                        com.google.api.services.youtube.YouTube.Videos.List list = buildYouTube().videos().list("statistics");
//                        list.setKey(apiKey);
//                        list.setId(video.getVideoId());
//
//                        List<Video> items = list.execute().getItems();
//                        if(items.size() > 0) {
//                            Video item = items.get(0);
//                            VideoStatistics statistics = item.getStatistics();
//                            if(statistics != null) {
//                                long totalViewCount = statistics.getViewCount().longValue();
//                                long viewCount = totalViewCount - video.getViewCount();
//                                if(viewCount > 0) {
//                                    youTubeRepository.updateVideoViewCountByVideoUid(totalViewCount, video.getVideoUid());
//                                    youTubeRepository.insertVideoViewCount(new YouTube.Video.ViewCount(video.getVideoUid(), totalViewCount, viewCount, date, dateIdx));
//                                }
//                            }
//                        } else {
//                            youTubeRepository.deleteVideoByUid(video.getVideoUid());
//                        }
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                });
//            }
//        }
//    }

    @Override
    public void collectFancamVideoIds() {
        List<YouTube.FancamChannel> fancamChannels = youTubeRepository.findAllFancamChannel();
        fancamChannels.forEach(fancamChannel -> {
            taskExecutor.execute(() -> {
                String uploadsPlayListId = getUploadsPlayListId(fancamChannel);
                if (uploadsPlayListId != null) {
                    persistVideoIds(fancamChannel, collectVideoIds(uploadsPlayListId));
                }
            });
        });
    }

    @Override
    public void collectFancamVideoInfos() {
        List<YouTube.Video> videos = youTubeRepository.findAllFancamVideo();
        videos.forEach(video -> taskExecutor.execute(() -> collectFancamVideoInfos(video)));
    }

    private void persistVideoIds(YouTube.FancamChannel fancamChannel, Collection<String> collection) {
        collection.forEach(videoId -> {
            YouTube.Video video = youTubeRepository.findFancamVideoByVideoId(videoId);
            if(video == null) {
                video = new YouTube.Video();
                video.setVideoId(videoId);
                video.setFancamChannelUid(fancamChannel.getFancamChannelUid());
                youTubeRepository.insertFancamVideo(video);
            }
        });
    }

    @Override
    public String getUploadsPlayListId(YouTube.FancamChannel fancamChannel) {
        try {
            com.google.api.services.youtube.YouTube.Channels.List list = buildYouTube().channels().list("id,contentDetails");
            list.setKey(apiKey);
            if(fancamChannel.getType() == YouTube.FancamChannel.Type.ID) {
                list.setId(fancamChannel.getValue());
            } else if(fancamChannel.getType() == YouTube.FancamChannel.Type.USER) {
                list.setForUsername(fancamChannel.getValue());
            } else {
                return null;
            }

            Channel ch = list.execute().getItems().get(0);
            RelatedPlaylists relatedPlaylists = ch.getContentDetails().getRelatedPlaylists();

            return relatedPlaylists.getUploads();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void collectFancamVideoInfos(YouTube.Video video) {
        if (video != null) {
            try {
                com.google.api.services.youtube.YouTube.Videos.List list = buildYouTube().videos().list("snippet, player, contentDetails, statistics");
                if(list != null) {
                    list.setKey(apiKey);
                    list.setId(video.getVideoId());

                    VideoListResponse response = list.execute();
                    youTubeRepository.updateFancamVideo(populate(video, response.getItems().get(0)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void collectNewFancamVideos() {
        final Set<String> newIdSet = Collections.synchronizedSet(new HashSet<>());
        final Set<String> existIdSet = youTubeRepository.findAllFancamVideoIds();

        youTubeRepository.findAllFancamChannel().forEach(channel -> {
            Collection<String> set = collectNewVideoIds(existIdSet, channel.getUploadsPlaylistId());
            persistVideoIds(channel, set);
            newIdSet.addAll(set);
        });
        waitUntilDone();

        newIdSet.forEach(videoId -> taskExecutor.execute(() -> collectFancamVideoInfos(youTubeRepository.findFancamVideoByVideoId(videoId))));
        waitUntilDone();
    }

    @Override
    public void updateFancamViewCount() {
        Calendar calendar = Calendar.getInstance(Locale.KOREA);
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        final Date date = calendar.getTime();
        final int dateIdx = Integer.parseInt(fdf.format(date));

        Page page = new Page();
        page.setScale(50);
        page.setTotal(youTubeRepository.countFancamVideosByPage(page));
        int pages = page.getTotalPages() + 1;
        for(int i = 1; i <= pages; i++) {
            page.setPage(i);
            List<YouTube.Video> videos = youTubeRepository.findFancamVideosByPage(page);
            Map<String, YouTube.Video> videoMap = videos.stream().collect(Collectors.toMap(YouTube.Video::getVideoId, Function.identity(), (video1, video2) -> video1));
            List<YouTube.Video.ViewCount> vcList = new ArrayList<>();
            try {
                com.google.api.services.youtube.YouTube.Videos.List list = buildYouTube().videos().list("id, statistics");
                list.setKey(apiKey);
                list.setId(String.join(",", videoMap.keySet()));
                list.setMaxResults(50L);

                List<Video> items = list.execute().getItems();
                items.stream().filter(item -> item.getStatistics() != null).forEach(item -> {
                    YouTube.Video video = videoMap.remove(item.getId());
                    VideoStatistics statistics = item.getStatistics();

                    long totalViewCount = statistics.getViewCount().longValue();
                    long viewCount = totalViewCount - video.getViewCount();
                    if(viewCount > 0) {
                        vcList.add(new YouTube.Video.ViewCount(video.getVideoUid(), totalViewCount, viewCount, date, dateIdx));
                    }
                });
            } catch (Exception e) {
                videoMap.clear();
                e.printStackTrace();
            }

            for (YouTube.Video.ViewCount vc : vcList) {
                youTubeRepository.updateFancamVideoViewCountByVideoUid(vc.getTotalViewCount(), vc.getVideoUid());
                youTubeRepository.insertFancamVideoViewCount(vc);
            }
            // videoMap.values().forEach(video -> youTubeRepository.deleteVideoByUid(video.getVideoUid()));
        }
    }

//    @Override
//    public void updateFancamViewCount() {
//        Calendar calendar = Calendar.getInstance(Locale.KOREA);
//        calendar.add(Calendar.DAY_OF_MONTH, -1);
//
//        final Date date = calendar.getTime();
//        final int dateIdx = Integer.parseInt(fdf.format(date));
//
//        Page page = new Page();
//        page.setScale(300);
//        page.setTotal(youTubeRepository.countFancamVideosByPage(page));
//        int pages = page.getTotalPages() + 1;
//        for(int i = 1; i <= pages; i++) {
//            page.setPage(i);
//            for(YouTube.Video video : youTubeRepository.findFancamVideosByPage(page)) {
//                taskExecutor.execute(() -> {
//                    try {
//                        com.google.api.services.youtube.YouTube.Videos.List list = buildYouTube().videos().list("statistics");
//                        list.setKey(apiKey);
//                        list.setId(video.getVideoId());
//
//                        List<Video> items = list.execute().getItems();
//                        if(items.size() > 0) {
//                            Video item = items.get(0);
//                            VideoStatistics statistics = item.getStatistics();
//                            if(statistics != null) {
//                                long totalViewCount = statistics.getViewCount().longValue();
//                                long viewCount = totalViewCount - video.getViewCount();
//                                if(viewCount > 0) {
//                                    youTubeRepository.updateFancamVideoViewCountByVideoUid(totalViewCount, video.getVideoUid());
//                                    youTubeRepository.insertFancamVideoViewCount(new YouTube.Video.ViewCount(video.getVideoUid(), totalViewCount, viewCount, date, dateIdx));
//                                }
//                            }
//                        } else {
//                            youTubeRepository.deleteFancamVideoByUid(video.getVideoUid());
//                        }
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                });
//            }
//        }
//    }

    @Override
    public void collectShowVideoIds() {
        List<YouTube.ShowChannel> channels = youTubeRepository.findAllShowChannel();
        channels.forEach(channel -> {
            if(channel.getType() == YouTube.ShowChannel.Type.CH) {
                Collection<String> showPlayListIdSet = searchShowPlayListId(channel, 50L);
                showPlayListIdSet.forEach(showPlayListId -> {
                    taskExecutor.execute(() -> {
                        persistVideoIds(channel, collectVideoIds(showPlayListId));
                    });
                });
            }else if(channel.getType() == YouTube.ShowChannel.Type.PL) {
                taskExecutor.execute(() -> {
                    persistVideoIds(channel, collectVideoIds(channel.getValue()));
                });
            }
        });
    }

    private Collection<String> searchShowPlayListId(YouTube.ShowChannel channel, long resultCount) {
        try {
            com.google.api.services.youtube.YouTube.Search.List list = buildYouTube().search().list("snippet");

            if(list != null) {
                list.setKey(apiKey);
                list.setChannelId(channel.getValue());
                list.setQ(channel.getTitle());
                list.setOrder("date");
                list.setType("playlist");
                list.setMaxResults(resultCount);

                return list.execute().getItems().stream().map(item -> item.getId().getPlaylistId()).collect(Collectors.toSet());
            } else {
                return Collections.emptySet();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptySet();
        }
    }

    private void persistVideoIds(YouTube.ShowChannel showChannel, Collection<String> collection) {
        collection.forEach(videoId -> {
            YouTube.Video video = youTubeRepository.findShowVideoByVideoId(videoId);
            if(video == null) {
                video = new YouTube.Video();
                video.setVideoId(videoId);
                video.setShowChannelUid(showChannel.getShowChannelUid());

                try{
                    youTubeRepository.insertShowVideo(video);
                }catch(DuplicateKeyException e) {
                }
            }
        });
    }

    @Override
    public void collectShowVideoInfos() {
        List<YouTube.Video> videos = youTubeRepository.findAllShowVideo();
        videos.forEach(video -> taskExecutor.execute(() -> collectShowVideoInfos(video)));
    }

    private void collectShowVideoInfos(YouTube.Video video) {
        if (video != null) {
            try {
                com.google.api.services.youtube.YouTube.Videos.List list = buildYouTube().videos().list("snippet, player, contentDetails, statistics");
                if(list != null) {
                    list.setKey(apiKey);
                    list.setId(video.getVideoId());

                    VideoListResponse response = list.execute();
                    youTubeRepository.updateShowVideo(populate(video, response.getItems().get(0)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void collectNewShowVideos() {
        final Set<String> newIdSet = Collections.synchronizedSet(new HashSet<>());
        final Set<String> existIdSet = youTubeRepository.findAllShowVideoIds();

        List<YouTube.ShowChannel> channels = youTubeRepository.findAllShowChannel();
        channels.forEach(channel -> {
            if (channel.getType() == YouTube.ShowChannel.Type.CH) {
                Collection<String> showPlayListIdSet = searchShowPlayListId(channel, 1L);
                showPlayListIdSet.forEach(showPlayListId -> {
                    Collection<String> set = collectNewVideoIds(existIdSet, showPlayListId);
                    persistVideoIds(channel, set);
                    newIdSet.addAll(set);
                });
            } else if (channel.getType() == YouTube.ShowChannel.Type.PL) {
                Collection<String> set = collectNewVideoIds(existIdSet, channel.getValue());
                persistVideoIds(channel, set);
                newIdSet.addAll(set);
            }
        });
        waitUntilDone();

        newIdSet.forEach(videoId -> taskExecutor.execute(() -> collectShowVideoInfos(youTubeRepository.findShowVideoByVideoId(videoId))));
        waitUntilDone();
    }

    @Override
    public void cleanByTitleIsNull() {
        waitUntilDone();

        youTubeRepository.deleteVideoByTitleIsNull();
        youTubeRepository.deleteFancamVideoByTitleIsNull();
        youTubeRepository.deleteShowVideoByTitleIsNull();
    }




    private String getUploadsPlayListId(YouTube.LyricsChannel lyricsChannel) {
        try {
            com.google.api.services.youtube.YouTube.Channels.List list = buildYouTube().channels().list("id,contentDetails");
            list.setKey(apiKey);
            if(lyricsChannel.getType() == YouTube.LyricsChannel.Type.ID) {
                list.setId(lyricsChannel.getValue());
            } else if(lyricsChannel.getType() == YouTube.LyricsChannel.Type.USER) {
                list.setForUsername(lyricsChannel.getValue());
            } else {
                return null;
            }

            Channel ch = list.execute().getItems().get(0);
            RelatedPlaylists relatedPlaylists = ch.getContentDetails().getRelatedPlaylists();

            return relatedPlaylists.getUploads();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void updateLyricsUploadsPlaylistId() {
        List<LyricsChannel> channels = youTubeRepository.findAllLyricsChannel();
        channels.forEach(channel -> {
            String uploadsPlayListId = getUploadsPlayListId(channel);
            youTubeRepository.updateLyricsUploadsPlaylistId(uploadsPlayListId, channel.getLyricsChannelUid());
        });
    }

    @Override
    public void collectLyricsVideoIds() {
        List<LyricsChannel> channels = youTubeRepository.findAllLyricsChannel();
        channels.forEach(channel -> {
            taskExecutor.execute(() -> persistVideoIds(channel, collectVideoIds(channel.getUploadsPlaylistId())));
        });
    }

    @Override
    public void collectLyricsVideoInfos() {
        List<YouTube.Video> videos = youTubeRepository.findAllLyricsVideo();
        videos.forEach(video -> taskExecutor.execute(() -> collectLyricsVideoInfos(video)));
    }

    private void persistVideoIds(YouTube.LyricsChannel channel, Collection<String> collection) {
        collection.forEach(videoId -> {
            YouTube.Video video = youTubeRepository.findLyricsVideoByVideoId(videoId);
            if(video == null) {
                video = new YouTube.Video();
                video.setVideoId(videoId);
                video.setLyricsChannelUid(channel.getLyricsChannelUid());
                youTubeRepository.insertLyricsVideo(video);
            }
        });
    }

    private void collectLyricsVideoInfos(YouTube.Video video) {
        if (video != null) {
            try {
                com.google.api.services.youtube.YouTube.Videos.List list = buildYouTube().videos().list("snippet, player, contentDetails, statistics");
                if(list != null) {
                    list.setKey(apiKey);
                    list.setId(video.getVideoId());

                    VideoListResponse response = list.execute();
                    youTubeRepository.updateLyricsVideo(populate(video, response.getItems().get(0)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void collectNewLyricsVideos() {
        final Set<String> newIdSet = Collections.synchronizedSet(new HashSet<>());
        final Set<String> existIdSet = youTubeRepository.findAllLyricsVideoIds();

        youTubeRepository.findAllLyricsChannel().stream()
        .filter(channel -> channel.isScheduled())
        .forEach(channel -> {
            Collection<String> set = collectNewVideoIds(existIdSet, channel.getUploadsPlaylistId());
            persistVideoIds(channel, set);
            newIdSet.addAll(set);
        });
        waitUntilDone();

        newIdSet.forEach(videoId -> taskExecutor.execute(() -> collectLyricsVideoInfos(youTubeRepository.findLyricsVideoByVideoId(videoId))));
        waitUntilDone();
        removeLyricsVideoByTitle();
    }

    private void removeLyricsVideoByTitle() {
        List<YouTube.Video> videos = youTubeRepository.findAllLyricsVideo();
        videos.stream()
//        .filter(video -> video.getLyricsChannelUid().longValue() >= 66L && video.getLyricsChannelUid().longValue() <= 70L)
        .forEach(video -> {
            String title = video.getTitle();
            if (title != null) {
                title = title.toLowerCase();
                if(!(title.contains("lyric")
                        || (!title.contains("channel") && title.contains("han"))
                        || title.contains("rom")
                        || title.contains("eng")
                        || title.contains("trans")
                        || title.contains("sub")
                        || title.contains("karaoke")
                        || title.contains("vostfr")
                        )) {
                    youTubeRepository.deleteLyricsVideoByUid(video.getVideoUid());
                }

                if(title.contains("tutorial")) {
                    youTubeRepository.deleteLyricsVideoByUid(video.getVideoUid());
                }
            }
        });
    }

    @Override
    public void collectKaraokeVideoIds() {
        List<Idol> idols = idolRepository.findAll();
        idols.stream()
        .sorted((e1, e2) -> Long.compare(e1.getIdolUid(), e2.getIdolUid())).forEach(idol -> {
            taskExecutor.execute(() -> youTubeRepository.findKaraokePlayListByIdolUid(idol.getIdolUid()).forEach(playList -> {
                persistKaraokeVideoIds(playList, collectVideoIds(playList.getPlayListId()));
            }));
        });
    }

    private void persistKaraokeVideoIds(YouTube.PlayList playList, Collection<String> collection) {
        collection.forEach(videoId -> {
            YouTube.Video video = youTubeRepository.findKaraokeVideoByVideoId(videoId);
            if(video == null) {
                video = new YouTube.Video();
                video.setVideoId(videoId);
                youTubeRepository.insertKaraokeVideo(video);
            }

            try { youTubeRepository.insertIdolKaraokeVideo(playList.getIdolUid(), video.getVideoUid());} catch(Exception e) {}
        });
    }

    @Override
    public void collectKaraokeVideoInfos() {
        List<YouTube.Video> videos = youTubeRepository.findAllKaraokeVideo();
        videos.stream()
        .forEach(video -> taskExecutor.execute(() -> collectKaraokeVideoInfos(video)));
    }

    private void collectKaraokeVideoInfos(YouTube.Video video) {
        if (video != null) {
            try {
                com.google.api.services.youtube.YouTube.Videos.List list = buildYouTube().videos().list("snippet, player, contentDetails, statistics");
                if(list != null) {
                    list.setKey(apiKey);
                    list.setId(video.getVideoId());

                    VideoListResponse response = list.execute();
                    youTubeRepository.updateKaraokeVideo(populate(video, response.getItems().get(0)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void collectNewKaraokeVideos() {
        final Set<String> newIdSet = Collections.synchronizedSet(new HashSet<>());
        final Set<String> existIdSet = youTubeRepository.findAllKaraokeVideoIds();
        List<Idol> idols = idolRepository.findAll();
        Collections.sort(idols, (e1, e2) -> Long.compare(e1.getIdolUid(), e2.getIdolUid()));

        for(Idol idol : idols) {
            taskExecutor.execute(() -> {
                youTubeRepository.findKaraokePlayListByIdolUid(idol.getIdolUid()).forEach(playList -> {
                    Collection<String> set = collectNewVideoIds(existIdSet, playList.getPlayListId());
                    persistKaraokeVideoIds(playList, set);
                    newIdSet.addAll(set);
                });
            });
        }
        waitUntilDone();

        newIdSet.forEach(videoId -> taskExecutor.execute(() -> collectKaraokeVideoInfos(youTubeRepository.findKaraokeVideoByVideoId(videoId))));
        waitUntilDone();
    }

    @Override
    public void updateKaraokeViewCount() {
        Calendar calendar = Calendar.getInstance(Locale.KOREA);
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        final Date date = calendar.getTime();
        final int dateIdx = Integer.parseInt(fdf.format(date));

        Page page = new Page();
        page.setScale(50);
        page.setTotal(youTubeRepository.countKaraokeVideosByPage(page));
        int pages = page.getTotalPages() + 1;
        for(int i = 1; i <= pages; i++) {
            page.setPage(i);
            List<YouTube.Video> videos = youTubeRepository.findKaraokeVideosByPage(page);
            Map<String, YouTube.Video> videoMap = videos.stream().collect(Collectors.toMap(YouTube.Video::getVideoId, Function.identity(), (video1, video2) -> video1));
            List<YouTube.Video.ViewCount> vcList = new ArrayList<>();
            try {
                com.google.api.services.youtube.YouTube.Videos.List list = buildYouTube().videos().list("id, statistics");
                list.setKey(apiKey);
                list.setId(String.join(",", videoMap.keySet()));
                list.setMaxResults(50L);

                List<Video> items = list.execute().getItems();
                items.stream().filter(item -> item.getStatistics() != null).forEach(item -> {
                    YouTube.Video video = videoMap.remove(item.getId());
                    VideoStatistics statistics = item.getStatistics();

                    long totalViewCount = statistics.getViewCount().longValue();
                    long viewCount = totalViewCount - video.getViewCount();
                    if(viewCount > 0) {
                        vcList.add(new YouTube.Video.ViewCount(video.getVideoUid(), totalViewCount, viewCount, date, dateIdx));
                    }
                });
            } catch (Exception e) {
                videoMap.clear();
                e.printStackTrace();
            }

            for (YouTube.Video.ViewCount vc : vcList) {
                youTubeRepository.updateKaraokeVideoViewCountByVideoUid(vc.getTotalViewCount(), vc.getVideoUid());
                youTubeRepository.insertKaraokeVideoViewCount(vc);
            }
            // videoMap.values().forEach(video -> youTubeRepository.deleteKaraokeVideoByUid(video.getVideoUid()));
        }
    }

//    @Override
//    public void updateKaraokeViewCount() {
//        Calendar calendar = Calendar.getInstance(Locale.KOREA);
//        calendar.add(Calendar.DAY_OF_MONTH, -1);
//
//        final Date date = calendar.getTime();
//        final int dateIdx = Integer.parseInt(fdf.format(date));
//
//        Page page = new Page();
//        page.setScale(300);
//        page.setTotal(youTubeRepository.countKaraokeVideosByPage(page));
//        int pages = page.getTotalPages() + 1;
//        for(int i = 1; i <= pages; i++) {
//            page.setPage(i);
//            for(YouTube.Video video : youTubeRepository.findKaraokeVideosByPage(page)) {
//                taskExecutor.execute(() -> {
//                    try {
//                        com.google.api.services.youtube.YouTube.Videos.List list = buildYouTube().videos().list("statistics");
//                        list.setKey(apiKey);
//                        list.setId(video.getVideoId());
//
//                        List<Video> items = list.execute().getItems();
//                        if(items.size() > 0) {
//                            Video item = items.get(0);
//                            VideoStatistics statistics = item.getStatistics();
//                            if(statistics != null) {
//                                long totalViewCount = statistics.getViewCount().longValue();
//                                long viewCount = totalViewCount - video.getViewCount();
//                                if(viewCount > 0) {
//                                    youTubeRepository.updateKaraokeVideoViewCountByVideoUid(totalViewCount, video.getVideoUid());
//                                    youTubeRepository.insertKaraokeVideoViewCount(new YouTube.Video.ViewCount(video.getVideoUid(), totalViewCount, viewCount, date, dateIdx));
//                                }
//                            }
//                        } else {
//                            youTubeRepository.deleteKaraokeVideoByUid(video.getVideoUid());
//                        }
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                });
//            }
//        }
//    }
}
