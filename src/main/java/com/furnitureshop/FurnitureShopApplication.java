package com.furnitureshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class FurnitureShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(FurnitureShopApplication.class, args);
    }
}
