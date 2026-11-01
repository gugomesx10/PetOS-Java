package br.com.petos.project.service;

import br.com.petos.project.domain.VaccinationPolicy;
import br.com.petos.project.entity.Vaccine;
import br.com.petos.project.enums.VaccineStatus;
import br.com.petos.project.repository.VaccineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class VaccineStatusRefreshService {

    private final VaccineRepository vaccineRepository;
    private final VaccineAlertService vaccineAlertService;

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 5 0 * * *")
    @Transactional
    public void refreshStatuses() {
        LocalDate today = LocalDate.now();

        LocalDate threshold =
                VaccinationPolicy.warningThreshold(today);

        for (Vaccine vaccine :
                vaccineRepository.findByPetActiveTrueAndDueDateLessThanEqual(
                        threshold
                )) {

            VaccineStatus previousStatus = vaccine.getStatus();

            vaccine.refreshStatus(today);

            if (previousStatus != vaccine.getStatus()) {
                vaccineAlertService.synchronizePreventiveAlert(vaccine);
            }
        }
    }
}