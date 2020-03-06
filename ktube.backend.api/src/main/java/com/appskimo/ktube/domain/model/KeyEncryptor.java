package com.appskimo.ktube.domain.model;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.util.StringUtils;

public abstract class KeyEncryptor {
    public enum Target { IDOL, VIDEO, USER, COMMON }

    private static final TextEncryptor idolEncryptor = Encryptors.text("a5ae2709fda3a2b3", "c149009b9cdc4505");
    private static final TextEncryptor videoEncryptor = Encryptors.text("dc355b1584438273", "f0a28a84f6d2dc57");
    private static final TextEncryptor userEncryptor = Encryptors.text("564eb24741eca4cc", "2cdd1d7edb56ab89");
    private static final TextEncryptor commonEncryptor = Encryptors.text("04ebf971c3799073", "bf8b318c8632a2d3");

    private static final Map<Target, TextEncryptor> targetEncryptors = new HashMap<>();
    static {
        targetEncryptors.put(Target.IDOL, idolEncryptor);
        targetEncryptors.put(Target.VIDEO, videoEncryptor);
        targetEncryptors.put(Target.USER, userEncryptor);
        targetEncryptors.put(Target.COMMON, commonEncryptor);
    }

    public static final <T> String getKey(Target target, T uid) {
        return uid == null ? null : targetEncryptors.get(target).encrypt(String.valueOf(uid));
    }

    public static final Long getUid(Target target, String key) {
        return StringUtils.hasText(key) ? Long.parseLong(targetEncryptors.get(target).decrypt(key)) : null;
    }

    public static final String getPlain(Target target, String key) {
        return StringUtils.hasText(key) ? targetEncryptors.get(target).decrypt(key) : null;
    }
}
