package com.appskimo.ktube.domain.persist;

import java.util.Set;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import com.appskimo.ktube.domain.model.IdolImage;

@Mapper
public interface IdolImageRepository {

    @Insert("INSERT INTO `IdolImage` (`idolUid`,`sourceLink`,`url`,`width`,`height`,`thumbUrl`,`thumbWidth`,`thumbHeight`) VALUES (#{idolUid},#{sourceLink},#{url},#{width},#{height},#{thumbUrl},#{thumbWidth},#{thumbHeight})")
    @Options(useGeneratedKeys = true, keyProperty = "idolImageUid", keyColumn = "idolImageUid")
    void insert(IdolImage idolImage);

    @Delete("DELETE FROM `IdolImage` WHERE `mainImage` = 0 AND `idolUid` = #{idolUid}")
    void deleteNoMainByIdolUid(Long idolUid);

    @Select("SELECT `url` FROM `IdolImage`")
    Set<String> findAllIdolImagesUrl();

}
