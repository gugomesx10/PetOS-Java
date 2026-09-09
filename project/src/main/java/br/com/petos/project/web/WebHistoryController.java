package br.com.petos.project.web;

import br.com.petos.project.dto.PetResponseDTO;
import br.com.petos.project.dto.RoutineRequestDTO;
import br.com.petos.project.enums.RoutineType;
import br.com.petos.project.service.PetHistoryService;
import br.com.petos.project.service.PetService;
import br.com.petos.project.service.RoutineRecordService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;

@Controller
@RequestMapping("/web/pets/{petId}")
@RequiredArgsConstructor
public class WebHistoryController {
    private final PetService petService;
    private final PetHistoryService historyService;
    private final RoutineRecordService routineService;

    @InitBinder("form")
    void fields(WebDataBinder binder) { binder.setAllowedFields("type", "description", "recordDate"); }

    @ModelAttribute("pet")
    PetResponseDTO pet(@PathVariable Long petId) { return petService.findById(petId); }
    @ModelAttribute("form")
    RoutineRequestDTO form(@PathVariable Long petId) {
        return RoutineRequestDTO.builder().petId(petId).type(RoutineType.WALK).recordDate(LocalDate.now()).build();
    }
    @ModelAttribute("types")
    RoutineType[] types() { return RoutineType.values(); }

    @GetMapping("/historico")
    String history(@PathVariable Long petId, @RequestParam(defaultValue = "Tudo") String category, Model model) {
        var history = historyService.getHistory(petId);
        model.addAttribute("history", history);
        model.addAttribute("category", category);
        model.addAttribute("entries", HealthTimeline.from(history).stream()
                .filter(entry -> category.equals("Tudo") || category.equals(entry.category())).toList());
        return "historico/index";
    }

    @GetMapping("/rotinas/nova")
    String routine() { return "historico/form"; }

    @PostMapping("/rotinas/nova")
    String save(@PathVariable Long petId, @Valid @ModelAttribute("form") RoutineRequestDTO form,
                BindingResult errors, HttpServletResponse response, RedirectAttributes redirect) {
        if (errors.hasErrors()) { response.setStatus(400); return "historico/form"; }
        routineService.create(form);
        redirect.addFlashAttribute("success", "Cuidado registrado. O histórico consolidado já inclui esta nova etapa.");
        return "redirect:/web/pets/" + petId + "/historico";
    }
}

