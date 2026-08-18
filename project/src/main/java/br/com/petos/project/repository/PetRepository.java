package br.com.petos.project.repository;

import br.com.petos.project.entity.Pet;
import br.com.petos.project.enums.Species;
import br.com.petos.project.enums.VaccineStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

    Optional<Pet> findByIdAndActiveTrue(Long id);

    Page<Pet> findByActiveTrue(Pageable pageable);

    Page<Pet> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    Page<Pet> findBySpeciesAndActiveTrue(Species species, Pageable pageable);

    Page<Pet> findByOwnerIdAndActiveTrue(Long ownerId, Pageable pageable);

    Page<Pet> findByOwnerIdAndNameContainingIgnoreCaseAndActiveTrue(Long ownerId, String name, Pageable pageable);

    Page<Pet> findByOwnerIdAndSpeciesAndActiveTrue(Long ownerId, Species species, Pageable pageable);

    @Query("""
            SELECT DISTINCT p FROM Pet p
            JOIN p.vaccines v
            WHERE p.active = true
              AND v.dueDate IS NOT NULL
              AND v.dueDate <= :threshold
              AND v.status <> :resolvedStatus
            """)
    List<Pet> findActivePetsWithVaccinesDueUntil(@Param("threshold") LocalDate threshold,
                                                 @Param("resolvedStatus") VaccineStatus resolvedStatus);


    @Query("""
            SELECT DISTINCT p FROM Pet p
            JOIN p.vaccines v
            WHERE p.active = true
              AND p.owner.id = :ownerId
              AND v.dueDate IS NOT NULL
              AND v.dueDate <= :threshold
              AND v.status <> :resolvedStatus
            """)
    List<Pet> findActivePetsByOwnerWithVaccinesDueUntil(@Param("ownerId") Long ownerId,
                                                        @Param("threshold") LocalDate threshold,
                                                        @Param("resolvedStatus") VaccineStatus resolvedStatus);
}
