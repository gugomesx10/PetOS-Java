package br.com.petos.project.repository;

import br.com.petos.project.domain.VaccinationPolicy;
import br.com.petos.project.entity.Pet;
import br.com.petos.project.entity.User;
import br.com.petos.project.entity.Vaccine;
import br.com.petos.project.enums.Role;
import br.com.petos.project.enums.Species;
import br.com.petos.project.enums.VaccineStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Consultas preventivas de pets e vacinas")
class PetVaccineQueriesTest {

    private static final LocalDate TODAY = LocalDate.now();

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private VaccineRepository vaccineRepository;

    @Autowired
    private UserRepository userRepository;

    private User tutor;
    private User outroTutor;

    @BeforeEach
    void setUp() {
        tutor = userRepository.save(user("dono@petos.test"));
        outroTutor = userRepository.save(user("outro@petos.test"));
    }

    @Test
    @DisplayName("Pet cuja vacina já foi aplicada e está em dia não entra na lista preventiva")
    void appliedVaccineIsNotReported() {
        Pet pet = savePet("Thor", tutor, true);
        saveVaccine(pet, TODAY.plusDays(5), VaccineStatus.APPLIED);

        assertThat(findDuePets()).isEmpty();
    }

    @Test
    @DisplayName("Pet com vacina vencida entra na lista preventiva")
    void overdueVaccineIsReported() {
        Pet pet = savePet("Bob", tutor, true);
        saveVaccine(pet, TODAY.minusDays(10), VaccineStatus.OVERDUE);

        assertThat(findDuePets()).extracting(Pet::getName).containsExactly("Bob");
    }

    @Test
    @DisplayName("Pet com vacina dentro da janela de 30 dias entra na lista preventiva")
    void expiringSoonVaccineIsReported() {
        Pet pet = savePet("Luna", tutor, true);
        saveVaccine(pet, TODAY.plusDays(10), VaccineStatus.EXPIRING_SOON);

        assertThat(findDuePets()).extracting(Pet::getName).containsExactly("Luna");
    }

    @Test
    @DisplayName("Vacina com vencimento além da janela não entra na lista preventiva")
    void vaccineOutsideWindowIsNotReported() {
        Pet pet = savePet("Nina", tutor, true);
        saveVaccine(pet, TODAY.plusDays(90), VaccineStatus.PENDING);

        assertThat(findDuePets()).isEmpty();
    }

    @Test
    @DisplayName("Pet inativo nunca entra na lista preventiva")
    void inactivePetIsNotReported() {
        Pet pet = savePet("Inativo", tutor, false);
        saveVaccine(pet, TODAY.minusDays(10), VaccineStatus.OVERDUE);

        assertThat(findDuePets()).isEmpty();
    }

    @Test
    @DisplayName("A consulta por tutor não vaza pets de outro tutor")
    void ownerScopedQueryIsIsolated() {
        Pet meuPet = savePet("Meu", tutor, true);
        saveVaccine(meuPet, TODAY.minusDays(3), VaccineStatus.OVERDUE);
        Pet petAlheio = savePet("Alheio", outroTutor, true);
        saveVaccine(petAlheio, TODAY.minusDays(3), VaccineStatus.OVERDUE);

        List<Pet> pets = petRepository.findActivePetsByOwnerWithVaccinesDueUntil(
                tutor.getId(), VaccinationPolicy.warningThreshold(TODAY), VaccineStatus.APPLIED);

        assertThat(pets).extracting(Pet::getName).containsExactly("Meu");
    }

    @Test
    @DisplayName("Vacinas pendentes do pet excluem as já aplicadas")
    void pendingVaccinesExcludeApplied() {
        Pet pet = savePet("Thor", tutor, true);
        saveVaccine(pet, TODAY.plusDays(5), VaccineStatus.EXPIRING_SOON);
        saveVaccine(pet, TODAY.minusDays(5), VaccineStatus.OVERDUE);
        saveVaccine(pet, TODAY.plusDays(300), VaccineStatus.APPLIED);

        List<Vaccine> pending = vaccineRepository.findByPetIdAndStatusInOrderByDueDateAsc(
                pet.getId(), VaccinationPolicy.PENDING_STATUSES);

        assertThat(pending).hasSize(2)
                .extracting(Vaccine::getStatus)
                .doesNotContain(VaccineStatus.APPLIED);
    }

    @Test
    @DisplayName("Pet inativo não é encontrado pela busca de pet ativo")
    void inactivePetIsNotFound() {
        Pet pet = savePet("Inativo", tutor, false);

        assertThat(petRepository.findByIdAndActiveTrue(pet.getId())).isEmpty();
    }

    private List<Pet> findDuePets() {
        return petRepository.findActivePetsWithVaccinesDueUntil(
                VaccinationPolicy.warningThreshold(TODAY), VaccineStatus.APPLIED);
    }

    private User user(String email) {
        return User.builder()
                .name("Tutor")
                .email(email)
                .passwordHash("hash-irrelevante-para-o-teste")
                .role(Role.TUTOR)
                .build();
    }

    private Pet savePet(String name, User owner, boolean active) {
        return petRepository.save(Pet.builder()
                .name(name)
                .species(Species.DOG)
                .tutorName("Tutor")
                .owner(owner)
                .active(active)
                .build());
    }

    private void saveVaccine(Pet pet, LocalDate dueDate, VaccineStatus status) {
        vaccineRepository.save(Vaccine.builder()
                .pet(pet)
                .name("Vacina")
                .dueDate(dueDate)
                .status(status)
                .build());
    }
}


