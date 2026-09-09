package br.com.petos.project.web;

import br.com.petos.project.dto.RegisterRequestDTO;
import br.com.petos.project.enums.Role;
import br.com.petos.project.exception.BusinessRuleException;
import br.com.petos.project.service.AuthService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class WebAuthController {
    private final AuthService authService;

    @InitBinder("form")
    void fields(WebDataBinder binder) { binder.setAllowedFields("name", "email", "password", "role"); }

    @GetMapping("/login")
    String login() { return "login"; }

    @GetMapping("/cadastro")
    String register(Model model) {
        model.addAttribute("form", RegisterRequestDTO.builder().role(Role.TUTOR).build());
        return "cadastro";
    }

    @PostMapping("/cadastro")
    String register(@Valid @ModelAttribute("form") RegisterRequestDTO form, BindingResult errors,
                    HttpServletResponse response, RedirectAttributes redirect) {
        if (errors.hasErrors()) { response.setStatus(400); return "cadastro"; }
        try {
            authService.register(form);
        } catch (BusinessRuleException exception) {
            errors.reject("registration", exception.getMessage());
            response.setStatus(422);
            return "cadastro";
        }
        redirect.addFlashAttribute("success", "Conta criada. Entre para abrir sua caderneta de cuidados.");
        return "redirect:/login";
    }

    @GetMapping("/web/acesso-negado")
    String denied(Model model, HttpServletResponse response) {
        response.setStatus(403);
        model.addAttribute("status", 403);
        model.addAttribute("message", "Seu perfil não permite esta ação ou a sessão do formulário expirou. Volte à página e tente novamente.");
        return "error";
    }
}

