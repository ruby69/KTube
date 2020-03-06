package com.appskimo.ktube.domain.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.ToString;

public class Naver {

    @Data
    @ToString(of = {"total", "start"})
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Image implements Serializable {
        private static final long serialVersionUID = 7557738626767891791L;

        private int total;
        private int start;
        private int display;
        private List<Item> items;

        @Data
        @ToString(of = {"title", "link"})
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Item implements Serializable {
            private static final long serialVersionUID = 4768549895529985473L;

            private String title;
            private String link;
            private String thumbnail;
            private int sizeheight;
            private int sizewidth;

            public IdolImage toIdolImage(Long idolUid) {
                return new IdolImage(idolUid, link, link, sizewidth, sizeheight, thumbnail, 0, 0);
            }
        }
    }
}
