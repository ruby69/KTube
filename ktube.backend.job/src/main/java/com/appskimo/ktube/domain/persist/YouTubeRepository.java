package com.appskimo.ktube.domain.persist;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.appskimo.ktube.domain.model.Page;
import com.appskimo.ktube.domain.model.YouTube;

@Mapper
public interface YouTubeRepository {

    // Video ========================================================================================================================

    @Insert("INSERT INTO `Video` (`videoId`) VALUES (#{videoId})")
    @Options(useGeneratedKeys = true, keyProperty = "videoUid", keyColumn = "videoUid")
    void insertVideo(YouTube.Video video);

    @Select("SELECT * FROM `Video` WHERE `videoId` = #{videoId}")
    YouTube.Video findVideoByVideoId(String videoId);

    @Select("SELECT * FROM `Video`")
    List<YouTube.Video> findAllVideo();

    @Update("UPDATE `Video` SET `title` = #{title}, `description` = #{description}, `definition` = #{definition}, `duration` = #{duration}, `embedHtml` = #{embedHtml}, `publishedAt` = #{publishedAt}, `publishedAtIdx` = #{publishedAtIdx}, `viewCount` = #{viewCount}, `thumbnailDefault` = #{thumbnailDefault}, `thumbnailMedium` = #{thumbnailMedium}, `thumbnailHigh` = #{thumbnailHigh}, `thumbnailStandard` = #{thumbnailStandard}, `thumbnailMaxres` = #{thumbnailMaxres} where `videoUid` = #{videoUid}")
    void updateVideo(YouTube.Video video);

    @Update("UPDATE `Video` SET `viewCount` = #{viewCount} where `videoUid` = #{videoUid}")
    void updateVideoViewCountByVideoUid(@Param("viewCount") long viewCount, @Param("videoUid") Long videoUid);

    @Select("SELECT COUNT(1) FROM `Video`")
    int countVideosByPage(Page page);

    List<YouTube.Video> findVideosByPage(Page page);

    @Select("SELECT distinct `videoId` FROM `Video`")
    Set<String> findAllVideoIds();

    @Delete("DELETE FROM `Video` WHERE `title` IS NULL")
    void deleteVideoByTitleIsNull();

    @Delete("DELETE FROM `Video` WHERE `videoUid` = #{videoUid}")
    void deleteVideoByUid(Long videoUid);







    // PlayList ========================================================================================================================
    @Select("SELECT * FROM `PlayList` WHERE `idolUid` = #{idolUid}")
    List<YouTube.PlayList> findPlayListByIdolUid(Long idolUid);

    // IdolVideo ========================================================================================================================
    @Insert("INSERT INTO `IdolVideo` (`idolUid`,`videoUid`) VALUES (#{idolUid},#{videoUid})")
    void insertIdolVideo(@Param("idolUid") Long idolUid, @Param("videoUid") Long videoUid);

    // PlayListVideo ========================================================================================================================
    @Insert("INSERT INTO `PlayListVideo` (`playListUid`,`videoUid`) VALUES (#{playListUid},#{videoUid})")
    void insertPlayListVideo(@Param("playListUid") Long playListUid, @Param("videoUid") Long videoUid);

    // VideoViewCount ========================================================================================================================
    @Insert("INSERT INTO `VideoViewCount` (`videoUid`,`totalViewCount`,`viewCount`,`date`,`dateIdx`) VALUES (#{videoUid},#{totalViewCount},#{viewCount},#{date},#{dateIdx})")
    void insertVideoViewCount(YouTube.Video.ViewCount viewCount);

    @Delete("DELETE FROM `VideoViewCount` WHERE `dateIdx` = #{dateIdx}")
    void deleteVideoViewCount(int dateIdx);

    // VideoRankDaily ========================================================================================================================
    @Insert("INSERT INTO `VideoRankDaily` (`videoUid`, `viewCount`, `dateIdx`) SELECT `videoUid`, SUM(`viewCount`) AS `viewCount`, `dateIdx` FROM `VideoViewCount` WHERE `dateIdx` = #{dateIdx} GROUP BY `videoUid` ORDER BY `viewCount` DESC LIMIT 1000")
    void insertVideoRankDaily(int dateIdx);

    @Delete("DELETE FROM `VideoRankDaily` WHERE `dateIdx` = #{dateIdx}")
    void deleteVideoRankDaily(int dateIdx);

    // VideoRankWeekly ========================================================================================================================
    @Insert("INSERT INTO `VideoRankWeekly` (`videoUid`, `viewCount`, `dateIdx`) SELECT `videoUid`, SUM(`viewCount`) AS `viewCount`, #{fromIdx} AS `dateIdx` FROM `VideoRankDaily` WHERE `dateIdx` >= #{fromIdx} AND `dateIdx` <= #{toIdx} GROUP BY `videoUid` ORDER BY `viewCount` DESC")
    void insertVideoRankWeekly(@Param("fromIdx") int fromIdx, @Param("toIdx") int toIdx);

    @Delete("DELETE FROM `VideoRankWeekly` WHERE `dateIdx` = #{dateIdx}")
    void deleteVideoRankWeekly(int dateIdx);

    // VideoRankMonthly ========================================================================================================================
    @Insert("INSERT INTO `VideoRankMonthly` (`videoUid`, `viewCount`, `dateIdx`) SELECT `videoUid`, SUM(`viewCount`) AS `viewCount`, #{monthIdx} AS `dateIdx` FROM `VideoRankDaily` WHERE `dateIdx` >= #{fromIdx} AND `dateIdx` <= #{toIdx} GROUP BY `videoUid` ORDER BY `viewCount` DESC")
    void insertVideoRankMonthly(@Param("fromIdx") int fromIdx, @Param("toIdx") int toIdx, @Param("monthIdx") int monthIdx);

    @Delete("DELETE FROM `VideoRankMonthly` WHERE `dateIdx` = #{dateIdx}")
    void deleteVideoRankMonthly(int dateIdx);





    // ShowChannel ========================================================================================================================
    @Select("SELECT * FROM `ShowChannel`")
    List<YouTube.ShowChannel> findAllShowChannel();

    // ShowVideo ========================================================================================================================
    @Insert("INSERT INTO `ShowVideo` (`videoId`, `showChannelUid`) VALUES (#{videoId}, #{showChannelUid})")
    @Options(useGeneratedKeys = true, keyProperty = "videoUid", keyColumn = "videoUid")
    void insertShowVideo(YouTube.Video video);

    @Select("SELECT * FROM `ShowVideo` WHERE `videoId` = #{videoId}")
    YouTube.Video findShowVideoByVideoId(String videoId);

    @Select("SELECT * FROM `ShowVideo`")
    List<YouTube.Video> findAllShowVideo();

    @Update("UPDATE `ShowVideo` SET `title` = #{title}, `description` = #{description}, `definition` = #{definition}, `duration` = #{duration}, `embedHtml` = #{embedHtml}, `publishedAt` = #{publishedAt}, `publishedAtIdx` = #{publishedAtIdx}, `viewCount` = #{viewCount}, `thumbnailDefault` = #{thumbnailDefault}, `thumbnailMedium` = #{thumbnailMedium}, `thumbnailHigh` = #{thumbnailHigh}, `thumbnailStandard` = #{thumbnailStandard}, `thumbnailMaxres` = #{thumbnailMaxres} where `videoUid` = #{videoUid}")
    void updateShowVideo(YouTube.Video video);

    @Select("SELECT distinct `videoId` FROM `ShowVideo`")
    Set<String> findAllShowVideoIds();

    @Delete("DELETE FROM `ShowVideo` WHERE `title` IS NULL")
    void deleteShowVideoByTitleIsNull();



    // FancamChannel ========================================================================================================================
    @Select("SELECT * FROM `FancamChannel`")
    List<YouTube.FancamChannel> findAllFancamChannel();

    // FancamVideo ========================================================================================================================
    @Insert("INSERT INTO `FancamVideo` (`videoId`, `fancamChannelUid`) VALUES (#{videoId}, #{fancamChannelUid})")
    @Options(useGeneratedKeys = true, keyProperty = "videoUid", keyColumn = "videoUid")
    void insertFancamVideo(YouTube.Video video);

    @Select("SELECT * FROM `FancamVideo` WHERE `videoId` = #{videoId}")
    YouTube.Video findFancamVideoByVideoId(String videoId);

    @Select("SELECT * FROM `FancamVideo`")
    List<YouTube.Video> findAllFancamVideo();

    @Update("UPDATE `FancamVideo` SET `title` = #{title}, `description` = #{description}, `definition` = #{definition}, `duration` = #{duration}, `embedHtml` = #{embedHtml}, `publishedAt` = #{publishedAt}, `publishedAtIdx` = #{publishedAtIdx}, `viewCount` = #{viewCount}, `thumbnailDefault` = #{thumbnailDefault}, `thumbnailMedium` = #{thumbnailMedium}, `thumbnailHigh` = #{thumbnailHigh}, `thumbnailStandard` = #{thumbnailStandard}, `thumbnailMaxres` = #{thumbnailMaxres} where `videoUid` = #{videoUid}")
    void updateFancamVideo(YouTube.Video video);

    @Update("UPDATE `FancamVideo` SET `viewCount` = #{viewCount} where `videoUid` = #{videoUid}")
    void updateFancamVideoViewCountByVideoUid(@Param("viewCount") long viewCount, @Param("videoUid") Long videoUid);

    @Select("SELECT COUNT(1) FROM `FancamVideo`")
    int countFancamVideosByPage(Page page);

    List<YouTube.Video> findFancamVideosByPage(Page page);

    @Select("SELECT distinct `videoId` FROM `FancamVideo`")
    Set<String> findAllFancamVideoIds();

    @Delete("DELETE FROM `FancamVideo` WHERE `title` IS NULL")
    void deleteFancamVideoByTitleIsNull();

    @Delete("DELETE FROM `FancamVideo` WHERE `videoUid` = #{videoUid}")
    void deleteFancamVideoByUid(Long videoUid);



    // FancamVideoViewCount ========================================================================================================================
    @Insert("INSERT INTO `FancamVideoViewCount` (`videoUid`,`totalViewCount`,`viewCount`,`date`,`dateIdx`) VALUES (#{videoUid},#{totalViewCount},#{viewCount},#{date},#{dateIdx})")
    void insertFancamVideoViewCount(YouTube.Video.ViewCount viewCount);

    @Delete("DELETE FROM `FancamVideoViewCount` WHERE `dateIdx` = #{dateIdx}")
    void deleteFancamVideoViewCount(int dateIdx);

    // FancamVideoRankDaily ========================================================================================================================
    @Insert("INSERT INTO `FancamVideoRankDaily` (`videoUid`, `viewCount`, `dateIdx`) SELECT `videoUid`, SUM(`viewCount`) AS `viewCount`, `dateIdx` FROM `FancamVideoViewCount` WHERE `dateIdx` = #{dateIdx} GROUP BY `videoUid` ORDER BY `viewCount` DESC LIMIT 1000")
    void insertFancamVideoRankDaily(int dateIdx);

    @Delete("DELETE FROM `FancamVideoRankDaily` WHERE `dateIdx` = #{dateIdx}")
    void deleteFancamVideoRankDaily(int dateIdx);

    // FancamVideoRankWeekly ========================================================================================================================
    @Insert("INSERT INTO `FancamVideoRankWeekly` (`videoUid`, `viewCount`, `dateIdx`) SELECT `videoUid`, SUM(`viewCount`) AS `viewCount`, #{fromIdx} AS `dateIdx` FROM `FancamVideoRankDaily` WHERE `dateIdx` >= #{fromIdx} AND `dateIdx` <= #{toIdx} GROUP BY `videoUid` ORDER BY `viewCount` DESC")
    void insertFancamVideoRankWeekly(@Param("fromIdx") int fromIdx, @Param("toIdx") int toIdx);

    @Delete("DELETE FROM `FancamVideoRankWeekly` WHERE `dateIdx` = #{dateIdx}")
    void deleteFancamVideoRankWeekly(int dateIdx);

    // FancamVideoRankMonthly ========================================================================================================================
    @Insert("INSERT INTO `FancamVideoRankMonthly` (`videoUid`, `viewCount`, `dateIdx`) SELECT `videoUid`, SUM(`viewCount`) AS `viewCount`, #{monthIdx} AS `dateIdx` FROM `FancamVideoRankDaily` WHERE `dateIdx` >= #{fromIdx} AND `dateIdx` <= #{toIdx} GROUP BY `videoUid` ORDER BY `viewCount` DESC")
    void insertFancamVideoRankMonthly(@Param("fromIdx") int fromIdx, @Param("toIdx") int toIdx, @Param("monthIdx") int monthIdx);

    @Delete("DELETE FROM `FancamVideoRankMonthly` WHERE `dateIdx` = #{dateIdx}")
    void deleteFancamVideoRankMonthly(int dateIdx);





    // LyricsChannel ========================================================================================================================
    @Select("SELECT * FROM `LyricsChannel`")
    List<YouTube.LyricsChannel> findAllLyricsChannel();

    @Update("UPDATE `LyricsChannel` SET `uploadsPlaylistId` = #{uploadsPlaylistId} where `lyricsChannelUid` = #{lyricsChannelUid}")
    void updateLyricsUploadsPlaylistId(@Param("uploadsPlaylistId") String uploadsPlaylistId, @Param("lyricsChannelUid") Long lyricsChannelUid);

    // LyricsVideo ========================================================================================================================
    @Insert("INSERT INTO `LyricsVideo` (`videoId`, `lyricsChannelUid`) VALUES (#{videoId}, #{lyricsChannelUid})")
    @Options(useGeneratedKeys = true, keyProperty = "videoUid", keyColumn = "videoUid")
    void insertLyricsVideo(YouTube.Video video);

    @Select("SELECT * FROM `LyricsVideo` WHERE `videoId` = #{videoId}")
    YouTube.Video findLyricsVideoByVideoId(String videoId);

    @Select("SELECT * FROM `LyricsVideo`")
    List<YouTube.Video> findAllLyricsVideo();

    @Update("UPDATE `LyricsVideo` SET `title` = #{title}, `description` = #{description}, `definition` = #{definition}, `duration` = #{duration}, `embedHtml` = #{embedHtml}, `publishedAt` = #{publishedAt}, `publishedAtIdx` = #{publishedAtIdx}, `viewCount` = #{viewCount}, `thumbnailDefault` = #{thumbnailDefault}, `thumbnailMedium` = #{thumbnailMedium}, `thumbnailHigh` = #{thumbnailHigh}, `thumbnailStandard` = #{thumbnailStandard}, `thumbnailMaxres` = #{thumbnailMaxres} where `videoUid` = #{videoUid}")
    void updateLyricsVideo(YouTube.Video video);

    @Select("SELECT distinct `videoId` FROM `LyricsVideo`")
    Set<String> findAllLyricsVideoIds();

    @Delete("DELETE FROM `LyricsVideo` WHERE `videoUid` = #{videoUid}")
    void deleteLyricsVideoByUid(Long videoUid);




    // KaraokePlayList ========================================================================================================================
    @Select("SELECT * FROM `KaraokePlayList` WHERE `idolUid` = #{idolUid}")
    List<YouTube.PlayList> findKaraokePlayListByIdolUid(Long idolUid);

    // IdolKaraokeVideo ========================================================================================================================
    @Insert("INSERT INTO `IdolKaraokeVideo` (`idolUid`,`videoUid`) VALUES (#{idolUid},#{videoUid})")
    void insertIdolKaraokeVideo(@Param("idolUid") Long idolUid, @Param("videoUid") Long videoUid);

    // KaraokeVideo ========================================================================================================================
    @Insert("INSERT INTO `KaraokeVideo` (`videoId`) VALUES (#{videoId})")
    @Options(useGeneratedKeys = true, keyProperty = "videoUid", keyColumn = "videoUid")
    void insertKaraokeVideo(YouTube.Video video);

    @Select("SELECT * FROM `KaraokeVideo` WHERE `videoId` = #{videoId}")
    YouTube.Video findKaraokeVideoByVideoId(String videoId);

    @Select("SELECT * FROM `KaraokeVideo`")
    List<YouTube.Video> findAllKaraokeVideo();

    @Update("UPDATE `KaraokeVideo` SET `title` = #{title}, `description` = #{description}, `definition` = #{definition}, `duration` = #{duration}, `embedHtml` = #{embedHtml}, `publishedAt` = #{publishedAt}, `publishedAtIdx` = #{publishedAtIdx}, `viewCount` = #{viewCount}, `thumbnailDefault` = #{thumbnailDefault}, `thumbnailMedium` = #{thumbnailMedium}, `thumbnailHigh` = #{thumbnailHigh}, `thumbnailStandard` = #{thumbnailStandard}, `thumbnailMaxres` = #{thumbnailMaxres} where `videoUid` = #{videoUid}")
    void updateKaraokeVideo(YouTube.Video video);

    @Update("UPDATE `KaraokeVideo` SET `viewCount` = #{viewCount} where `videoUid` = #{videoUid}")
    void updateKaraokeVideoViewCountByVideoUid(@Param("viewCount") long viewCount, @Param("videoUid") Long videoUid);

    @Select("SELECT COUNT(1) FROM `KaraokeVideo`")
    int countKaraokeVideosByPage(Page page);

    List<YouTube.Video> findKaraokeVideosByPage(Page page);

    @Select("SELECT distinct `videoId` FROM `KaraokeVideo`")
    Set<String> findAllKaraokeVideoIds();

    @Delete("DELETE FROM `KaraokeVideo` WHERE `videoUid` = #{videoUid}")
    void deleteKaraokeVideoByUid(Long videoUid);


    // KaraokeVideoViewCount ========================================================================================================================
    @Insert("INSERT INTO `KaraokeVideoViewCount` (`videoUid`,`totalViewCount`,`viewCount`,`date`,`dateIdx`) VALUES (#{videoUid},#{totalViewCount},#{viewCount},#{date},#{dateIdx})")
    void insertKaraokeVideoViewCount(YouTube.Video.ViewCount viewCount);

    @Delete("DELETE FROM `KaraokeVideoViewCount` WHERE `dateIdx` = #{dateIdx}")
    void deleteKaraokeVideoViewCount(int dateIdx);

    // KaraokeVideoRankDaily ========================================================================================================================
    @Insert("INSERT INTO `KaraokeVideoRankDaily` (`videoUid`, `viewCount`, `dateIdx`) SELECT `videoUid`, SUM(`viewCount`) AS `viewCount`, `dateIdx` FROM `KaraokeVideoViewCount` WHERE `dateIdx` = #{dateIdx} GROUP BY `videoUid` ORDER BY `viewCount` DESC LIMIT 1000")
    void insertKaraokeVideoRankDaily(int dateIdx);

    @Delete("DELETE FROM `KaraokeVideoRankDaily` WHERE `dateIdx` = #{dateIdx}")
    void deleteKaraokeVideoRankDaily(int dateIdx);

    // KaraokeVideoRankWeekly ========================================================================================================================
    @Insert("INSERT INTO `KaraokeVideoRankWeekly` (`videoUid`, `viewCount`, `dateIdx`) SELECT `videoUid`, SUM(`viewCount`) AS `viewCount`, #{fromIdx} AS `dateIdx` FROM `KaraokeVideoRankDaily` WHERE `dateIdx` >= #{fromIdx} AND `dateIdx` <= #{toIdx} GROUP BY `videoUid` ORDER BY `viewCount` DESC")
    void insertKaraokeVideoRankWeekly(@Param("fromIdx") int fromIdx, @Param("toIdx") int toIdx);

    @Delete("DELETE FROM `KaraokeVideoRankWeekly` WHERE `dateIdx` = #{dateIdx}")
    void deleteKaraokeVideoRankWeekly(int dateIdx);

    // KaraokeVideoRankMonthly ========================================================================================================================
    @Insert("INSERT INTO `KaraokeVideoRankMonthly` (`videoUid`, `viewCount`, `dateIdx`) SELECT `videoUid`, SUM(`viewCount`) AS `viewCount`, #{monthIdx} AS `dateIdx` FROM `KaraokeVideoRankDaily` WHERE `dateIdx` >= #{fromIdx} AND `dateIdx` <= #{toIdx} GROUP BY `videoUid` ORDER BY `viewCount` DESC")
    void insertKaraokeVideoRankMonthly(@Param("fromIdx") int fromIdx, @Param("toIdx") int toIdx, @Param("monthIdx") int monthIdx);

    @Delete("DELETE FROM `KaraokeVideoRankMonthly` WHERE `dateIdx` = #{dateIdx}")
    void deleteKaraokeVideoRankMonthly(int dateIdx);



}
