package br.com.petos.project.web;

import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

@Component("view")
public class ViewLabels {
    public static String label(Enum<?> value) {
        if (value == null) return "Não informado";
        return switch (value.name()) {
            case "DOG" -> "Cachorro";
            case "CAT" -> "Gato";
            case "BIRD" -> "Ave";
            case "RABBIT" -> "Coelho";
            case "HAMSTER" -> "Hamster";
            case "FISH" -> "Peixe";
            case "REPTILE" -> "Réptil";
            case "TUTOR" -> "Tutor";
            case "CLINICA" -> "Clínica veterinária";
            case "PENDING" -> "Pendente";
            case "APPLIED" -> "Aplicada";
            case "EXPIRING_SOON" -> "Vence em breve";
            case "OVERDUE" -> "Atrasada";
            case "VACCINE_DUE" -> "Vacina próxima do vencimento";
            case "VACCINE_OVERDUE" -> "Vacina atrasada";
            case "ROUTINE_REMINDER" -> "Lembrete de cuidado";
            case "HEALTH_CHECK" -> "Avaliação de saúde";
            case "BIRTHDAY" -> "Aniversário";
            case "FEEDING" -> "Alimentação";
            case "BATHING" -> "Banho";
            case "WALK" -> "Passeio";
            case "MEDICATION" -> "Medicação";
            case "VET_VISIT" -> "Consulta veterinária";
            case "GROOMING" -> "Higiene e tosa";
            case "EXERCISE" -> "Exercício";
            default -> "Outro";
        };
    }
    public String date(TemporalAccessor value) {
        return value == null ? "Sem data definida" : DateTimeFormatter.ofPattern("dd/MM/yyyy").format(value);
    }
    public LocalDate today() { return LocalDate.now(); }
}

