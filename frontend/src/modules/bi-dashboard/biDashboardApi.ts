import type {
  BIAIKpi,
  BIAITrend,
  BIAgendaOverview,
  BIAppointmentTrend,
  BIBarItem,
  BIClinicalKpi,
  BIDashboardFilters,
  BIDocumentKpi,
  BIFHIRKpi,
  BISpecialtyOccupancy,
  BISummary
} from './biDashboard.types';

const API_BASE = '/api';

async function request<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`);

  if (!response.ok) {
    const payload = await response.json().catch(() => null);
    throw new Error(payload?.message ?? 'No fue posible completar la solicitud');
  }

  return response.json() as Promise<T>;
}

export async function getBISummary(filters: BIDashboardFilters) {
  return request<BISummary>(`/bi/summary${queryString(filters)}`);
}

export async function getBIAgenda(filters: BIDashboardFilters) {
  return request<BIAgendaOverview>(`/bi/agenda${queryString(filters)}`);
}

export async function getBIClinical(filters: BIDashboardFilters) {
  return request<BIClinicalKpi>(`/bi/clinical${queryString(filters)}`);
}

export async function getBIDocuments(filters: Pick<BIDashboardFilters, 'from' | 'to'>) {
  return request<BIDocumentKpi>(`/bi/documents${queryString(filters)}`);
}

export async function getBIAI(filters: Pick<BIDashboardFilters, 'from' | 'to'>) {
  return request<BIAIKpi>(`/bi/ai${queryString(filters)}`);
}

export async function getBIFHIR(filters: Pick<BIDashboardFilters, 'from' | 'to'>) {
  return request<BIFHIRKpi>(`/bi/fhir${queryString(filters)}`);
}

export async function getBIAppointmentTrend(filters: BIDashboardFilters) {
  return request<BIAppointmentTrend>(`/bi/trends/appointments${queryString(filters)}`);
}

export async function getBISpecialtyOccupancy(filters: BIDashboardFilters) {
  return request<BISpecialtyOccupancy[]>(`/bi/specialties/occupancy${queryString(filters)}`);
}

export async function getBITopDiagnoses(filters: BIDashboardFilters) {
  return request<BIBarItem[]>(`/bi/clinical/top-diagnoses${queryString(filters)}`);
}

export async function getBITopMedications(filters: BIDashboardFilters) {
  return request<BIBarItem[]>(`/bi/clinical/top-medications${queryString(filters)}`);
}

export async function getBIAITrends(filters: Pick<BIDashboardFilters, 'from' | 'to'>) {
  return request<BIAITrend>(`/bi/ai/trends${queryString(filters)}`);
}

function queryString(filters: BIDashboardFilters) {
  const query = new URLSearchParams();
  if (filters.from) {
    query.set('from', filters.from);
  }
  if (filters.to) {
    query.set('to', filters.to);
  }
  if (filters.professionalId) {
    query.set('professionalId', filters.professionalId);
  }
  if (filters.specialtyId) {
    query.set('specialtyId', filters.specialtyId);
  }
  const value = query.toString();
  return value ? `?${value}` : '';
}
