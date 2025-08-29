package com.fraudguard.repository;

import com.fraudguard.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .passwordHash("hashed-password")
                .isActive(true)
                .riskProfile("LOW")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenEmailExists() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> result = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        assertThat(result.get().getFirstName()).isEqualTo("Test");
    }

    @Test
    void findByEmail_ShouldReturnEmpty_WhenEmailNotExists() {
        // When
        Optional<User> result = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        boolean exists = userRepository.existsByEmail("test@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_ShouldReturnFalse_WhenEmailNotExists() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void findByRiskProfile_ShouldReturnUsers_WithSpecificRiskProfile() {
        // Given
        User lowRiskUser = createUserWithRiskProfile("LOW");
        User highRiskUser = createUserWithRiskProfile("HIGH");
        
        entityManager.persistAndFlush(lowRiskUser);
        entityManager.persistAndFlush(highRiskUser);
        entityManager.persistAndFlush(testUser); // Also LOW risk

        // When
        List<User> lowRiskUsers = userRepository.findByRiskProfile("LOW");

        // Then
        assertThat(lowRiskUsers).hasSize(2);
        assertThat(lowRiskUsers).allMatch(user -> user.getRiskProfile().equals("LOW"));
    }

    @Test
    void findActiveUsers_ShouldReturnOnlyActiveUsers() {
        // Given
        User inactiveUser = User.builder()
                .id(UUID.randomUUID())
                .email("inactive@example.com")
                .firstName("Inactive")
                .lastName("User")
                .isActive(false)
                .riskProfile("LOW")
                .createdAt(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(testUser); // Active
        entityManager.persistAndFlush(inactiveUser); // Inactive

        // When
        List<User> activeUsers = userRepository.findByIsActiveTrue();

        // Then
        assertThat(activeUsers).hasSize(1);
        assertThat(activeUsers.get(0).isActive()).isTrue();
        assertThat(activeUsers.get(0).getEmail()).isEqualTo("test@example.com");
    }

    private User createUserWithRiskProfile(String riskProfile) {
        return User.builder()
                .id(UUID.randomUUID())
                .email(riskProfile.toLowerCase() + "@example.com")
                .firstName("Test")
                .lastName("User")
                .isActive(true)
                .riskProfile(riskProfile)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
