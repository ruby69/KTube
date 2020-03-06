package com.appskimo.app.ktube.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(of = {"idolKey", "name1", "name2"})
@JsonIgnoreProperties(ignoreUnknown = true)
@DatabaseTable(tableName = "FavoriteIdol")
public class Artist implements Serializable {
    private static final long serialVersionUID = -7289112582024250448L;
    public static final String FIELD_favoriteSeq = "favoriteSeq";
    public static final String FIELD_name2 = "name2";

    @DatabaseField(columnName = FIELD_favoriteSeq, generatedId = true) protected Long favoriteSeq;
    @DatabaseField private String idolKey;
    @DatabaseField private String tags;

    @DatabaseField private String imageUrl;
    @DatabaseField private String thumbUrl;
    @DatabaseField private String name1;
    @DatabaseField private String url1;
    @DatabaseField private String summary1;
    @DatabaseField private String name2;
    @DatabaseField private String url2;
    @DatabaseField private String summary2;
    private int likeCount;

    public String getArtistKey() {
        return idolKey;
    }

    public static class Collection extends ArrayList<Artist> {
        private static final long serialVersionUID = 1988697232013836919L;
    }
}
