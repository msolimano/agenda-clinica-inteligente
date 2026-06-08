package com.iclinical.technology.professionals;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProfessionalRepository extends JpaRepository<Professional, UUID> {

    List<Professional> findByStatusNotOrderByLastNameAscFirstNameAsc(String status);

    @Query("""
        select count(p) > 0
        from Professional p
        where p.organizationId = :organizationId
          and p.status <> 'deleted'
          and lower(p.documentType) = lower(:documentType)
          and lower(p.documentNumber) = lower(:documentNumber)
          and (:excludedId is null or p.id <> :excludedId)
        """)
    boolean existsDuplicateDocument(@Param("organizationId") UUID organizationId, @Param("documentType") String documentType, @Param("documentNumber") String documentNumber, @Param("excludedId") UUID excludedId);

    @Query("""
        select count(p) > 0
        from Professional p
        where p.organizationId = :organizationId
          and p.status <> 'deleted'
          and lower(p.registryNumber) = lower(:registryNumber)
          and (:excludedId is null or p.id <> :excludedId)
        """)
    boolean existsDuplicateRegistry(@Param("organizationId") UUID organizationId, @Param("registryNumber") String registryNumber, @Param("excludedId") UUID excludedId);
}
