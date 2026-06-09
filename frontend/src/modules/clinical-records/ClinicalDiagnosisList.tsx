import { ClinicalDiagnosisCard } from './ClinicalDiagnosisCard';
import type { ClinicalDiagnosis, ClinicalDiagnosisStatus } from './clinicalDiagnoses.types';

interface ClinicalDiagnosisListProps {
  diagnoses: ClinicalDiagnosis[];
  disabled: boolean;
  onEdit: (diagnosis: ClinicalDiagnosis) => void;
  onStatusChange: (diagnosis: ClinicalDiagnosis, status: ClinicalDiagnosisStatus) => Promise<void>;
}

export function ClinicalDiagnosisList({ diagnoses, disabled, onEdit, onStatusChange }: ClinicalDiagnosisListProps) {
  if (!diagnoses.length) {
    return <p className="clinical-diagnosis-list__empty">No hay diagnosticos clinicos registrados.</p>;
  }

  return (
    <div className="clinical-diagnosis-list">
      {diagnoses.map((diagnosis) => (
        <ClinicalDiagnosisCard
          key={diagnosis.id}
          diagnosis={diagnosis}
          disabled={disabled}
          onEdit={onEdit}
          onStatusChange={onStatusChange}
        />
      ))}
    </div>
  );
}
