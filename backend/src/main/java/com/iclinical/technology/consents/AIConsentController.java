package com.iclinical.technology.consents;

import com.iclinical.technology.consents.dto.AIConsentAcceptRequest;
import com.iclinical.technology.consents.dto.AIConsentResponse;
import com.iclinical.technology.consents.dto.AIConsentRevokeRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/patients/{patientId}/ai-consent")
public class AIConsentController {

    private final AIConsentService consentService;

    public AIConsentController(AIConsentService consentService) {
        this.consentService = consentService;
    }

    @GetMapping
    public AIConsentResponse getCurrent(@PathVariable UUID patientId) {
        return consentService.getCurrent(patientId);
    }

    @PostMapping
    public AIConsentResponse accept(@PathVariable UUID patientId, @RequestBody(required = false) AIConsentAcceptRequest request) {
        return consentService.accept(patientId, request);
    }

    @PatchMapping("/revoke")
    public AIConsentResponse revoke(@PathVariable UUID patientId, @RequestBody(required = false) AIConsentRevokeRequest request) {
        return consentService.revoke(patientId, request);
    }
}
