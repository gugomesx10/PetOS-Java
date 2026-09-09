package br.com.petos.project.service;

import br.com.petos.project.domain.VaccinationPolicy;
import br.com.petos.project.entity.Alert;
import br.com.petos.project.entity.Vaccine;
import br.com.petos.project.enums.AlertType;
import br.com.petos.project.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VaccineAlertService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AlertRepository alertRepository;

    @Transactional
    public Optional<Alert> synchronizePreventiveAlert(Vaccine vaccine) {
        Optional<Alert> pendingAlert = alertRepository.findByVaccineIdAndSentFalse(vaccine.getId());
        Optional<AlertType> requiredType = VaccinationPolicy.preventiveAlertType(vaccine.getStatus());

        if (requiredType.isEmpty()) {
            pendingAlert.ifPresent(alertRepository::delete);
            return Optional.empty();
        }

        AlertType type = requiredType.get();
        Alert alert = pendingAlert.orElseGet(() -> newAlertFor(vaccine));
        alert.setType(type);
        alert.setMessage(buildMessage(type, vaccine));
        alert.setDueDate(vaccine.getDueDate());
        return Optional.of(alertRepository.save(alert));
    }

    private Alert newAlertFor(Vaccine vaccine) {
        return Alert.builder()
                .pet(vaccine.getPet())
                .vaccine(vaccine)
                .sent(false)
                .build();
    }

    private String buildMessage(AlertType type, Vaccine vaccine) {
        String formattedDueDate = vaccine.getDueDate().format(DATE_FORMAT);
        String petName = vaccine.getPet().getName();
        return switch (type) {
            case VACCINE_OVERDUE -> "Vacina '%s' do pet %s está vencida desde %s. Regularize o quanto antes."
                    .formatted(vaccine.getName(), petName, formattedDueDate);
            case VACCINE_DUE -> "Vacina '%s' do pet %s vence em %s. Agende a dose de reforço."
                    .formatted(vaccine.getName(), petName, formattedDueDate);
            default -> "Vacina '%s' do pet %s requer atenção.".formatted(vaccine.getName(), petName);
        };
    }
}

