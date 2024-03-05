package com.zachary.magpie.mqtt.utils;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

/**
 * @description: 雪花
 * @author: cuiweiman
 * @date: 2024/3/5 10:22
 */
@Slf4j
public class SnowFlake {
    private static final Long WORK_ID_BITS = 5L;
    private static final Long DATA_CENTER_ID_BITS = 5L;
    private static final Long SEQUENCE_BITS = 12L;
    private static final Long SEQUENCE_MAX = ~(-1L << SEQUENCE_BITS);
    private static final Long WORKER_SHIFT_BITS = SEQUENCE_BITS;
    private static final Long DATA_CENTER_SHIFT_BITS = WORKER_SHIFT_BITS + WORK_ID_BITS;
    private static final Long OFFSET_SHIFT_BITS = DATA_CENTER_SHIFT_BITS + DATA_CENTER_ID_BITS;

    private static final Long OFFSET = 1288834974657L;


    private final Long workId;
    private final Long dataCenterId;
    private Long sequence;

    private long lastTimestamp = -1L;

    public SnowFlake(Long workId, Long dataCenterId) {
        Long workIdMax = ~(-1L << WORK_ID_BITS);
        if (workId > workIdMax || workId < 0) {
            throw new IllegalArgumentException(
                    String.format("worker Id can't be greater than %d or less than 0", workIdMax));
        }
        Long dataCenterIdMax = ~(-1L << DATA_CENTER_ID_BITS);
        if (dataCenterId > dataCenterIdMax || dataCenterId < 0) {
            throw new IllegalArgumentException(
                    String.format("dataCenter Id can't be greater than %d or less than 0", dataCenterIdMax));
        }
        this.workId = workId;
        this.dataCenterId = dataCenterId;
    }

    public synchronized Long nextId() {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) {
            log.error("clock is moving backwards. Rejecting requests until {}.", lastTimestamp);
            throw new RuntimeException(
                    String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds",
                            lastTimestamp - timestamp));
        }
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MAX;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        return (timestamp - OFFSET) << OFFSET_SHIFT_BITS
                | dataCenterId << DATA_CENTER_SHIFT_BITS
                | workId << WORKER_SHIFT_BITS
                | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return Instant.now().toEpochMilli();
    }
}