package br.com.petos.project.repository;

import br.com.petos.project.entity.RoutineRecord;
import br.com.petos.project.enums.RoutineType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoutineRecordRepository extends JpaRepository<RoutineRecord, Long> {

    List<RoutineRecord> findByPetIdOrderByRecordDateDesc(Long petId);

    List<RoutineRecord> findByPetIdAndType(Long petId, RoutineType type);
}

