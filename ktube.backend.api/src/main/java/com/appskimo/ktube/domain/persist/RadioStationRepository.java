package com.appskimo.ktube.domain.persist;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.appskimo.ktube.domain.model.RadioStation;

@Mapper
public interface RadioStationRepository {

    @Select("SELECT * FROM `RadioStation` ORDER BY `title`")
    List<RadioStation> findAll();

}
