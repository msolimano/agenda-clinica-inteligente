export type ClinicalDocumentType =
  | 'medical_report'
  | 'laboratory_exam'
  | 'imaging_exam'
  | 'prescription'
  | 'medical_order'
  | 'certificate'
  | 'other';

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

export interface ClinicalDocumentSummary {
  id: string;
  patientId: string;
  patientName: string;
  professionalId: string | null;
  professionalName: string | null;
  documentType: ClinicalDocumentType;
  title: string;
  fileName: string;
  mimeType: string;
  fileSize: number;
  status: string;
  createdAt: string;
}

export interface ClinicalDocument extends ClinicalDocumentSummary {
  organizationId: string;
  description: string | null;
  storagePath: string;
  checksumSha256: string | null;
  aiAnalysisStatus: string;
  updatedAt: string;
}

export interface ClinicalDocumentUploadPayload {
  patientId: string;
  professionalId?: string;
  documentType: ClinicalDocumentType;
  title: string;
  description?: string;
  file: File;
}
