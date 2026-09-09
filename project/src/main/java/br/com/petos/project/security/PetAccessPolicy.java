package br.com.petos.project.security;

import br.com.petos.project.entity.Pet;
import br.com.petos.project.exception.ResourceNotFoundException;
import br.com.petos.project.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PetAccessPolicy {

    private final PetRepository petRepository;
    private final CurrentUserProvider currentUserProvider;

    public Pet requireReadable(Long petId) {
        Pet pet = requireActivePet(petId);
        AuthenticatedUser user = currentUserProvider.require();
        if (user.isClinica() || pet.isOwnedBy(user.getId())) {
            return pet;
        }
        throw new AccessDeniedException("Pet pertence a outro tutor.");
    }


    public Pet requireOwned(Long petId) {
        Pet pet = requireActivePet(petId);
        AuthenticatedUser user = currentUserProvider.require();
        if (pet.isOwnedBy(user.getId())) {
            return pet;
        }
        throw new AccessDeniedException("Pet pertence a outro tutor.");
    }

    public Pet requireActivePet(Long petId) {
        return petRepository.findByIdAndActiveTrue(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet", petId));
    }
}
