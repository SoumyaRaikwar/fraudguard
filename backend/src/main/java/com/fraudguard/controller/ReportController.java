
package com.fraudguard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        return Map.of(
                "totalAlerts", 142,
                "highPriority", 27,
                "sarsFiled", 18,
                "avgInvestigationHours", 3.2,
                "modelAccuracy", Map.of("XGBoost", 0.92, "GNN", 0.89, "RuleEngine", 0.76)
        );
    }
}
