package com.appskimo.app.ktube.support;

public interface Query<T> {
    T execute() throws Exception;
}