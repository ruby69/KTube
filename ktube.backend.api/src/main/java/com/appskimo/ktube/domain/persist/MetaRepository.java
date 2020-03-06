package com.appskimo.ktube.domain.persist;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.appskimo.ktube.domain.model.Meta;

@Mapper
public interface MetaRepository {

    @Deprecated
    @Select("SELECT * FROM `Meta` ORDER BY `metaUid` DESC LIMIT 1")
    Meta findByLatest();

    @Select("SELECT * FROM `${value}` ORDER BY `metaUid` DESC LIMIT 1")
    Meta findAppMetaByLatest(String table);

}
