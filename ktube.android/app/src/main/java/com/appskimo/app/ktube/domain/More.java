package com.appskimo.app.ktube.domain;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class More implements Serializable {
    private static final long serialVersionUID = 6155172233771388941L;

    private Long scale = 20L;
    private List<? extends YoutubeVideo> content;
    private boolean hasMore;

    public Long getLastSeq() {
        if(hasContents()) {
            return content.get(content.size() - 1).getSeq();
        }
        return null;
    }

    private boolean hasContents() {
        return content != null && !content.isEmpty();
    }
}
