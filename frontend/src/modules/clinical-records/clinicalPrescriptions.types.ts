export type ClinicalPrescriptionStatus = 'draft' | 'active' | 'suspended' | 'completed' | 'cancelled';

export interface ClinicalPrescription {
  id: string;
  organizationId: string;
  clinicalRecordId: string;
  patientId: string;
  patientName: string;
  professionalId: string;
  professionalName: string;
  diagnosisId: string | null;
  diagnosisText: string | null;
  medicationCatalogId: string | null;
  medicationCode: string | null;
  medicationCodeSystem: string | null;
  medicationCodeDisplay: string | null;
  medicationName: string;
  dosage: string;
  frequency: string;
  duration: string | null;
  route: string | null;
  patientInstructions: string | null;
  clinicalNotes: string | null;
  prescriptionStatus: ClinicalPrescriptionStatus;
  status: 'active' | 'deleted';
  createdAt: string;
  updatedAt: string;
}

export interface ClinicalPrescriptionPayload {
  diagnosisId?: string | null;
  medicationCatalogId?: string | null;
  medicationName: string;
  dosage: string;
  frequency: string;
  duration?: string | null;
  route?: string | null;
  patientInstructions?: string | null;
  clinicalNotes?: string | null;
  prescriptionStatus?: ClinicalPrescriptionStatus;
}
