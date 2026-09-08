package br.com.petos.project.web;

import br.com.petos.project.dto.PetResponseDTO;
import br.com.petos.project.dto.VaccineRequestDTO;
import br.com.petos.project.dto.VaccineResponseDTO;
import br.com.petos.project.enums.AlertType;
import br.com.petos.project.exception.BusinessRuleException;
import br.com.petos.project.exception.ResourceNotFoundException;
import br.com.petos.project.service.AlertService;
import br.com.petos.project.service.PetService;
import br.com.petos.project.service.VaccineService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web/pets/{petId}/vacinas")
@RequiredArgsConstructor
public class WebVaccinationController {
    private final PetService petService;
    private final VaccineService vaccineService;
    private final AlertService alertService;

    @InitBinder("form")
    void fields(WebDataBinder binder) { binder.setAllowedFields("name", "applicationDate", "dueDate"); }

    @ModelAttribute("pet")
    PetResponseDTO pet(@PathVariable Long petId) { return petService.findById(petId); }

    @ModelAttribute("form")
    VaccineRequestDTO form(@PathVariable Long petId) { return VaccineRequestDTO.builder().petId(petId).build(); }

    @GetMapping
    String overview(@PathVariable Long petId, Model model) {
        model.addAttribute("vaccines", vaccineService.findByPetId(petId));
        model.addAttribute("alerts", alertService.findPendingByPetId(petId).stream()
                .filter(alert -> alert.getType() == AlertType.VACCINE_DUE || alert.getType() == AlertType.VACCINE_OVERDUE).toList());
        return "vacinacao/index";
    }

    @GetMapping("/nova")
    String createForm() { return "vacinacao/form"; }

    @GetMapping("/{vaccineId}/editar")
    String editForm(@PathVariable Long petId, @PathVariable Long vaccineId, Model model) {
        var vaccine = requirePetVaccine(petId, vaccineId);
        model.addAttribute("form", VaccineRequestDTO.builder().petId(petId).name(vaccine.getName())
                .applicationDate(vaccine.getApplicationDate()).dueDate(vaccine.getDueDate()).build());
        return "vacinacao/form";
    }

    @PostMapping({"/nova", "/{vaccineId}/editar"})
    String save(@PathVariable Long petId, @PathVariable(required = false) Long vaccineId,
                @Valid @ModelAttribute("form") VaccineRequestDTO form, BindingResult errors,
                HttpServletResponse response, RedirectAttributes redirect) {
        if (vaccineId != null) requirePetVaccine(petId, vaccineId);
        if (errors.hasErrors()) { response.setStatus(400); return "vacinacao/form"; }
        VaccineResponseDTO vaccine;
        try {
            vaccine = vaccineId == null ? vaccineService.create(form) : vaccineService.update(vaccineId, form);
        } catch (BusinessRuleException exception) {
            errors.reject("vaccination", exception.getMessage());
            response.setStatus(422);
            return "vacinacao/form";
        }
        redirect.addFlashAttribute("success", "Vacina salva. Situação: " + ViewLabels.label(vaccine.getStatus())
                + ". A avaliação preventiva foi processada e os alertas foram sincronizados.");
        return "redirect:/web/pets/" + petId + "/vacinas";
    }

    @PostMapping("/{vaccineId}/excluir")
    String delete(@PathVariable Long petId, @PathVariable Long vaccineId, RedirectAttributes redirect) {
        requirePetVaccine(petId, vaccineId);
        vaccineService.delete(vaccineId);
        redirect.addFlashAttribute("success", "Registro de vacina excluído. Caderneta atualizada.");
        return "redirect:/web/pets/" + petId + "/vacinas";
    }

    private VaccineResponseDTO requirePetVaccine(Long petId, Long vaccineId) {
        var vaccine = vaccineService.findById(vaccineId);
        if (!petId.equals(vaccine.getPetId())) throw new ResourceNotFoundException("Vacina do pet", vaccineId);
        return vaccine;
    }
}

