package br.com.petos.project.service;

import br.com.petos.project.entity.Alert;
import br.com.petos.project.enums.AlertType;
import br.com.petos.project.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "petos.email",
        name = "enabled",
        havingValue = "true"
)
public class VaccineEmailNotificationService {

    private final AlertRepository alertRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String senderAddress;

    @Scheduled(
            cron = "${petos.email.cron:0 0 9 * * *}",
            zone = "America/Sao_Paulo"
    )
    @Transactional
    public void sendPendingVaccinationEmails() {

        if (senderAddress == null || senderAddress.isBlank()) {
            log.warn("Remetente de e-mail do PetOS não configurado.");
            return;
        }

        List<Alert> pending = alertRepository
                .findVaccinationAlertsPendingEmail(
                        List.of(
                                AlertType.VACCINE_DUE,
                                AlertType.VACCINE_OVERDUE
                        )
                );

        for (Alert alert : pending) {

            if (alert.getPet().getOwner() == null) {
                continue;
            }

            String tutorEmail = alert.getPet()
                    .getOwner()
                    .getEmail();

            if (tutorEmail == null || tutorEmail.isBlank()) {
                log.warn(
                        "Alerta {} sem destinatário válido.",
                        alert.getId()
                );
                continue;
            }

            if (tutorEmail.trim()
                    .toLowerCase(Locale.ROOT)
                    .endsWith("@petos.local")) {
                continue;
            }

            SimpleMailMessage email = new SimpleMailMessage();

            email.setFrom(senderAddress);
            email.setTo(tutorEmail.trim());

            email.setSubject(
                    "PetOS | Lembrete de vacinação"
            );

            email.setText(
                    "Olá, " +
                            alert.getPet().getOwner().getName() +
                            "!\n\n" +

                            alert.getMessage() +

                            "\n\n" +
                            "Confira a caderneta de vacinação no PetOS." +

                            "\n\n" +
                            "Equipe PetOS"
            );

            try {
                mailSender.send(email);

                alert.setEmailSentAt(LocalDateTime.now());
                alertRepository.save(alert);

                log.info(
                        "E-mail de vacinação enviado. Alerta ID: {}",
                        alert.getId()
                );

            } catch (MailException exception) {
                log.warn(
                        "Falha no envio do alerta {}: {}",
                        alert.getId(),
                        exception.getClass().getSimpleName()
                );
            }
        }
    }
}