package com.appskimo.ktube.domain.persist;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.appskimo.ktube.domain.model.Idol;

@Mapper
public interface IdolRepository {

    @Select("SELECT i.*, if1.`name` AS `name1`, if1.`url` AS `url1`, if1.`summary` AS `summary1`, if2.`name` AS `name2`, if2.`url` AS `url2`, if2.`summary` AS `summary2` FROM `Idol` i LEFT JOIN `IdolInfo` if1 ON i.`idolUid` = if1.`idolUid` AND if1.`lang` = #{lang} LEFT JOIN `IdolInfo` if2 ON i.`idolUid` = if2.`idolUid` AND if2.`lang` = 'en' WHERE i.`idolUid` = #{idolUid}")
    Idol findByUid(@Param("idolUid") Long idolUid, @Param("lang") String lang);

    @Select("SELECT i.*, ii.url AS `imageUrl`, ii.thumbUrl AS `thumbUrl`, if1.`name` AS `name1`, if2.`name` AS `name2` FROM `Idol` i LEFT JOIN `IdolImage` ii ON i.`idolUid` = ii.`idolUid` AND ii.`mainImage` = 1 LEFT JOIN `IdolInfo` if1 ON i.`idolUid` = if1.`idolUid` AND if1.`lang` = #{lang} LEFT JOIN `IdolInfo` if2 ON i.`idolUid` = if2.`idolUid` AND if2.`lang` = 'en' ORDER BY if2.`name` ASC")
    List<Idol> findByLang(String lang);

    @Select("SELECT " +
            "  i.*, " +
            "  ii.url AS `imageUrl`, " +
            "  ii.thumbUrl AS `thumbUrl`, " +
            "  if1.`name` AS `name1`, " +
            "  if2.`name` AS `name2` " +
            "FROM ( " +
            "  SELECT " +
            "    DISTINCT iv.`idolUid` AS `idolUid` " +
            "  FROM " +
            "    ( " +
            "      SELECT " +
            "        vr.`videoUid`, " +
            "        SUM(vr.`viewCount`) AS `rankViewCount` " +
            "      FROM " +
            "        `VideoRankDaily` vr " +
            "      WHERE " +
            "        vr.`dateIdx` > (SELECT DISTINCT `dateIdx` FROM `VideoRankDaily` ORDER BY `dateIdx` DESC LIMIT 1, 1) " +
            "      GROUP BY vr.`videoUid` " +
            "      ORDER BY `rankViewCount` DESC " +
            "      LIMIT 100 " +
            "    ) vr " +
            "    INNER JOIN `IdolVideo` iv ON vr.`videoUid` = iv.`videoUid` " +
            "  ) a " +
            "  INNER JOIN `Idol` i ON a.`idolUid` = i.`idolUid` " +
            "  LEFT JOIN `IdolImage` ii ON i.`idolUid` = ii.`idolUid` AND ii.`mainImage` = 1 " +
            "  LEFT JOIN `IdolInfo` if1 ON i.`idolUid` = if1.`idolUid` AND if1.`lang` = #{lang} " +
            "  LEFT JOIN `IdolInfo` if2 ON i.`idolUid` = if2.`idolUid` AND if2.`lang` = 'en' " +
            "LIMIT 10")
    List<Idol> findByBest(String lang);
}
