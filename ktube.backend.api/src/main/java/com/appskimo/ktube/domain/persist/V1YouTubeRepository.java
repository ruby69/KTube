package com.appskimo.ktube.domain.persist;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.appskimo.ktube.domain.model.Page;
import com.appskimo.ktube.domain.model.YouTube;

@Mapper
public interface V1YouTubeRepository {

    // Video ========================================================================================================================

    YouTube.V1Video findVideoByUid(Long videoUid);

    @Select("SELECT COUNT(1) FROM `IdolVideo` WHERE `idolUid` = #{p.idolUid}")
    int countIdolVideosByPage(Page page);

    List<YouTube.V1Video> findIdolVideosByPage(Page page);

    @Select("SELECT COUNT(1) FROM `Video` WHERE `viewCount` < 50000000 ")
    int countVideosByPage(Page page);

    List<YouTube.V1Video> findVideosByPage(Page page);

    @Select("SELECT COUNT(1) FROM ${p.tableName} vr INNER JOIN `Video` v ON vr.`videoUid` = v.`videoUid` INNER JOIN `IdolVideo` iv ON vr.`videoUid` = iv.`videoUid` WHERE vr.`dateIdx` = #{p.dateIdx} AND v.`viewCount` < 50000001")
    int countBestsByPage(Page page);

    List<YouTube.V1Video> findBestsByPage(Page page);

    int countLastsByPage(Page page);

    List<YouTube.V1Video> findLastsByPage(Page page);



    @Select("SELECT COUNT(1) FROM `Video` WHERE `viewCount` > 49999999")
    int countOver50000000ByPage(Page page);

    @Select("SELECT v.*, iv.`idolUid` AS `idolUid` FROM ( SELECT v.`videoUid` FROM `Video` v WHERE v.`viewCount` > 49999999 ORDER BY v.`viewCount` DESC ) a INNER JOIN `Video` v ON a.`videoUid` = v.`videoUid` INNER JOIN `IdolVideo` iv ON a.`videoUid` = iv.`videoUid` ORDER BY v.`viewCount` DESC LIMIT #{beginIndex}, #{scale}")
    List<YouTube.V1Video> findOver50000000ByPage(Page page);





    @Select("SELECT COUNT(DISTINCT vr.`videoUid`) FROM `VideoRankDaily` vr WHERE vr.`dateIdx` >= #{p.fromIdx} and vr.`dateIdx` <= #{p.toIdx}")
    int countSampleMonthly(Page page);

    @Select("SELECT v.*, SUM(vr.`viewCount`) AS `rankViewCount`, iv.`idolUid` AS `idolUid` FROM `VideoRankDaily` vr INNER JOIN `Video` v ON vr.`videoUid` = v.`videoUid` INNER JOIN `IdolVideo` iv ON vr.`videoUid` = iv.`videoUid` WHERE vr.`dateIdx` >= #{p.fromIdx} and vr.`dateIdx` <= #{p.toIdx} GROUP BY vr.`videoUid` ORDER BY `rankViewCount` DESC LIMIT #{beginIndex}, #{scale}")
    List<YouTube.V1Video> findSampleMonthly(Page page);



    // ShowVideo ========================================================================================================================
    YouTube.V1Video findShowVideoByUid(Long videoUid);

    @Select("SELECT COUNT(1) FROM `ShowVideo` WHERE `showChannelUid` = #{p.showChannel}")
    int countShowVideosByPage(Page page);

    List<YouTube.V1Video> findShowVideosByPage(Page page);




    // FancamVideo ========================================================================================================================
    YouTube.V1Video findFancamVideoByUid(Long videoUid);

    @Select("SELECT COUNT(1) FROM `FancamVideo`")
    int countFancamVideosByPage(Page page);

    List<YouTube.V1Video> findFancamVideosByPage(Page page);

    @Select("SELECT COUNT(1) FROM ${p.tableName} WHERE `dateIdx` = #{p.dateIdx}")
    int countFancamBestsByPage(Page page);

    List<YouTube.V1Video> findFancamBestsByPage(Page page);

    @Select("SELECT COUNT(DISTINCT vr.`videoUid`) FROM `FancamVideoRankDaily` vr WHERE vr.`dateIdx` > #{p.dateIdx}")
    int countFancamLastsByPage(Page page);

    List<YouTube.V1Video> findFancamLastsByPage(Page page);




    // LyricsVideo ========================================================================================================================
    YouTube.V1Video findLyricsVideoByUid(Long videoUid);

    @Select("SELECT COUNT(1) FROM `LyricsVideo`")
    int countLyricsVideosByPage(Page page);

    List<YouTube.V1Video> findLyricsVideosByPage(Page page);




    // KaraokeVideo ========================================================================================================================
    YouTube.V1Video findKaraokeVideoByUid(Long videoUid);

    @Select("SELECT COUNT(1) FROM `IdolKaraokeVideo` WHERE `idolUid` = #{p.idolUid}")
    int countIdolKaraokeVideosByPage(Page page);

    List<YouTube.V1Video> findIdolKaraokeVideosByPage(Page page);

    @Select("SELECT COUNT(1) FROM `KaraokeVideo`")
    int countKaraokeVideosByPage(Page page);

    List<YouTube.V1Video> findKaraokeVideosByPage(Page page);

    @Select("SELECT COUNT(1) FROM ${p.tableName} WHERE `dateIdx` = #{p.dateIdx}")
    int countKaraokeBestsByPage(Page page);

    List<YouTube.V1Video> findKaraokeBestsByPage(Page page);





    @Select("SELECT COUNT(1) FROM ${p.tableName} WHERE `title` LIKE CONCAT('%', #{p.q}, '%')")
    int countLikeTitle(Page page);

    @Select("SELECT v.* FROM ( SELECT v.`videoUid` FROM ${p.tableName} v WHERE v.`title` LIKE CONCAT('%', #{p.q}, '%') ORDER BY v.`viewCount` DESC ) a INNER JOIN ${p.tableName} v ON a.`videoUid` = v.`videoUid` ORDER BY v.`viewCount` DESC LIMIT #{beginIndex}, #{scale}")
    List<YouTube.V1Video> findLikeTitle(Page page);

}
