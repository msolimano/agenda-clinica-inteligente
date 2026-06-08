package com.iclinical.technology.specialties;

import com.iclinical.technology.specialties.dto.SpecialtyResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/specialties")
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    public SpecialtyController(SpecialtyService specialtyService) {
        this.specialtyService = specialtyService;
    }

    @GetMapping
    public List<SpecialtyResponse> listSpecialties() {
        specialtyService.ensureDefaultCatalog();
        return specialtyService.listActive();
    }
}
