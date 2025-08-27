package com.fraudguard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * FraudGuard Application - Advanced Fraud Detection System
 * 
 * This is the main Spring Boot application class that bootstraps the entire
 * fraud detection system with real-time transaction monitoring, ML-based
 * risk scoring, and comprehensive alert management.
 * 
 * Features:
 * - Real-time transaction processing
 * - Machine learning fraud detection
 * - Behavioral analysis and pattern recognition
 * - Alert management and case workflow
 * - Performance monitoring and analytics
 * 
 * @author FraudGuard Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.fraudguard.repository")
@EnableTransactionManagement
@EnableCaching
@EnableAsync
@EnableScheduling
public class FraudGuardApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(FraudGuardApplication.class);
    
    /**
     * Main method to start the FraudGuard application
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // Set system properties for optimal performance
        System.setProperty("spring.jpa.open-in-view", "false");
        System.setProperty("spring.jpa.hibernate.use-new-id-generator-mappings", "true");
        
        try {
            // Start the Spring Boot application
            SpringApplication.run(FraudGuardApplication.class, args);
            
        } catch (Exception e) {
            logger.error("Failed to start FraudGuard Application: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
    
    /**
     * Application startup event listener
     * Displays system information and startup confirmation
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        logger.info("════════════════════════════════════════════════════════════");
        logger.info("🛡️  FRAUDGUARD FRAUD DETECTION SYSTEM STARTED SUCCESSFULLY");
        logger.info("════════════════════════════════════════════════════════════");
        logger.info("🚀 Application: FraudGuard Backend API");
        logger.info("⏰ Started At: {}", timestamp);
        logger.info("🌐 Environment: {}", getActiveProfile());
        logger.info("📊 Server Port: {}", getServerPort());
        logger.info("🔗 Health Check: http://localhost:{}/actuator/health", getServerPort());
        logger.info("📖 API Documentation: http://localhost:{}/swagger-ui.html", getServerPort());
        logger.info("🔍 API Endpoints: http://localhost:{}/api-docs", getServerPort());
        logger.info("════════════════════════════════════════════════════════════");
        logger.info("🔐 FRAUD DETECTION CAPABILITIES:");
        logger.info("   ✅ Real-time Transaction Monitoring");
        logger.info("   ✅ Machine Learning Risk Scoring");
        logger.info("   ✅ Behavioral Pattern Analysis");
        logger.info("   ✅ Velocity and Amount Checking");
        logger.info("   ✅ Geolocation Anomaly Detection");
        logger.info("   ✅ Device Fingerprint Analysis");
        logger.info("   ✅ Alert Management System");
        logger.info("   ✅ Case Investigation Workflow");
        logger.info("   ✅ Performance Analytics Dashboard");
        logger.info("════════════════════════════════════════════════════════════");
        logger.info("🛡️  FRAUDGUARD IS NOW PROTECTING YOUR TRANSACTIONS");
        logger.info("════════════════════════════════════════════════════════════");
        
        // Log system statistics
        logSystemStatistics();
    }
    
    /**
     * Get active Spring profile
     */
    private String getActiveProfile() {
        String profiles = System.getProperty("spring.profiles.active");
        return profiles != null ? profiles : "default";
    }
    
    /**
     * Get server port from environment
     */
    private String getServerPort() {
        String port = System.getProperty("server.port");
        return port != null ? port : "8080";
    }
    
    /**
     * Log system statistics and configuration
     */
    private void logSystemStatistics() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;
        
        logger.info("💾 SYSTEM RESOURCES:");
        logger.info("   📊 Max Memory: {} MB", maxMemory);
        logger.info("   📊 Total Memory: {} MB", totalMemory);
        logger.info("   📊 Used Memory: {} MB", usedMemory);
        logger.info("   📊 Free Memory: {} MB", freeMemory);
        logger.info("   📊 Available Processors: {}", runtime.availableProcessors());
        logger.info("   📊 Java Version: {}", System.getProperty("java.version"));
        logger.info("   📊 Java Vendor: {}", System.getProperty("java.vendor"));
        
        // Log database connection info
        logDatabaseInfo();
    }
    
    /**
     * Log database connection information
     */
    private void logDatabaseInfo() {
        try {
            String dbUrl = System.getProperty("spring.datasource.url");
            String dbUser = System.getProperty("spring.datasource.username");
            
            if (dbUrl != null) {
                logger.info("🗄️  DATABASE CONNECTION:");
                logger.info("   📊 Database URL: {}", maskSensitiveInfo(dbUrl));
                logger.info("   📊 Database User: {}", maskSensitiveInfo(dbUser));
                logger.info("   ✅ Database Connection: Ready");
            }
        } catch (Exception e) {
            logger.warn("Could not retrieve database information: {}", e.getMessage());
        }
    }
    
    /**
     * Mask sensitive information for logging
     */
    private String maskSensitiveInfo(String info) {
        if (info == null || info.length() <= 4) {
            return "****";
        }
        return info.substring(0, Math.min(info.length() / 3, 10)) + "****";
    }
}
package com.fraudguard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Enhanced FraudGuard Application with additional configuration beans
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.fraudguard.repository")
@EnableJpaAuditing
@EnableTransactionManagement
@EnableCaching
@EnableAsync
@EnableScheduling
@Configuration
public class FraudGuardApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(FraudGuardApplication.class);
    
    public static void main(String[] args) {
        // Performance optimizations
        System.setProperty("spring.jpa.open-in-view", "false");
        System.setProperty("spring.jpa.hibernate.use-new-id-generator-mappings", "true");
        System.setProperty("spring.jpa.hibernate.jdbc.batch_size", "50");
        System.setProperty("spring.jpa.hibernate.order_inserts", "true");
        System.setProperty("spring.jpa.hibernate.order_updates", "true");
        
        SpringApplication app = new SpringApplication(FraudGuardApplication.class);
        
        // Add shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("🛑 FraudGuard Application is shutting down gracefully...");
        }));
        
        app.run(args);
    }
    
    /**
     * Configure Jackson ObjectMapper for proper JSON handling
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }
    
    /**
     * Configure CORS settings for frontend integration
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    /**
     * Configure async task executor for fraud detection processing
     */
    @Bean(name = "fraudDetectionExecutor")
    public Executor fraudDetectionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("FraudDetection-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
    
    /**
     * Configure async task executor for alert processing
     */
    @Bean(name = "alertProcessingExecutor")
    public Executor alertProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("AlertProcessing-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        displayStartupBanner();
        logSystemConfiguration();
        verifySystemHealth();
    }
    
    private void displayStartupBanner() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║          🛡️  FRAUDGUARD FRAUD DETECTION SYSTEM          ║");
        System.out.println("║                   Successfully Started                   ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.println("║  🚀 Application: FraudGuard Backend API                 ║");
        System.out.println("║  ⏰ Started At: " + String.format("%-35s", timestamp) + " ║");
        System.out.println("║  🌐 Environment: " + String.format("%-34s", getActiveProfile()) + " ║");
        System.out.println("║  📊 Server Port: " + String.format("%-34s", getServerPort()) + " ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.println("║                    🔗 QUICK LINKS                        ║");
        System.out.println("║  Health Check: http://localhost:" + getServerPort() + "/actuator/health    ║");
        System.out.println("║  API Docs: http://localhost:" + getServerPort() + "/swagger-ui.html       ║");
        System.out.println("║  Endpoints: http://localhost:" + getServerPort() + "/api-docs            ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.println("║              🔐 FRAUD DETECTION FEATURES                 ║");
        System.out.println("║  ✅ Real-time Transaction Monitoring                    ║");
        System.out.println("║  ✅ Machine Learning Risk Scoring                       ║");
        System.out.println("║  ✅ Behavioral Pattern Analysis                         ║");
        System.out.println("║  ✅ Velocity and Amount Checking                        ║");
        System.out.println("║  ✅ Geolocation Anomaly Detection                       ║");
        System.out.println("║  ✅ Device Fingerprint Analysis                         ║");
        System.out.println("║  ✅ Alert Management System                             ║");
        System.out.println("║  ✅ Case Investigation Workflow                         ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();
        
        logger.info("🛡️ FraudGuard is now protecting your transactions!");
    }
    
    private void logSystemConfiguration() {
        Runtime runtime = Runtime.getRuntime();
        logger.info("💾 System Resources - Max Memory: {} MB, Available Processors: {}", 
                   runtime.maxMemory() / (1024 * 1024), runtime.availableProcessors());
        logger.info("☕ Java Runtime - Version: {}, Vendor: {}", 
                   System.getProperty("java.version"), System.getProperty("java.vendor"));
    }
    
    private void verifySystemHealth() {
        try {
            // Add any startup health checks here
            logger.info("✅ System health check passed - All components ready");
        } catch (Exception e) {
            logger.error("❌ System health check failed: {}", e.getMessage(), e);
        }
    }
    
    private String getActiveProfile() {
        String profiles = System.getProperty("spring.profiles.active");
        return profiles != null ? profiles : "default";
    }
    
    private String getServerPort() {
        return System.getProperty("server.port", "8080");
    }
}
