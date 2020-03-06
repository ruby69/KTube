package com.appskimo.ktube.domain.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

public class YouTube {

    @Data
    @ToString(of = {"videoUid", "videoId"})
    @JsonInclude(Include.NON_NULL)
    public static class Video implements Serializable {
        private static final long serialVersionUID = -367147674937618526L;

        @JsonIgnore private Long videoUid;
        @JsonIgnore private Long fancamChannelUid;
        @JsonIgnore private Long showChannelUid;
        @JsonIgnore private Long lyricsChannelUid;
        private String videoId;
        private String title;
        private String description;
        @JsonIgnore private String definition;
        private long duration;
        private String embedHtml;
        private Date publishedAt;
        @JsonIgnore private long publishedAtIdx;
        private long viewCount;
        private long likeCount;
        private String thumbnailDefault;
        private String thumbnailMedium;
        private String thumbnailHigh;
        private String thumbnailStandard;
        private String thumbnailMaxres;

        @JsonIgnore private Long idolUid;
        private long rankViewCount;

        @Data
        @ToString(of = {"rankUid", "videoUid", "viewCount"})
        @JsonInclude(Include.NON_NULL)
        public static class Rank implements Serializable {
            private static final long serialVersionUID = -7307625783729070005L;

            private Long rankUid;
            @JsonIgnore private Long videoUid;
            private long viewCount;
            private int dateIdx;
        }

        @Data
        @ToString(of = {"videoUid", "totalViewCount"})
        @JsonInclude(Include.NON_NULL)
        @NoArgsConstructor
        public static class ViewCount implements Serializable {
            private static final long serialVersionUID = 1664720107667975952L;

            private Long videoViewCountUid;
            private Long videoUid;
            private long totalViewCount;
            private long viewCount;
            private Date date;
            private int dateIdx;

            public ViewCount(Long videoUid, long totalViewCount, long viewCount, Date date, int dateIdx) {
                this.videoUid = videoUid;
                this.totalViewCount = totalViewCount;
                this.viewCount = viewCount;
                this.date = date;
                this.dateIdx = dateIdx;
            }
        }
    }

    @Data
    @ToString(of = {"playListUid", "idolUid", "playListId"})
    @JsonInclude(Include.NON_NULL)
    public static class PlayList implements Serializable {
        private static final long serialVersionUID = -7100116050831579332L;

        private Long playListUid;
        private Long idolUid;
        private String playListId;

        private List<Video> videos;
    }

    @Data
    @ToString(of = {"fancamChannelUid", "title"})
    @JsonInclude(Include.NON_NULL)
    public static class FancamChannel implements Serializable {
        private static final long serialVersionUID = -5018441616423185320L;

        public static enum Type {
            ID, USER
        }

        private Long fancamChannelUid;
        private String title;
        private Type type;
        private String value;
        private String uploadsPlaylistId;
        private Date registTime;
    }

    @Data
    @ToString(of = {"showChannelUid", "title"})
    @JsonInclude(Include.NON_NULL)
    public static class ShowChannel implements Serializable {
        private static final long serialVersionUID = -1028270208687066661L;

        public static enum Type {
            CH, PL
        }

        private Long showChannelUid;
        private String title;
        private Type type;
        private String value;
        private Date registTime;
    }

    @Data
    @ToString(of = {"lyricsChannelUid", "title"})
    @JsonInclude(Include.NON_NULL)
    public static class LyricsChannel implements Serializable {
        private static final long serialVersionUID = 4185373578086637197L;

        public static enum Type {
            ID, USER
        }

        private Long lyricsChannelUid;
        private String title;
        private Type type;
        private String value;
        private String uploadsPlaylistId;
        private String filteringKeywords;
        private boolean scheduled;
        private Date registTime;
    }

}
