package com.iclinical.technology.appointments.availability;

import com.iclinical.technology.appointments.availability.dto.ProfessionalAvailabilityRequest;
import com.iclinical.technology.appointments.availability.dto.ProfessionalAvailabilityResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/professionals/{professionalId}/availability")
public class ProfessionalAvailabilityController {

    private final ProfessionalAvailabilityService availabilityService;

    public ProfessionalAvailabilityController(ProfessionalAvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping
    public List<ProfessionalAvailabilityResponse> list(@PathVariable UUID professionalId) {
        return availabilityService.list(professionalId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfessionalAvailabilityResponse create(@PathVariable UUID professionalId, @RequestBody ProfessionalAvailabilityRequest request) {
        return availabilityService.create(professionalId, request);
    }

    @PutMapping("/{availabilityId}")
    public ProfessionalAvailabilityResponse update(
        @PathVariable UUID professionalId,
        @PathVariable UUID availabilityId,
        @RequestBody ProfessionalAvailabilityRequest request
    ) {
        return availabilityService.update(professionalId, availabilityId, request);
    }
}
