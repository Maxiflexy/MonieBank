package com.maxiflexy.auth_service.dto.request;

//import com.maxiflexy.auth_service.encryption.Encrypted;
import com.maxiflexy.common.encryption.annotations.Encrypted;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {

    @NotBlank
    @Encrypted
    private String name;

    @NotBlank
    @Email
    @Encrypted
    private String email;

    @NotBlank
    @Size(min = 6, max = 20)
    @Encrypted
    private String password;
}
