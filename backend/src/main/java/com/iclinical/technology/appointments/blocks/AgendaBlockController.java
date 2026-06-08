package com.iclinical.technology.appointments.blocks;

import com.iclinical.technology.appointments.blocks.dto.AffectedAppointmentResponse;
import com.iclinical.technology.appointments.blocks.dto.AgendaBlockCancelRequest;
import com.iclinical.technology.appointments.blocks.dto.AgendaBlockCreateRequest;
import com.iclinical.technology.appointments.blocks.dto.AgendaBlockPreviewRequest;
import com.iclinical.technology.appointments.blocks.dto.AgendaBlockPreviewResponse;
import com.iclinical.technology.appointments.blocks.dto.AgendaBlockResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
@RequestMapping("/api/appointments/blocks")
public class AgendaBlockController {

    private final AgendaBlockService agendaBlockService;

    public AgendaBlockController(AgendaBlockService agendaBlockService) {
        this.agendaBlockService = agendaBlockService;
    }

    @PostMapping("/preview")
    public AgendaBlockPreviewResponse preview(@Valid @RequestBody AgendaBlockPreviewRequest request) {
        return agendaBlockService.preview(request);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgendaBlockResponse create(@Valid @RequestBody AgendaBlockCreateRequest request) {
        return agendaBlockService.create(request);
    }

    @GetMapping
    public List<AgendaBlockResponse> list(
        @RequestParam UUID professionalId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to
    ) {
        return agendaBlockService.list(professionalId, from, to);
    }

    @GetMapping("/{id}/affected")
    public List<AffectedAppointmentResponse> affected(@PathVariable UUID id) {
        return agendaBlockService.affected(id);
    }

    @PatchMapping("/{id}/cancel")
    public AgendaBlockResponse cancel(@PathVariable UUID id, @RequestBody(required = false) AgendaBlockCancelRequest request) {
        return agendaBlockService.cancel(id, request);
    }
}
