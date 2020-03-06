package com.appskimo.app.ktube.domain;

import java.util.EnumSet;
import java.util.Set;

import lombok.Getter;

@Getter
public enum SupportLanguage {

    ar("ar", "العربية"),
    bg("bg", "български"),
    ca("ca", "català"),
    cs("cs", "čeština"),
    da("da", "Dansk"),
    de("de", "Deutsch"),
    el("el", "Ελληνικά"),
    en("en", "English"),
    es("es", "español"),
    fi("fi", "suomi"),
    fr("fr", "français"),
    hr("hr", "hrvatski"),
    hu("hu", "magyar"),
    it("it", "italiano"),
    ja("ja", "日本語"),
    ko("ko", "한국어"),
    lt("lt", "Lietuvių"),
    lv("lv", "Latviešu"),
    nl("nl", "Nederlands"),
    no("no", "norsk"),
    pl("pl", "polski"),
    pt("pt", "português"),
    ro("ro", "română"),
    ru("ru", "русский"),
    sr("sr", "Српски"),
    sk("sk", "Slovenčina"),
    sl("sl", "Slovenščina"),
    sv("sv", "svenska"),
    th("th", "ไทย"),
    tr("tr", "Türkçe"),
    uk("uk", "українська"),
    vi("vi", "Tiếng Việt"),
    zh_Hans("zh_Hans", "中文(简体)"), // 간체 (중국) zh-cn, zh-sg
    zh_Hant("zh_Hant", "中文(繁體)"); // 번체 (대만) zh-hk, zh-tw;

    private String code;
    private String displayName;

    private SupportLanguage(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    private static Set<SupportLanguage> set = EnumSet.of(ar, bg, ca, hr, zh_Hans, zh_Hant, cs, da, nl, en, fi, fr, de, el, hu, it, ja, ko, lv, lt, no, pl, pt, ro, ru, sr, sk, sl, es, sv, th, tr, uk, vi);

    public static boolean isSupportLanguage(String code) {
        try {
            return set.contains(SupportLanguage.valueOf(code));
        } catch (Exception e) {
            return false;
        }
    }

}
