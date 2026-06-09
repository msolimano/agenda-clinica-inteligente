export type MedicationCatalogStatus = 'active' | 'inactive' | 'deleted';

export interface MedicationCatalog {
  id: string;
  organizationId: string | null;
  medicationCode: string | null;
  medicationCodeSystem: string | null;
  medicationName: string;
  activeIngredient: string | null;
  presentation: string | null;
  strength: string | null;
  pharmaceuticalForm: string | null;
  route: string | null;
  manufacturer: string | null;
  status: MedicationCatalogStatus;
  createdAt: string;
  updatedAt: string;
}

export interface MedicationCatalogPayload {
  organizationId?: string | null;
  medicationCode?: string | null;
  medicationCodeSystem?: string | null;
  medicationName: string;
  activeIngredient?: string | null;
  presentation?: string | null;
  strength?: string | null;
  pharmaceuticalForm?: string | null;
  route?: string | null;
  manufacturer?: string | null;
  status?: 'active' | 'inactive';
}
