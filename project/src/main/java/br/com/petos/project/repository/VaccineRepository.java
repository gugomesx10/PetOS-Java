package br.com.petos.project.repository;

import br.com.petos.project.entity.Vaccine;
import br.com.petos.project.enums.VaccineStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface VaccineRepository extends JpaRepository<Vaccine, Long> {

    List<Vaccine> findByPetIdOrderByDueDateAsc(Long petId);

    List<Vaccine> findByPetIdAndStatusInOrderByDueDateAsc(Long petId, Collection<VaccineStatus> statuses);

    Page<Vaccine> findByPetActiveTrue(Pageable pageable);

    Page<Vaccine> findByPetOwnerIdAndPetActiveTrue(Long ownerId, Pageable pageable);
}
