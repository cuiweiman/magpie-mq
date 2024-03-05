package com.zachary.magpie.mqtt.utils;

import java.lang.management.ManagementFactory;

/**
 * @description: 系统环境工具
 * @author: cuiweiman
 * @date: 2024/3/4 18:37
 */
public class SystemUtil {

    public static String getPid() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        return name.split("@")[0];
    }

}
