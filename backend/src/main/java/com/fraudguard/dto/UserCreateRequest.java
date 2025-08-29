package com.fraudguard.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreateRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 100)
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 100)
    private String lastName;
    
    @Size(max = 20)
    private String phoneNumber;
    
    @Size(max = 100)
    private String city;
    
    @Size(max = 100)
    private String country;
}
