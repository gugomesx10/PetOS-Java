package br.com.petos.project.repository;

import br.com.petos.project.entity.Pet;
import br.com.petos.project.enums.Species;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

    Page<Pet> findByActiveTrue(Pageable pageable);

    Page<Pet> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    Page<Pet> findBySpeciesAndActiveTrue(Species species, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Pet p JOIN p.vaccines v " +
           "WHERE v.dueDate <= CURRENT_DATE OR v.dueDate <= (CURRENT_DATE + 30) " +
           "AND p.active = true")
    List<Pet> findPetsWithVaccinesDueOrExpiringSoon();
}

