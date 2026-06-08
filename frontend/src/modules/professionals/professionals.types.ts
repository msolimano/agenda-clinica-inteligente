export type ProfessionalStatus = 'active' | 'inactive' | 'deleted';

export interface Specialty {
  id: string;
  organizationId: string | null;
  name: string;
  code: string | null;
  status: ProfessionalStatus;
}

export interface ProfessionalSpecialty {
  id: string;
  specialtyId: string;
  specialtyName: string;
  specialtyCode: string | null;
  primary: boolean;
  status: ProfessionalStatus;
}

export interface Professional {
  id: string;
  organizationId: string;
  userId: string | null;
  documentType: string | null;
  documentNumber: string | null;
  registryNumber: string | null;
  firstName: string;
  lastName: string;
  email: string | null;
  phone: string | null;
  status: ProfessionalStatus;
  specialties: ProfessionalSpecialty[];
  createdAt: string;
  updatedAt: string;
}

export interface ProfessionalPayload {
  documentType?: string;
  documentNumber?: string;
  registryNumber?: string;
  firstName: string;
  lastName: string;
  email?: string;
  phone?: string;
  specialties?: Array<{
    specialtyId: string;
    primary: boolean;
  }>;
}
