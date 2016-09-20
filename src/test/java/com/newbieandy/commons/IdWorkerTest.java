package com.newbieandy.commons;

import org.junit.Test;

/**
 * Created by machao on 2016/9/20.
 */
public class IdWorkerTest {
    IdWorker idWorker = new IdWorker();
    @Test
    public void testGenerateId(){
        long id = idWorker.nextId();
        System.out.println(id);
    }
}
