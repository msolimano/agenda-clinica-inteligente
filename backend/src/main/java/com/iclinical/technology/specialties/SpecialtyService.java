package com.iclinical.technology.specialties;

import com.iclinical.technology.specialties.dto.SpecialtyResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SpecialtyService {

    private static final List<SpecialtySeed> DEFAULT_SPECIALTIES = List.of(
        new SpecialtySeed("Medicina General", "MED-GEN"),
        new SpecialtySeed("Dermatología", "DER"),
        new SpecialtySeed("Cardiología", "CAR"),
        new SpecialtySeed("Traumatología", "TRA"),
        new SpecialtySeed("Pediatría", "PED"),
        new SpecialtySeed("Ginecología", "GIN")
    );

    private final SpecialtyRepository specialtyRepository;

    public SpecialtyService(SpecialtyRepository specialtyRepository) {
        this.specialtyRepository = specialtyRepository;
    }

    @Transactional(readOnly = true)
    public List<SpecialtyResponse> listActive() {
        return specialtyRepository.findByStatusOrderByNameAsc("active")
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public void ensureDefaultCatalog() {
        for (var seed : DEFAULT_SPECIALTIES) {
            specialtyRepository.findByNameIgnoreCaseAndStatusNot(seed.name(), "deleted")
                .orElseGet(() -> specialtyRepository.save(new Specialty(seed.name(), seed.code())));
        }
    }

    public SpecialtyResponse toResponse(Specialty specialty) {
        return new SpecialtyResponse(
            specialty.getId(),
            specialty.getOrganizationId(),
            specialty.getName(),
            specialty.getCode(),
            specialty.getStatus()
        );
    }

    private record SpecialtySeed(String name, String code) {
    }
}
