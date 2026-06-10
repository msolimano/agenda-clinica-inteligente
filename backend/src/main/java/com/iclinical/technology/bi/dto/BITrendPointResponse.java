package com.iclinical.technology.bi.dto;

import java.time.LocalDate;

public record BITrendPointResponse(
    LocalDate date,
    long scheduled,
    long confirmed,
    long cancelled,
    long noShow
) {
}
