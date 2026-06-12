import type { ClinicalInsight, ClinicalInsightReviewPayload } from './clinicalInsights.types';

const API_BASE = '/api';

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
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

export async function listDocumentClinicalInsights(documentId: string) {
  return request<ClinicalInsight[]>(`/clinical-documents/${documentId}/insights`);
}

export async function acceptClinicalInsight(id: string, payload: ClinicalInsightReviewPayload = {}) {
  return request<ClinicalInsight>(`/clinical-insights/${id}/accept`, {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export async function rejectClinicalInsight(id: string, payload: ClinicalInsightReviewPayload = {}) {
  return request<ClinicalInsight>(`/clinical-insights/${id}/reject`, {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export async function dismissClinicalInsight(id: string, payload: ClinicalInsightReviewPayload = {}) {
  return request<ClinicalInsight>(`/clinical-insights/${id}/dismiss`, {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}
