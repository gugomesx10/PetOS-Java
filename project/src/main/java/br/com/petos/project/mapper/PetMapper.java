package br.com.petos.project.mapper;

import br.com.petos.project.dto.PetRequestDTO;
import br.com.petos.project.dto.PetResponseDTO;
import br.com.petos.project.entity.Pet;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

@Component
public class PetMapper {

    public Pet toEntity(PetRequestDTO dto) {
        return Pet.builder()
                .name(dto.getName())
                .species(dto.getSpecies())
                .breed(dto.getBreed())
                .birthDate(dto.getBirthDate())
                .weight(dto.getWeight())
                .tutorName(dto.getTutorName())
                .tutorPhone(dto.getTutorPhone())
                .active(true)
                .build();
    }

    public PetResponseDTO toResponseDTO(Pet pet) {
        Integer ageInMonths = null;
        if (pet.getBirthDate() != null) {
            Period period = Period.between(pet.getBirthDate(), LocalDate.now());
            ageInMonths = period.getYears() * 12 + period.getMonths();
        }
        return PetResponseDTO.builder()
                .id(pet.getId())
                .name(pet.getName())
                .species(pet.getSpecies())
                .breed(pet.getBreed())
                .birthDate(pet.getBirthDate())
                .weight(pet.getWeight())
                .tutorName(pet.getTutorName())
                .tutorPhone(pet.getTutorPhone())
                .active(pet.getActive())
                .ageInMonths(ageInMonths)
                .build();
    }

    public void updateEntity(Pet pet, PetRequestDTO dto) {
        pet.setName(dto.getName());
        pet.setSpecies(dto.getSpecies());
        pet.setBreed(dto.getBreed());
        pet.setBirthDate(dto.getBirthDate());
        pet.setWeight(dto.getWeight());
        pet.setTutorName(dto.getTutorName());
        pet.setTutorPhone(dto.getTutorPhone());
    }
}

