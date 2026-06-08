export type ClinicalRecordStatus = 'draft' | 'open' | 'closed';

export interface PatientOption {
  id: string;
  firstName: string;
  lastName: string;
  documentType: string | null;
  documentNumber: string | null;
  status: string;
}

export interface ProfessionalOption {
  id: string;
  firstName: string;
  lastName: string;
  status: string;
}

export interface ClinicalRecordSummary {
  id: string;
  patientId: string;
  patientName: string;
  professionalId: string;
  professionalName: string;
  appointmentId: string | null;
  recordDate: string;
  chiefComplaint: string | null;
  assessment: string | null;
  status: ClinicalRecordStatus;
  createdAt: string;
  updatedAt: string;
}

export interface ClinicalRecord extends ClinicalRecordSummary {
  organizationId: string;
  anamnesis: string | null;
  physicalExam: string | null;
  plan: string | null;
  notes: string | null;
  finalizedAt: string | null;
}

export interface ClinicalRecordPayload {
  patientId: string;
  professionalId: string;
  appointmentId?: string | null;
  recordDate: string;
  chiefComplaint?: string;
  anamnesis?: string;
  physicalExam?: string;
  assessment?: string;
  plan?: string;
  notes?: string;
  status?: ClinicalRecordStatus;
}
