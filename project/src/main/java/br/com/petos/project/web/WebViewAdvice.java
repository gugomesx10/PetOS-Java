package br.com.petos.project.web;

import br.com.petos.project.exception.BusinessRuleException;
import br.com.petos.project.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.format.Formatter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDate;
import java.util.Locale;

@ControllerAdvice(basePackageClasses = WebViewAdvice.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebViewAdvice {
    private static final Logger log = LoggerFactory.getLogger(WebViewAdvice.class);

    @InitBinder
    void dates(WebDataBinder binder) {
        binder.addCustomFormatter(new Formatter<LocalDate>() {
            public LocalDate parse(String text, Locale locale) { return LocalDate.parse(text); }
            public String print(LocalDate value, Locale locale) { return value.toString(); }
        });
    }

    @ExceptionHandler(AccessDeniedException.class)
    String forbidden(HttpServletResponse response, Model model) {
        return error(response, model, 403, "Este cuidado pertence a outro perfil ou a outro tutor.");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    String missing(HttpServletResponse response, Model model) {
        return error(response, model, 404, "Registro não encontrado ou pet inativo.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    String invalid(HttpServletResponse response, Model model) {
        return error(response, model, 400, "O endereço ou parâmetro informado é inválido.");
    }

    @ExceptionHandler(BusinessRuleException.class)
    String business(BusinessRuleException exception, HttpServletResponse response, Model model) {
        return error(response, model, 422, exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    String unexpected(Exception exception, HttpServletResponse response, Model model) {
        log.error("Falha ao renderizar página PetOS", exception);
        return error(response, model, 500, "Não foi possível concluir este cuidado. Tente novamente mais tarde.");
    }

    private String error(HttpServletResponse response, Model model, int status, String message) {
        response.setStatus(status);
        model.addAttribute("status", status);
        model.addAttribute("message", message);
        return "error";
    }
}

