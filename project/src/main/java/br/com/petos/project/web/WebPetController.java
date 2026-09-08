package br.com.petos.project.web;

import br.com.petos.project.dto.PetRequestDTO;
import br.com.petos.project.enums.Species;
import br.com.petos.project.service.AuthService;
import br.com.petos.project.service.PetService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class WebPetController {
    private final PetService petService;
    private final AuthService authService;

    @InitBinder("form")
    void fields(WebDataBinder binder) {
        binder.setAllowedFields("name", "species", "breed", "birthDate", "weight", "tutorName", "tutorPhone");
    }

    @ModelAttribute("species")
    Species[] species() { return Species.values(); }

    @GetMapping("/")
    String index() { return "redirect:/web"; }

    @GetMapping({"/web", "/web/"})
    String home(@RequestParam(defaultValue = "") String name, @RequestParam(defaultValue = "0") int page, Model model) {
        var pageable = PageRequest.of(Math.max(0, page), 9, Sort.by("name"));
        model.addAttribute("pets", name.isBlank() ? petService.findAll(pageable) : petService.searchByName(name.trim(), pageable));
        model.addAttribute("name", name);
        model.addAttribute("preventive", petService.findPetsWithVaccinesDueOrExpiringSoon());
        return "home";
    }

    @GetMapping("/web/pets/novo")
    String newPet(Model model) {
        model.addAttribute("form", PetRequestDTO.builder().species(Species.DOG).tutorName(authService.currentUser().getName()).build());
        return "pets/form";
    }

    @PostMapping("/web/pets/novo")
    String create(@Valid @ModelAttribute("form") PetRequestDTO form, BindingResult errors,
                  HttpServletResponse response, RedirectAttributes redirect) {
        if (errors.hasErrors()) { response.setStatus(400); return "pets/form"; }
        var pet = petService.create(form);
        redirect.addFlashAttribute("success", "Caderneta criada. Cada cuidado de " + pet.getName() + " começa aqui.");
        return "redirect:/web/pets/" + pet.getId() + "/vacinas";
    }
}

