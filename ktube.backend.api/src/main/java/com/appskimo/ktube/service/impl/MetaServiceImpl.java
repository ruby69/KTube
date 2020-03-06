package com.appskimo.ktube.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.appskimo.ktube.domain.model.App;
import com.appskimo.ktube.domain.persist.MetaRepository;
import com.appskimo.ktube.service.MetaService;

@Component
@Transactional(readOnly = true)
public class MetaServiceImpl implements MetaService {
    @Autowired private MetaRepository metaRepository;

    @Override
    public Object getMeta(App app) {
        return metaRepository.findAppMetaByLatest(app.getTable());
    }
}
