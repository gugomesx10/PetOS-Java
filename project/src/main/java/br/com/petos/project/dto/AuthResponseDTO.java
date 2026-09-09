package br.com.petos.project.dto;

import br.com.petos.project.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {

    private String token;
    private String tokenType;
    private Long expiresInSeconds;
    private Long userId;
    private String name;
    private String email;
    private Role role;
}

