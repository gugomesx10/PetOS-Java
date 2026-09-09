package br.com.petos.project.config;

import br.com.petos.project.entity.Pet;
import br.com.petos.project.entity.User;
import br.com.petos.project.enums.Role;
import br.com.petos.project.repository.PetRepository;
import br.com.petos.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DevelopmentDataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DevelopmentDataSeeder.class);

    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${petos.dev.seed.tutor-email:tutor@petos.local}")
    private String tutorEmail;

    @Value("${petos.dev.seed.clinica-email:clinica@petos.local}")
    private String clinicaEmail;

    @Value("${petos.dev.seed.password:petos@dev2026}")
    private String seedPassword;

    @Bean
    public ApplicationRunner seedDevelopmentUsers() {
        return args -> seed();
    }

    private void seed() {
        User tutor = createIfAbsent(tutorEmail, "Gustavo Gomes Martins (tutor de exemplo)", Role.TUTOR);
        createIfAbsent(clinicaEmail, "Clínica VetPetOS (exemplo)", Role.CLINICA);
        adoptOrphanPets(tutor);
    }

    private User createIfAbsent(String email, String name, Role role) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> {
                    User user = userRepository.save(User.builder()
                            .name(name)
                            .email(email.toLowerCase())
                            .passwordHash(passwordEncoder.encode(seedPassword))
                            .role(role)
                            .active(true)
                            .build());
                    log.info("Usuário de desenvolvimento criado: {} ({})", email, role);
                    return user;
                });
    }

    private void adoptOrphanPets(User tutor) {
        List<Pet> orphanPets = petRepository.findAll().stream()
                .filter(pet -> pet.getOwner() == null)
                .toList();
        if (orphanPets.isEmpty()) {
            return;
        }
        orphanPets.forEach(pet -> pet.setOwner(tutor));
        petRepository.saveAll(orphanPets);
        log.info("{} pet(s) de desenvolvimento vinculados ao tutor {}", orphanPets.size(), tutor.getEmail());
    }
}

