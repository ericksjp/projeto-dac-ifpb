package com.ifpb.charger_proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ChargerProxyApplication{
    public static void main(String[] args) {
        SpringApplication.run(ChargerProxyApplication.class, args);
    }
}
