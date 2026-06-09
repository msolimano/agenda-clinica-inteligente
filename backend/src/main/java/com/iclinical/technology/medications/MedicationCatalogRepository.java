package com.iclinical.technology.medications;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MedicationCatalogRepository extends JpaRepository<MedicationCatalog, UUID> {

    @Query("""
        select m
        from MedicationCatalog m
        where m.status <> 'deleted'
          and (:includeInactive = true or m.status = 'active')
          and (:organizationId is null or m.organizationId is null or m.organizationId = :organizationId)
          and (:search is null
            or lower(m.medicationName) like concat('%', :search, '%')
            or lower(coalesce(m.activeIngredient, '')) like concat('%', :search, '%')
            or lower(coalesce(m.medicationCode, '')) like concat('%', :search, '%'))
        order by m.medicationName asc, m.activeIngredient asc, m.strength asc
        """)
    List<MedicationCatalog> findFiltered(
        @Param("organizationId") UUID organizationId,
        @Param("search") String search,
        @Param("includeInactive") boolean includeInactive
    );
}
