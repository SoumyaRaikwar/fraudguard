package com.fraudguard.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class AuthRequest {
    private String username;
    private String password;

    public String getUsername() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object getPassword() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
