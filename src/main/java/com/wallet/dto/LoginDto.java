package com.wallet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto {

    private String username;

    private String email;

    private String usernameOrEmail;

    @NotBlank(message = "Password is required")
    private String password;
}
