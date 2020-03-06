package com.appskimo.ktube.domain.persist;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.appskimo.ktube.domain.model.Page;
import com.appskimo.ktube.domain.model.YouTube;

@Mapper
public interface V2YouTubeRepository {

    @Select("SELECT COUNT(1) FROM `Video`")
    int countVideosByPage(Page page);

    List<YouTube.V2Video> findVideosByPage(Page page);

    int countMillionVideosByPage(Page page);

    List<YouTube.V2Video> findMillionVideosByPage(Page page);

    @Select("SELECT COUNT(1) FROM `IdolVideo` WHERE `idolUid` = #{p.idolUid}")
    int countIdolVideosByPage(Page page);

    List<YouTube.V2Video> findIdolVideosByPage(Page page);

    @Select("SELECT COUNT(1) FROM (SELECT DISTINCT vr.`videoUid` FROM `VideoRankDaily` vr WHERE vr.`dateIdx` > (SELECT DISTINCT `dateIdx` FROM `VideoRankDaily` ORDER BY `dateIdx` DESC LIMIT #{p.day}, 1) ) a")
    int countLastNDaysBestVideosByPage(Page page);

    List<YouTube.V2Video> findLastNDaysBestVideosByPage(Page page);

    @Select("SELECT COUNT(1) FROM `FancamVideo`")
    int countFancamVideosByPage(Page page);

    List<YouTube.V2Video> findFancamVideosByPage(Page page);

    @Select("SELECT COUNT(1) FROM ( SELECT DISTINCT vr.`videoUid` FROM `FancamVideoRankDaily` vr WHERE vr.`dateIdx` > (SELECT DISTINCT `dateIdx` FROM `FancamVideoRankDaily` ORDER BY `dateIdx` DESC LIMIT #{p.day}, 1) ) a")
    int countLastNDaysBestFancamsByPage(Page page);

    List<YouTube.V2Video> findLastNDaysBestFancamsByPage(Page page);

    @Select("SELECT COUNT(1) FROM `KaraokeVideo`")
    int countKaraokeVideosByPage(Page page);

    List<YouTube.V2Video> findKaraokeVideosByPage(Page page);

    @Select("SELECT COUNT(1) FROM `IdolKaraokeVideo` WHERE `idolUid` = #{p.idolUid}")
    int countIdolKaraokeVideosByPage(Page page);

    List<YouTube.V2Video> findIdolKaraokeVideosByPage(Page page);

    @Select("SELECT COUNT(1) FROM ( SELECT DISTINCT vr.`videoUid` FROM `KaraokeVideoRankDaily` vr WHERE vr.`dateIdx` > (SELECT DISTINCT `dateIdx` FROM `KaraokeVideoRankDaily` ORDER BY `dateIdx` DESC LIMIT #{p.day}, 1) ) a")
    int countLastNDaysBestKaraokesByPage(Page page);

    List<YouTube.V2Video> findLastNDaysBestKaraokesByPage(Page page);

    @Select("SELECT COUNT(1) FROM `LyricsVideo`")
    int countLyricsVideosByPage(Page page);

    List<YouTube.V2Video> findLyricsVideosByPage(Page page);

    @Select("SELECT COUNT(1) FROM ${p.tableName} WHERE `title` LIKE CONCAT('%', #{p.q}, '%')")
    int countLikeTitle(Page page);

    @Select("SELECT v.* FROM ( SELECT v.`videoUid` FROM ${p.tableName} v WHERE v.`title` LIKE CONCAT('%', #{p.q}, '%') ORDER BY v.`viewCount` DESC ) a INNER JOIN ${p.tableName} v ON a.`videoUid` = v.`videoUid` ORDER BY v.`viewCount` DESC LIMIT #{beginIndex}, #{scale}")
    List<YouTube.V2Video> findLikeTitle(Page page);
}


