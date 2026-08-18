package br.com.petos.project.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CurrentUserProvider {

    public Optional<AuthenticatedUser> current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        if (authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public AuthenticatedUser require() {
        return current().orElseThrow(() ->
                new IllegalStateException("Nenhum usuário autenticado no contexto de segurança."));
    }
}

