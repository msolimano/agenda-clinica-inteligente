import { ClinicalPrescriptionCard } from './ClinicalPrescriptionCard';
import type { ClinicalPrescription, ClinicalPrescriptionStatus } from './clinicalPrescriptions.types';

interface ClinicalPrescriptionListProps {
  prescriptions: ClinicalPrescription[];
  disabled: boolean;
  onEdit: (prescription: ClinicalPrescription) => void;
  onStatusChange: (prescription: ClinicalPrescription, status: ClinicalPrescriptionStatus) => Promise<void>;
}

export function ClinicalPrescriptionList({ prescriptions, disabled, onEdit, onStatusChange }: ClinicalPrescriptionListProps) {
  if (!prescriptions.length) {
    return <p className="clinical-prescription-list__empty">No hay prescripciones clinicas registradas.</p>;
  }

  return (
    <div className="clinical-prescription-list">
      {prescriptions.map((prescription) => (
        <ClinicalPrescriptionCard
          key={prescription.id}
          prescription={prescription}
          disabled={disabled}
          onEdit={onEdit}
          onStatusChange={onStatusChange}
        />
      ))}
    </div>
  );
}
