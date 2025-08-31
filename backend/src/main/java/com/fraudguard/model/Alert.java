
package com.fraudguard.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String severity; // HIGH/MEDIUM/LOW
    private String status;   // NEW/IN_REVIEW/ESCALATED/SAR_PREPARED
}
