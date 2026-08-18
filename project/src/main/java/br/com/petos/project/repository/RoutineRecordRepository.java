package br.com.petos.project.repository;

import br.com.petos.project.entity.RoutineRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoutineRecordRepository extends JpaRepository<RoutineRecord, Long> {

    List<RoutineRecord> findByPetIdOrderByRecordDateDesc(Long petId);

    Page<RoutineRecord> findByPetActiveTrue(Pageable pageable);

    Page<RoutineRecord> findByPetOwnerIdAndPetActiveTrue(Long ownerId, Pageable pageable);

}
