export type DiagnosisCatalogStatus = 'active' | 'inactive' | 'deleted';

export interface DiagnosisCatalog {
  id: string;
  organizationId: string | null;
  diagnosisCode: string | null;
  codeSystem: string | null;
  diagnosisDisplay: string;
  category: string | null;
  description: string | null;
  status: DiagnosisCatalogStatus;
  createdAt: string;
  updatedAt: string;
}

export interface DiagnosisCatalogPayload {
  organizationId?: string | null;
  diagnosisCode?: string | null;
  codeSystem?: string | null;
  diagnosisDisplay: string;
  category?: string | null;
  description?: string | null;
  status?: 'active' | 'inactive';
}
