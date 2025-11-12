package com.yourcompany.erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ErpApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpApplication.class, args);
        System.out.println("========================================");
        System.out.println("✅ ERP Backend 启动成功！");
        System.out.println("📡 API 地址: http://localhost:8080");
        System.out.println("========================================");
    }

}
