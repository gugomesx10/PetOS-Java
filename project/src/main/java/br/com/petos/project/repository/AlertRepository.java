package br.com.petos.project.repository;

import br.com.petos.project.entity.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
