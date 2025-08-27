package com.fraudguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableCaching
@EnableJpaRepositories
@EnableTransactionManagement
public class FraudGuardApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(FraudGuardApplication.class, args);
        System.out.println("🚀 FraudGuard Backend Started Successfully!");
        System.out.println("📊 Swagger UI: http://localhost:8080/api/swagger-ui.html");
        System.out.println("🔍 API Docs: http://localhost:8080/api/api-docs");
    }
}
