package br.com.petos.project.service;

import br.com.petos.project.entity.Alert;
import br.com.petos.project.entity.User;
import br.com.petos.project.enums.AlertType;
import br.com.petos.project.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "petos.email",
        name = "enabled",
        havingValue = "true"
)
public class VaccineEmailNotificationService {

    private static final String EMAILJS_URL =
            "https://api.emailjs.com/api/v1.0/email/send";

    private final AlertRepository alertRepository;
    private final RestClient.Builder restClientBuilder;

    @Value("${petos.emailjs.service-id:}")
    private String serviceId;

    @Value("${petos.emailjs.template-id:}")
    private String templateId;

    @Value("${petos.emailjs.public-key:}")
    private String publicKey;

    @Scheduled(
            cron = "${petos.email.cron:0 0 9 * * *}",
            zone = "America/Sao_Paulo"
    )
    @Transactional
    public void sendPendingVaccinationEmails() {

        if (serviceId.isBlank()
                || templateId.isBlank()
                || publicKey.isBlank()) {

            log.warn("EmailJS não configurado. Verifique as variáveis de ambiente.");
            return;
        }

        List<Alert> pending = alertRepository
                .findVaccinationAlertsPendingEmail(
                        List.of(
                                AlertType.VACCINE_DUE,
                                AlertType.VACCINE_OVERDUE
                        )
                );

        RestClient client = restClientBuilder.build();
        boolean attemptedRequest = false;

        for (Alert alert : pending) {

            if (alert.getPet() == null
                    || alert.getPet().getOwner() == null) {
                continue;
            }

            User tutor = alert.getPet().getOwner();
            String tutorEmail = tutor.getEmail();

            if (tutorEmail == null || tutorEmail.isBlank()) {
                continue;
            }

            if (attemptedRequest) {
                try {
                    Thread.sleep(1100);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    log.warn("Envio de notificações interrompido.");
                    break;
                }
            }

            attemptedRequest = true;

            Map<String, Object> payload = Map.of(
                    "service_id", serviceId,
                    "template_id", templateId,
                    "user_id", publicKey,
                    "template_params", Map.of(
                            "email", tutorEmail,
                            "name", tutor.getName(),
                            "message", alert.getMessage()
                    )
            );

            try {
                client.post()
                        .uri(EMAILJS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(payload)
                        .retrieve()
                        .toBodilessEntity();

                alert.setEmailSentAt(LocalDateTime.now());
                alertRepository.save(alert);

                log.info(
                        "Notificação enviada pelo EmailJS. Alerta: {}",
                        alert.getId()
                );

            } catch (RestClientResponseException exception) {

                log.warn(
                        "Falha no envio do alerta {}. EmailJS HTTP {}",
                        alert.getId(),
                        exception.getStatusCode()
                );

            } catch (RestClientException exception) {

                log.warn(
                        "Falha de comunicação com EmailJS no alerta {}: {}",
                        alert.getId(),
                        exception.getClass().getSimpleName()
                );
            }
        }
    }
}