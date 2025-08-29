package com.fraudguard.repository;

import com.fraudguard.entity.FraudRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class FraudRuleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FraudRuleRepository fraudRuleRepository;

    private FraudRule activeRule;
    private FraudRule inactiveRule;

    @BeforeEach
    void setUp() {
        activeRule = FraudRule.builder()
                .id("rule-001")
                .name("High Amount Transaction")
                .description("Flag transactions over $10,000")
                .ruleType("AMOUNT")
                .thresholdValue(new BigDecimal("10000.00"))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        inactiveRule = FraudRule.builder()
                .id("rule-002")
                .name("Inactive Velocity Rule")
                .description("Old velocity rule")
                .ruleType("VELOCITY")
                .thresholdValue(new BigDecimal("5.0"))
                .isActive(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void findByIsActiveTrue_ShouldReturnOnlyActiveRules() {
        // Given
        entityManager.persistAndFlush(activeRule);
        entityManager.persistAndFlush(inactiveRule);

        // When
        List<FraudRule> activeRules = fraudRuleRepository.findByIsActiveTrue();

        // Then
        assertThat(activeRules).hasSize(1);
        assertThat(activeRules.get(0).isActive()).isTrue();
        assertThat(activeRules.get(0).getId()).isEqualTo("rule-001");
    }

    @Test
    void findByRuleType_ShouldReturnRulesOfSpecificType() {
        // Given
        FraudRule anotherAmountRule = FraudRule.builder()
                .id("rule-003")
                .name("Medium Amount Rule")
                .ruleType("AMOUNT")
                .thresholdValue(new BigDecimal("5000.00"))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(activeRule); // AMOUNT type
        entityManager.persistAndFlush(inactiveRule); // VELOCITY type
        entityManager.persistAndFlush(anotherAmountRule); // AMOUNT type

        // When
        List<FraudRule> amountRules = fraudRuleRepository.findByRuleType("AMOUNT");

        // Then
        assertThat(amountRules).hasSize(2);
        assertThat(amountRules).allMatch(rule -> rule.getRuleType().equals("AMOUNT"));
    }

    @Test
    void findByRuleTypeAndIsActiveTrue_ShouldReturnActiveRulesOfType() {
        // Given
        entityManager.persistAndFlush(activeRule); // AMOUNT, active
        entityManager.persistAndFlush(inactiveRule); // VELOCITY, inactive

        // When
        List<FraudRule> activeAmountRules = 
                fraudRuleRepository.findByRuleTypeAndIsActiveTrue("AMOUNT");

        // Then
        assertThat(activeAmountRules).hasSize(1);
        assertThat(activeAmountRules.get(0).getRuleType()).isEqualTo("AMOUNT");
        assertThat(activeAmountRules.get(0).isActive()).isTrue();
    }
}
