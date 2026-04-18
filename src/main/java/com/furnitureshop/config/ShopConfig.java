package com.furnitureshop.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "shop")
@Getter
@Setter
public class ShopConfig {
    private String name = "MMD Furniture House";
    private String address = "123, MG Road, Bengaluru - 560001, Karnataka";
    private String phone = "+91-9876543210";
    private String email = "info@MMDfurniturehouse.com";
    private String gstin = "29AABCR1234F1Z5";
    private String tagline = "Quality Furniture for Every Home";
}
