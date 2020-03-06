package com.appskimo.ktube.domain.persist;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

import com.appskimo.ktube.domain.model.IdolInfo;

@Mapper
public interface IdolInfoRepository {

    @Insert("INSERT INTO `IdolInfo` (`idolUid`,`lang`,`name`,`url`,`summary`) VALUES (#{idolUid},#{lang},#{name},#{url},#{summary})")
    @Options(useGeneratedKeys = true, keyProperty = "idolInfoUid", keyColumn = "idolInfoUid")
    void insert(IdolInfo idolInfo);

    @Delete("DELETE FROM `IdolInfo` WHERE `idolUid` = #{idolUid}")
    void deleteByIdolUid(Long idolUid);

}
