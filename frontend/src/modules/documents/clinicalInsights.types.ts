export type ClinicalInsightType =
  | 'clinical_summary'
  | 'clinical_alert'
  | 'diagnosis_candidate'
  | 'medication_candidate'
  | 'allergy_candidate'
  | 'risk_factor_candidate'
  | 'lab_result_candidate'
  | 'observation_candidate';

export type ClinicalInsightStatus = 'pending' | 'accepted' | 'rejected' | 'dismissed';

export interface ClinicalInsight {
  id: string;
  organizationId: string;
  patientId: string;
  clinicalDocumentId: string;
  clinicalRecordId: string | null;
  professionalId: string | null;
  aiAnalysisId: string | null;
  sourceDocumentName: string;
  insightType: ClinicalInsightType;
  title: string;
  description: string;
  sourceText: string | null;
  confidence: number | null;
  status: ClinicalInsightStatus;
  createdAt: string;
  updatedAt: string;
  reviewedAt: string | null;
  reviewedByProfessionalId: string | null;
  reviewNotes: string | null;
  disclaimer: string;
}

export interface ClinicalInsightReviewPayload {
  reviewedByProfessionalId?: string;
  reviewNotes?: string;
}
