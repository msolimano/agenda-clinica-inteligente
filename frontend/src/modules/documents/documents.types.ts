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
export type AIConsentStatus = 'active' | 'revoked' | 'deleted' | 'not_requested';
export type TextExtractionStatus = 'not_requested' | 'completed' | 'failed' | 'unsupported' | 'empty';


export interface AIProviderStatus {
  activeProvider: 'mock' | 'openai' | string;
  configured: boolean;
  modelName: string | null;
  promptVersion: string;
  mockAvailable: boolean;
  openAIConfigured: boolean;
  message: string;
}

export interface AIConsent {
  id: string | null;
  organizationId: string;
  patientId: string;
  consentType: 'ai_analysis';
  active: boolean;
  granted: boolean;
  consentVersion: string;
  grantedAt: string | null;
  revokedAt: string | null;
  source: string | null;
  notes: string | null;
  status: AIConsentStatus;
  createdAt: string | null;
  updatedAt: string | null;
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
  aiConsentId: string | null;
  status: AIAnalysisStatus;
  modelName: string | null;
  providerName: string | null;
  providerRequestId: string | null;
  promptVersion: string | null;
  inputTokenCount: number | null;
  outputTokenCount: number | null;
  totalTokenCount: number | null;
  latencyMs: number | null;
  providerErrorCode: string | null;
  textExtractionStatus: TextExtractionStatus | null;
  textExtractionMethod: string | null;
  textExtractionConfidence: number | null;
  extractedTextPreview: string | null;
  textExtractionErrorMessage: string | null;
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
