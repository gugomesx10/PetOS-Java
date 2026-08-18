package br.com.petos.project.domain;

import br.com.petos.project.enums.AlertType;
import br.com.petos.project.enums.VaccineStatus;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;


public final class VaccinationPolicy {


    public static final int EXPIRY_WARNING_WINDOW_DAYS = 30;

    public static final Set<VaccineStatus> PENDING_STATUSES =
            Set.of(VaccineStatus.PENDING, VaccineStatus.EXPIRING_SOON, VaccineStatus.OVERDUE);

    private VaccinationPolicy() {
    }

    public static VaccineStatus resolveStatus(LocalDate applicationDate, LocalDate dueDate, LocalDate reference) {
        boolean applied = isApplied(applicationDate, reference);

        if (dueDate == null) {
            return applied ? VaccineStatus.APPLIED : VaccineStatus.PENDING;
        }
        if (dueDate.isBefore(reference)) {
            return VaccineStatus.OVERDUE;
        }
        if (isWithinWarningWindow(dueDate, reference)) {
            return VaccineStatus.EXPIRING_SOON;
        }
        return applied ? VaccineStatus.APPLIED : VaccineStatus.PENDING;
    }


    public static boolean isExpiringSoon(VaccineStatus status, LocalDate dueDate, LocalDate reference) {
        if (dueDate == null || status == VaccineStatus.APPLIED) {
            return false;
        }
        return isWithinWarningWindow(dueDate, reference);
    }

    public static Optional<AlertType> preventiveAlertType(VaccineStatus status) {
        return switch (status) {
            case OVERDUE -> Optional.of(AlertType.VACCINE_OVERDUE);
            case EXPIRING_SOON -> Optional.of(AlertType.VACCINE_DUE);
            case PENDING, APPLIED -> Optional.empty();
        };
    }

    public static LocalDate warningThreshold(LocalDate reference) {
        return reference.plusDays(EXPIRY_WARNING_WINDOW_DAYS);
    }

    private static boolean isApplied(LocalDate applicationDate, LocalDate reference) {
        return applicationDate != null && !applicationDate.isAfter(reference);
    }

    private static boolean isWithinWarningWindow(LocalDate dueDate, LocalDate reference) {
        return !dueDate.isAfter(warningThreshold(reference));
    }
}

