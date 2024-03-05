package com.zachary.magpie.mqtt.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description:
 * @author: cuiweiman
 * @date: 2024/3/5 10:26
 */
public class IdWorker {

    private IdWorker() {
    }

    private enum Singleton {
        /**
         * 单例
         */
        INSTANCE;
        private final SnowFlake snowFlake;

        Singleton() {
            this.snowFlake = new SnowFlake(1L, 1L);
        }

        public SnowFlake getInstance() {
            return this.snowFlake;
        }
    }

    private static SnowFlake getInstance() {
        return Singleton.INSTANCE.getInstance();
    }

    public static Long nextId() {
        return IdWorker.getInstance().nextId();
    }

    private static final AtomicInteger ID_COUNTER = new AtomicInteger(1024);

    public static Integer nextIntId() {
        return ID_COUNTER.incrementAndGet();
    }

}
