package com.iclinical.technology.bi.dto;

import java.util.List;

public record BIDocumentKpiResponse(
    long clinicalDocumentsTotal,
    List<BIBarItemResponse> documentsByType,
    long deletedDocuments
) {
}
