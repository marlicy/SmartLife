package com.utils;

public interface ILock {

    boolean tryLock(Long timeoutSec);


    void unlock();
}
