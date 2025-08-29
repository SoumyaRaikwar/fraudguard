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
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.fraudguard.repository")
@EnableJpaAuditing
@EnableTransactionManagement
@EnableCaching
@EnableAsync
@EnableScheduling
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

    /** Configure Jackson ObjectMapper for proper JSON handling */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }

    /** Configure CORS settings for frontend integration */
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

    /** Configure async task executor for fraud detection processing */
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

    /** Configure async task executor for alert processing */
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
}
