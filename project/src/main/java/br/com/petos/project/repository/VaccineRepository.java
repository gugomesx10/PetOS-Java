package br.com.petos.project.repository;

import br.com.petos.project.entity.Vaccine;
import br.com.petos.project.enums.VaccineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VaccineRepository extends JpaRepository<Vaccine, Long> {

    List<Vaccine> findByPetId(Long petId);

    List<Vaccine> findByPetIdAndStatus(Long petId, VaccineStatus status);

    @Query("SELECT v FROM Vaccine v WHERE v.pet.id = :petId AND (v.status = 'PENDING' OR v.status = 'EXPIRING_SOON')")
    List<Vaccine> findPendingByPetId(@Param("petId") Long petId);

    @Query("SELECT v FROM Vaccine v WHERE v.dueDate <= :threshold AND v.status != 'APPLIED'")
    List<Vaccine> findVaccinesDueBefore(@Param("threshold") LocalDate threshold);
}

