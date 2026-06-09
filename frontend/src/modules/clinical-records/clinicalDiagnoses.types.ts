export type ClinicalDiagnosisStatus = 'suspected' | 'confirmed' | 'resolved' | 'ruled_out';

export interface ClinicalDiagnosis {
  id: string;
  organizationId: string;
  clinicalRecordId: string;
  patientId: string;
  patientName: string;
  professionalId: string;
  professionalName: string;
  diagnosisCatalogId: string | null;
  diagnosisText: string;
  primary: boolean;
  diagnosisStatus: ClinicalDiagnosisStatus;
  observations: string | null;
  codeSystem: string | null;
  diagnosisCode: string | null;
  diagnosisCodeDisplay: string | null;
  status: 'active' | 'deleted';
  createdAt: string;
  updatedAt: string;
}

export interface ClinicalDiagnosisPayload {
  diagnosisCatalogId?: string | null;
  diagnosisText: string;
  primary: boolean;
  diagnosisStatus: ClinicalDiagnosisStatus;
  observations?: string | null;
}
