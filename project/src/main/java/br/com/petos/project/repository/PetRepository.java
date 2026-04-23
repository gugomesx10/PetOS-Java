package br.com.petos.project.repository;

import br.com.petos.project.entity.Pet;
import br.com.petos.project.enums.Species;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

    Page<Pet> findByActiveTrue(Pageable pageable);

    Page<Pet> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    Page<Pet> findBySpeciesAndActiveTrue(Species species, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Pet p JOIN p.vaccines v " +
           "WHERE (v.dueDate <= :today OR v.dueDate <= :thirtyDaysFromNow) " +
           "AND p.active = true")
    List<Pet> findPetsWithVaccinesDueOrExpiringSoon(
            @Param("today") LocalDate today,
            @Param("thirtyDaysFromNow") LocalDate thirtyDaysFromNow);
}

