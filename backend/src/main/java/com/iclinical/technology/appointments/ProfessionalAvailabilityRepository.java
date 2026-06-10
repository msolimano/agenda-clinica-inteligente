package com.iclinical.technology.appointments;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfessionalAvailabilityRepository extends JpaRepository<ProfessionalAvailability, UUID> {

    List<ProfessionalAvailability> findByProfessionalIdAndStatusOrderByWeekdayAscStartTimeAsc(UUID professionalId, String status);

    List<ProfessionalAvailability> findByProfessionalIdOrderByWeekdayAscStartTimeAsc(UUID professionalId);

    Optional<ProfessionalAvailability> findByIdAndProfessionalId(UUID id, UUID professionalId);
}
