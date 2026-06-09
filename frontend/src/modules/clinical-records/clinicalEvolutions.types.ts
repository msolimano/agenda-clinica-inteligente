export type ClinicalEvolutionStatus = 'draft' | 'active' | 'corrected' | 'cancelled';

export interface ClinicalEvolution {
  id: string;
  organizationId: string;
  clinicalRecordId: string;
  patientId: string;
  patientName: string;
  professionalId: string;
  professionalName: string;
  evolutionDate: string;
  subjective: string | null;
  objective: string | null;
  assessment: string | null;
  plan: string | null;
  notes: string | null;
  evolutionStatus: ClinicalEvolutionStatus;
  status: 'active' | 'deleted';
  createdAt: string;
  updatedAt: string;
}

export interface ClinicalEvolutionPayload {
  evolutionDate: string;
  subjective?: string;
  objective?: string;
  assessment?: string;
  plan?: string;
  notes?: string;
  evolutionStatus?: ClinicalEvolutionStatus;
}
