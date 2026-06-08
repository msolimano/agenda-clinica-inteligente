package com.iclinical.technology.appointments;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProfessionalAvailabilityRepository extends JpaRepository<ProfessionalAvailability, UUID> {

    List<ProfessionalAvailability> findByProfessionalIdAndStatusOrderByWeekdayAscStartTimeAsc(UUID professionalId, String status);
}
