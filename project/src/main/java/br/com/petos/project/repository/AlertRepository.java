package br.com.petos.project.repository;

import br.com.petos.project.entity.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import br.com.petos.project.entity.Vaccine;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import br.com.petos.project.enums.AlertType;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByPetIdOrderByCreatedAtDesc(Long petId);

    List<Alert> findByPetIdAndSentFalseOrderByDueDateAsc(Long petId);

    List<Alert> findByPetActiveTrueAndSentFalseOrderByDueDateAsc();

    List<Alert> findByPetOwnerIdAndPetActiveTrueAndSentFalseOrderByDueDateAsc(Long ownerId);

    Page<Alert> findByPetActiveTrue(Pageable pageable);

    Page<Alert> findByPetOwnerIdAndPetActiveTrue(Long ownerId, Pageable pageable);

    Optional<Alert> findByVaccineIdAndSentFalse(Long vaccineId);

    @Modifying
    @Query("DELETE FROM Alert alert WHERE alert.vaccine = :vaccine")
    int deleteLinkedToVaccine(@Param("vaccine") Vaccine vaccine);

    @Query("""
    SELECT alert FROM Alert alert
    JOIN FETCH alert.pet pet
    JOIN FETCH pet.owner owner
    WHERE alert.vaccine IS NOT NULL
      AND alert.sent = false
      AND alert.emailSentAt IS NULL
      AND alert.type IN :types
      AND pet.active = true
      AND owner.active = true
    """)
    List<Alert> findVaccinationAlertsPendingEmail(
            @Param("types") List<AlertType> types
    );
}
