
package com.fraudguard.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String role; // ADMIN/ANALYST

    public CharSequence getPassword() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setRole(String analyst) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object getRole() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPassword(String encode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getUsername() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
