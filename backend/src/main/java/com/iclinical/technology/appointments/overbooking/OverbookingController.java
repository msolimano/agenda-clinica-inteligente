package com.iclinical.technology.appointments.overbooking;

import com.iclinical.technology.appointments.overbooking.dto.OverbookingCapacityResponse;
import com.iclinical.technology.appointments.overbooking.dto.OverbookingCreateRequest;
import com.iclinical.technology.appointments.overbooking.dto.OverbookingResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
public class OverbookingController {

    private final OverbookingService overbookingService;

    public OverbookingController(OverbookingService overbookingService) {
        this.overbookingService = overbookingService;
    }

    @GetMapping("/overbooking-capacity")
    public OverbookingCapacityResponse capacity(
        @RequestParam UUID professionalId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startAt,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endAt
    ) {
        return overbookingService.capacity(professionalId, startAt, endAt);
    }

    @PostMapping("/overbookings")
    @ResponseStatus(HttpStatus.CREATED)
    public OverbookingResponse create(@Valid @RequestBody OverbookingCreateRequest request) {
        return overbookingService.create(request);
    }

    @GetMapping("/overbookings")
    public List<OverbookingResponse> list(
        @RequestParam UUID professionalId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to
    ) {
        return overbookingService.list(professionalId, from, to);
    }
}
