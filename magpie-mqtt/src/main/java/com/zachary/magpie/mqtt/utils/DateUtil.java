package com.zachary.magpie.mqtt.utils;

import java.time.Instant;

/**
 * @description: 日期工具类
 * @author: cuiweiman
 * @date: 2024/3/5 10:12
 */
public class DateUtil {

    public static Long milliTimestamp() {
        return Instant.now().toEpochMilli();
    }

}
