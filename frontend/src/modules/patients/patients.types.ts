export type PatientStatus = 'active' | 'inactive' | 'deleted';
export type PatientSex = 'female' | 'male' | 'other' | 'unknown' | '';

export interface PatientSummary {
  id: string;
  documentType: string | null;
  documentNumber: string | null;
  firstName: string;
  lastName: string;
  birthDate: string | null;
  sex: PatientSex | null;
  email: string | null;
  phone: string | null;
  status: PatientStatus;
}

export interface Patient extends PatientSummary {
  organizationId: string;
  userId: string | null;
  address: string | null;
  emergencyContactName: string | null;
  emergencyContactPhone: string | null;
  emergencyContactRelationship: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface PatientPayload {
  documentType?: string;
  documentNumber?: string;
  firstName: string;
  lastName: string;
  birthDate?: string;
  sex?: PatientSex;
  email?: string;
  phone?: string;
  address?: string;
  emergencyContactName?: string;
  emergencyContactPhone?: string;
  emergencyContactRelationship?: string;
}
