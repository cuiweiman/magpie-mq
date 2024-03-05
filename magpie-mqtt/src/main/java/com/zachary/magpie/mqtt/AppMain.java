package com.zachary.magpie.mqtt;

/**
 * @description:
 * @author: cuiweiman
 * @date: 2024/2/29 17:26
 */
public class AppMain {
    public static void main(String[] args) {
        MqttBootstrap mqttBootstrap = new MqttBootstrap();
        mqttBootstrap.start();
    }
}
