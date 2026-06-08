package com.iclinical.technology.appointments.overbooking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OverbookingRepository extends JpaRepository<Overbooking, UUID> {

    @Query("""
        select o
        from Overbooking o, Appointment a
        where o.appointmentId = a.id
          and o.professionalId = :professionalId
          and o.status <> 'deleted'
          and a.startAt < :to
          and a.endAt > :from
        order by a.startAt asc
        """)
    List<Overbooking> findActiveByProfessionalAndRange(
        @Param("professionalId") UUID professionalId,
        @Param("from") Instant from,
        @Param("to") Instant to
    );
}
