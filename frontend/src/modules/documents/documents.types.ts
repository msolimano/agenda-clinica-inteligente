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

export type AIAnalysisStatus = 'pending' | 'processing' | 'completed' | 'failed' | 'reviewed' | 'rejected' | 'deleted';
export type AIAnalysisDocumentStatus = AIAnalysisStatus | 'not_requested';

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
  aiAnalysisStatus: AIAnalysisDocumentStatus;
  createdAt: string;
}

export interface ClinicalDocument extends ClinicalDocumentSummary {
  organizationId: string;
  description: string | null;
  storagePath: string;
  checksumSha256: string | null;
  aiAnalysisStatus: AIAnalysisDocumentStatus;
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


export interface AIAnalysisSummary {
  id: string;
  patientId: string;
  clinicalDocumentId: string;
  status: AIAnalysisStatus;
  modelName: string | null;
  clinicalSummary: string | null;
  errorMessage: string | null;
  createdAt: string;
  completedAt: string | null;
}

export interface AIAnalysis extends AIAnalysisSummary {
  organizationId: string;
  analysisType: string;
  relevantFindings: string[];
  mentionedDiagnoses: string[];
  mentionedMedications: string[];
  mentionedAllergies: string[];
  recommendations: string | null;
  startedAt: string | null;
  updatedAt: string;
  disclaimer: string;
}
