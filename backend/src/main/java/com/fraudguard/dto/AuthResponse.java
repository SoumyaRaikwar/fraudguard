
package com.fraudguard.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class AuthResponse {

    public AuthResponse(String token2) {
        //TODO Auto-generated constructor stub
    }

    private String token;
}
