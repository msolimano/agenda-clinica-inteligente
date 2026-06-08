package com.iclinical.technology.professionals;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfessionalSpecialtyRepository extends JpaRepository<ProfessionalSpecialty, UUID> {

    Optional<ProfessionalSpecialty> findByProfessionalIdAndSpecialtyId(UUID professionalId, UUID specialtyId);

    List<ProfessionalSpecialty> findByProfessionalIdAndStatus(UUID professionalId, String status);
}
