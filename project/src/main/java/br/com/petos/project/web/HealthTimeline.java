package br.com.petos.project.web;

import br.com.petos.project.dto.PetHistoryDTO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class HealthTimeline {
    private HealthTimeline() { }
    public record Entry(String category, LocalDateTime date, String title, String description) { }

    public static List<Entry> from(PetHistoryDTO history) {
        List<Entry> entries = new ArrayList<>();
        history.getVaccines().forEach(vaccine -> {
            var date = vaccine.getApplicationDate() != null ? vaccine.getApplicationDate() : vaccine.getDueDate();
            entries.add(new Entry("Vacinas", date == null ? null : date.atStartOfDay(), vaccine.getName(),
                    ViewLabels.label(vaccine.getStatus()) + (vaccine.getApplicationDate() != null ? " · Data da aplicação" : " · Data prevista / vencimento")));
        });
        history.getRoutines().forEach(routine -> entries.add(new Entry("Rotinas", routine.getRecordDate().atStartOfDay(),
                ViewLabels.label(routine.getType()), routine.getDescription())));
        history.getAlerts().forEach(alert -> entries.add(new Entry("Alertas", alert.getCreatedAt(),
                ViewLabels.label(alert.getType()), alert.getMessage() + (Boolean.TRUE.equals(alert.getSent()) ? " · Enviado" : " · Pendente") + " · Data de criação")));
        entries.sort(Comparator.comparing(Entry::date, Comparator.nullsLast(Comparator.reverseOrder())));
        return List.copyOf(entries);
    }
}

