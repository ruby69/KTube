package com.appskimo.ktube.domain.persist;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.appskimo.ktube.domain.model.IdolImage;
import com.appskimo.ktube.domain.model.Page;

@Mapper
public interface IdolImageRepository {

    @Select("SELECT COUNT(1) FROM `IdolImage` WHERE `idolUid` = #{p.idolUid}")
    int countIdolImagesByPage(Page page);

    @Select("SELECT * FROM `IdolImage` ii WHERE ii.`idolUid` = #{p.idolUid} ORDER BY ii.`idolImageUid` DESC LIMIT #{beginIndex}, #{scale}")
    List<IdolImage> findIdolImagesByPage(Page page);

}
