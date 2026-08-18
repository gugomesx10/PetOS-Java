package br.com.petos.project.controller;

import br.com.petos.project.dto.AuthResponseDTO;
import br.com.petos.project.dto.LoginRequestDTO;
import br.com.petos.project.dto.RegisterRequestDTO;
import br.com.petos.project.dto.UserResponseDTO;
import br.com.petos.project.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Cadastro, login e usuário autenticado")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @SecurityRequirements
    @Operation(summary = "Cadastrar usuário (TUTOR ou CLINICA) e receber o token")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "422", description = "Email já cadastrado")
    })
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(dto));
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Autenticar e obter token JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticado"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Dados do usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário autenticado"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido")
    })
    public ResponseEntity<UserResponseDTO> me() {
        return ResponseEntity.ok(authService.currentUser());
    }
}

