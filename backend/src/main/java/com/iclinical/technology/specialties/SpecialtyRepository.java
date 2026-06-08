package com.iclinical.technology.specialties;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpecialtyRepository extends JpaRepository<Specialty, UUID> {

    List<Specialty> findByStatusOrderByNameAsc(String status);

    Optional<Specialty> findByNameIgnoreCaseAndStatusNot(String name, String status);
}
