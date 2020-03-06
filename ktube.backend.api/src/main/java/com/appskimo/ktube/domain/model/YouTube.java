package com.appskimo.ktube.domain.model;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.ToString;

public class YouTube {

    @Data
    @ToString(of = {"videoUid", "videoId"})
    @JsonInclude(Include.NON_NULL)
    public static class V1Video implements Serializable {
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

        public String getIdolKey() {
            return KeyEncryptor.getKey(KeyEncryptor.Target.IDOL, idolUid);
        }

        public void setIdolKey(String key) {
            Long uid = null;
            if (this.idolUid == null && (uid = KeyEncryptor.getUid(KeyEncryptor.Target.IDOL, key)) != null) {
                this.idolUid = uid;
            }
        }

        public String getVideoKey() {
            return KeyEncryptor.getKey(KeyEncryptor.Target.VIDEO, videoUid);
        }

        public void setVideoKey(String key) {
            Long uid = null;
            if (this.videoUid == null && (uid = KeyEncryptor.getUid(KeyEncryptor.Target.VIDEO, key)) != null) {
                this.videoUid = uid;
            }
        }
    }

    @Data
    @ToString(of = {"videoId", "title"})
    @JsonInclude(Include.NON_NULL)
    public static class V2Video implements Serializable {
        private static final long serialVersionUID = -7321946088822344653L;

        private String videoId;
        private String title;
        private Date publishedAt;
        private Long viewCount;
        private Long rankViewCount;

        public long getPublishedAt() {
            return publishedAt.getTime();
        }
    }
}
