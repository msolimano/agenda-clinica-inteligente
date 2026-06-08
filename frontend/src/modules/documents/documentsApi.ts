import type { AIAnalysis, AIAnalysisStatus, AIAnalysisSummary, ClinicalDocument, ClinicalDocumentSummary, ClinicalDocumentUploadPayload, PatientOption, ProfessionalOption } from './documents.types';

const API_BASE = '/api';

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: options?.body instanceof FormData ? options.headers : {
      'Content-Type': 'application/json',
      ...options?.headers
    },
    ...options
  });

  if (!response.ok) {
    const payload = await response.json().catch(() => null);
    throw new Error(payload?.message ?? 'No fue posible completar la solicitud');
  }

  return response.json() as Promise<T>;
}

export async function listPatients() {
  return request<PatientOption[]>('/patients');
}

export async function listProfessionals() {
  return request<ProfessionalOption[]>('/professionals');
}

export async function listPatientDocuments(patientId: string) {
  return request<ClinicalDocumentSummary[]>(`/patients/${patientId}/documents`);
}

export async function listClinicalDocuments(params: { patientId?: string; documentType?: string }) {
  const query = new URLSearchParams();
  if (params.patientId) {
    query.set('patientId', params.patientId);
  }
  if (params.documentType) {
    query.set('documentType', params.documentType);
  }
  const suffix = query.toString() ? `?${query.toString()}` : '';
  return request<ClinicalDocumentSummary[]>(`/clinical-documents${suffix}`);
}

export async function getClinicalDocument(id: string) {
  return request<ClinicalDocument>(`/clinical-documents/${id}`);
}

export async function uploadClinicalDocument(payload: ClinicalDocumentUploadPayload) {
  const formData = new FormData();
  formData.append('patientId', payload.patientId);
  if (payload.professionalId) {
    formData.append('professionalId', payload.professionalId);
  }
  formData.append('documentType', payload.documentType);
  formData.append('title', payload.title);
  if (payload.description) {
    formData.append('description', payload.description);
  }
  formData.append('file', payload.file);

  return request<ClinicalDocument>('/clinical-documents', {
    method: 'POST',
    body: formData
  });
}

export async function deleteClinicalDocument(id: string) {
  return request<ClinicalDocument>(`/clinical-documents/${id}`, { method: 'DELETE' });
}

export function downloadClinicalDocumentUrl(id: string) {
  return `${API_BASE}/clinical-documents/${id}/download`;
}


export async function requestDocumentAnalysis(documentId: string) {
  return request<AIAnalysis>(`/clinical-documents/${documentId}/ai-analysis`, { method: 'POST' });
}

export async function listDocumentAnalyses(documentId: string) {
  return request<AIAnalysisSummary[]>(`/clinical-documents/${documentId}/ai-analysis`);
}

export async function getAIAnalysis(id: string) {
  return request<AIAnalysis>(`/ai-analyses/${id}`);
}

export async function retryAIAnalysis(id: string) {
  return request<AIAnalysis>(`/ai-analyses/${id}/retry`, { method: 'POST' });
}

export async function listAIAnalyses(params: { status?: AIAnalysisStatus; patientId?: string; documentId?: string } = {}) {
  const query = new URLSearchParams();
  if (params.status) {
    query.set('status', params.status);
  }
  if (params.patientId) {
    query.set('patientId', params.patientId);
  }
  if (params.documentId) {
    query.set('documentId', params.documentId);
  }
  const suffix = query.toString() ? `?${query.toString()}` : '';
  return request<AIAnalysisSummary[]>(`/ai-analyses${suffix}`);
}
