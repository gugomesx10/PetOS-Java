package br.com.petos.project.service;

import br.com.petos.project.dto.AuthResponseDTO;
import br.com.petos.project.dto.LoginRequestDTO;
import br.com.petos.project.dto.RegisterRequestDTO;
import br.com.petos.project.dto.UserResponseDTO;
import br.com.petos.project.entity.User;
import br.com.petos.project.exception.BusinessRuleException;
import br.com.petos.project.repository.UserRepository;
import br.com.petos.project.security.AuthenticatedUser;
import br.com.petos.project.security.CurrentUserProvider;
import br.com.petos.project.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String BEARER_TYPE = "Bearer";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessRuleException("Já existe um usuário cadastrado com este email.");
        }

        User user = User.builder()
                .name(dto.getName().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .active(true)
                .build();

        return buildAuthResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, dto.getPassword()));

        AuthenticatedUser authenticated = (AuthenticatedUser) authentication.getPrincipal();
        return AuthResponseDTO.builder()
                .token(jwtService.generateToken(authenticated))
                .tokenType(BEARER_TYPE)
                .expiresInSeconds(jwtService.getExpirationSeconds())
                .userId(authenticated.getId())
                .name(resolveName(authenticated))
                .email(authenticated.getEmail())
                .role(authenticated.getRole())
                .build();
    }

    @Transactional(readOnly = true)
    public UserResponseDTO currentUser() {
        AuthenticatedUser authenticated = currentUserProvider.require();
        return UserResponseDTO.builder()
                .id(authenticated.getId())
                .name(resolveName(authenticated))
                .email(authenticated.getEmail())
                .role(authenticated.getRole())
                .build();
    }

    private AuthResponseDTO buildAuthResponse(User user) {
        AuthenticatedUser authenticated = AuthenticatedUser.from(user);
        return AuthResponseDTO.builder()
                .token(jwtService.generateToken(authenticated))
                .tokenType(BEARER_TYPE)
                .expiresInSeconds(jwtService.getExpirationSeconds())
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    private String resolveName(AuthenticatedUser authenticated) {
        return userRepository.findById(authenticated.getId())
                .map(User::getName)
                .orElse(authenticated.getEmail());
    }
}

