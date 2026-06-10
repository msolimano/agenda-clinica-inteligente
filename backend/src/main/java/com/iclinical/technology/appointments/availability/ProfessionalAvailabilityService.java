package com.iclinical.technology.appointments.availability;

import com.iclinical.technology.appointments.AppointmentNotFoundException;
import com.iclinical.technology.appointments.AppointmentValidationException;
import com.iclinical.technology.appointments.ProfessionalAvailability;
import com.iclinical.technology.appointments.ProfessionalAvailabilityRepository;
import com.iclinical.technology.appointments.availability.dto.ProfessionalAvailabilityRequest;
import com.iclinical.technology.appointments.availability.dto.ProfessionalAvailabilityResponse;
import com.iclinical.technology.professionals.Professional;
import com.iclinical.technology.professionals.ProfessionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ProfessionalAvailabilityService {

    private static final Set<String> STATUSES = Set.of("active", "inactive", "deleted");

    private final ProfessionalAvailabilityRepository availabilityRepository;
    private final ProfessionalRepository professionalRepository;

    public ProfessionalAvailabilityService(ProfessionalAvailabilityRepository availabilityRepository, ProfessionalRepository professionalRepository) {
        this.availabilityRepository = availabilityRepository;
        this.professionalRepository = professionalRepository;
    }

    @Transactional(readOnly = true)
    public List<ProfessionalAvailabilityResponse> list(UUID professionalId) {
        findActiveProfessional(professionalId);
        return availabilityRepository.findByProfessionalIdOrderByWeekdayAscStartTimeAsc(professionalId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public ProfessionalAvailabilityResponse create(UUID professionalId, ProfessionalAvailabilityRequest request) {
        var professional = findActiveProfessional(professionalId);
        var availability = new ProfessionalAvailability();
        availability.setOrganizationId(professional.getOrganizationId());
        availability.setProfessionalId(professional.getId());
        applyFields(availability, request, null);
        return toResponse(availabilityRepository.save(availability));
    }

    @Transactional
    public ProfessionalAvailabilityResponse update(UUID professionalId, UUID availabilityId, ProfessionalAvailabilityRequest request) {
        findActiveProfessional(professionalId);
        var availability = availabilityRepository.findByIdAndProfessionalId(availabilityId, professionalId)
            .orElseThrow(() -> new AppointmentNotFoundException("Disponibilidad profesional no encontrada"));
        applyFields(availability, request, availability);
        return toResponse(availability);
    }

    private Professional findActiveProfessional(UUID professionalId) {
        if (professionalId == null) {
            throw new AppointmentValidationException("Debe indicar el profesional");
        }
        return professionalRepository.findById(professionalId)
            .filter(professional -> "active".equals(professional.getStatus()))
            .orElseThrow(() -> new AppointmentValidationException("El profesional indicado no existe o no esta activo"));
    }

    private void applyFields(ProfessionalAvailability availability, ProfessionalAvailabilityRequest request, ProfessionalAvailability current) {
        if (request == null) {
            throw new AppointmentValidationException("Debe indicar la disponibilidad profesional");
        }

        var weekday = request.weekday() == null ? current == null ? null : current.getWeekday() : request.weekday();
        var startTime = request.startTime() == null ? current == null ? null : current.getStartTime() : request.startTime();
        var endTime = request.endTime() == null ? current == null ? null : current.getEndTime() : request.endTime();
        var slotMinutes = request.slotMinutes() == null ? current == null ? null : current.getSlotMinutes() : request.slotMinutes();
        var status = StringUtils.hasText(request.status()) ? request.status().trim() : current == null ? "active" : current.getStatus();
        var allowsOverbooking = request.allowsOverbooking() == null ? current != null && current.isAllowsOverbooking() : request.allowsOverbooking();
        var maxOverbookings = request.maxOverbookings() == null ? current == null ? 0 : current.getMaxOverbookings() : request.maxOverbookings();
        var validFrom = resolveDate(request.validFrom(), current == null ? null : current.getValidFrom());
        var validTo = resolveDate(request.validTo(), current == null ? null : current.getValidTo());

        validate(weekday, startTime, endTime, slotMinutes, validFrom, validTo, status, allowsOverbooking, maxOverbookings);

        availability.setWeekday(weekday);
        availability.setStartTime(startTime);
        availability.setEndTime(endTime);
        availability.setSlotMinutes(slotMinutes);
        availability.setLocation(clean(request.location(), current == null ? null : current.getLocation()));
        availability.setValidFrom(validFrom);
        availability.setValidTo(validTo);
        availability.setStatus(status);
        availability.setAllowsOverbooking(allowsOverbooking);
        availability.setMaxOverbookings(maxOverbookings);
    }

    private void validate(
        Short weekday,
        LocalTime startTime,
        LocalTime endTime,
        Integer slotMinutes,
        LocalDate validFrom,
        LocalDate validTo,
        String status,
        boolean allowsOverbooking,
        int maxOverbookings
    ) {
        if (weekday == null || weekday < 1 || weekday > 7) {
            throw new AppointmentValidationException("El dia de semana debe estar entre 1 y 7");
        }
        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            throw new AppointmentValidationException("La hora de inicio debe ser menor que la hora de termino");
        }
        if (slotMinutes == null || slotMinutes <= 0) {
            throw new AppointmentValidationException("La duracion del cupo debe ser mayor a 0 minutos");
        }
        if (validFrom != null && validTo != null && validFrom.isAfter(validTo)) {
            throw new AppointmentValidationException("El rango de vigencia de la disponibilidad no es valido");
        }
        if (!STATUSES.contains(status)) {
            throw new AppointmentValidationException("Estado de disponibilidad permitido: active, inactive o deleted");
        }
        if (maxOverbookings < 0) {
            throw new AppointmentValidationException("El maximo de sobrecupos debe ser mayor o igual a 0");
        }
        if (allowsOverbooking && maxOverbookings <= 0) {
            throw new AppointmentValidationException("Si permite sobrecupos, el maximo de sobrecupos debe ser mayor a 0");
        }
    }

    private String clean(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        var cleaned = value.trim();
        return StringUtils.hasText(cleaned) ? cleaned : null;
    }

    private LocalDate resolveDate(LocalDate value, LocalDate fallback) {
        return value == null ? fallback : value;
    }

    private ProfessionalAvailabilityResponse toResponse(ProfessionalAvailability availability) {
        return new ProfessionalAvailabilityResponse(
            availability.getId(),
            availability.getOrganizationId(),
            availability.getProfessionalId(),
            availability.getWeekday(),
            availability.getStartTime(),
            availability.getEndTime(),
            availability.getSlotMinutes(),
            availability.getLocation(),
            availability.getValidFrom(),
            availability.getValidTo(),
            availability.getStatus(),
            availability.isAllowsOverbooking(),
            availability.getMaxOverbookings(),
            availability.getCreatedAt(),
            availability.getUpdatedAt()
        );
    }
}
