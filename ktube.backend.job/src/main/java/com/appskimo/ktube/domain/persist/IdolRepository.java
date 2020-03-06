package com.appskimo.ktube.domain.persist;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.appskimo.ktube.domain.model.Idol;

@Mapper
public interface IdolRepository {

    @Insert("INSERT INTO `Idol` (`idolUid`,`searchName`, `tags`) VALUES (#{idolUid},#{searchName},#{tags})")
    void insert(Idol idol);

    @Select("SELECT * FROM `Idol`")
    List<Idol> findAll();

}
