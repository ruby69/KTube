package com.appskimo.ktube.service.impl;

import java.net.URLEncoder;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.appskimo.ktube.domain.model.Idol;
import com.appskimo.ktube.domain.model.IdolInfo;
import com.appskimo.ktube.domain.persist.IdolInfoRepository;
import com.appskimo.ktube.domain.persist.IdolRepository;
import com.appskimo.ktube.service.GoogleSearchable;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GoogleSearcher implements GoogleSearchable {
    @Autowired private IdolRepository idolRepository;
    @Autowired private IdolInfoRepository idolInfoRepository;

    private static final String GOOGLE_SEARCH = "https://www.google.co.kr/search?%s";

    @Override
    public void search() {
        List<Idol> idols = idolRepository.findAll();
        idols
        .stream().filter(idol -> idol.getIdolUid().longValue() == 116L)
        .forEach(this::search);
    }

    private void search(Idol idol) {
        String query = null;
        try{
            query = "q=".concat(URLEncoder.encode(idol.getSearchName(), "UTF-8"));
        }catch(Exception e) {
            return;
        }

        idolInfoRepository.deleteByIdolUid(idol.getIdolUid());

        for(IdolInfo.Lang lang : IdolInfo.Lang.values()){
            IdolInfo info = collectInfo(idol.getIdolUid(), query, lang);
            log.info("{}({}) : {}", idol.getSearchName(), lang, info);
            if(info != null && info.getUrl() != null) {
                idolInfoRepository.insert(info);
            }
            try{Thread.sleep(500L);}catch(Exception e){};
        }
    }

    private IdolInfo collectInfo(Long idolUid, String query, IdolInfo.Lang lang) {
        String langCode = lang.name();
        if(lang == IdolInfo.Lang.zh_Hans || lang == IdolInfo.Lang.zh_Hant) {
            langCode = langCode.replace("_", "-");
        }

        String url = String.format(GOOGLE_SEARCH, query.concat("&hl=").concat(langCode));
        try {
            Document document = Jsoup.connect(url).timeout(60 * 5000).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36").get();
            Element nameElement = document.select("div.kno-ecr-pt").first();
            if(nameElement == null) {
                return null;
            }

            IdolInfo info = new IdolInfo();
            info.setIdolUid(idolUid);
            info.setLang(lang);
            info.setName(nameElement.text());

            Element descElement = document.select("div.kno-rdesc").first();
            if(descElement != null) {
                Element summaryElement = descElement.select("span").first();
                info.setSummary(summaryElement == null ? null : summaryElement.text());

                Element linkElement = descElement.select("a").first();
                info.setUrl(linkElement == null ? null : linkElement.attr("href"));
            }

            return info;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}

