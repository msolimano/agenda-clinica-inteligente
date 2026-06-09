import type { ClinicalDiagnosisStatus } from './clinicalDiagnoses.types';

const LABELS: Record<ClinicalDiagnosisStatus, string> = {
  suspected: 'Sospechado',
  confirmed: 'Confirmado',
  resolved: 'Resuelto',
  ruled_out: 'Descartado'
};

interface ClinicalDiagnosisStatusBadgeProps {
  status: ClinicalDiagnosisStatus;
}

export function ClinicalDiagnosisStatusBadge({ status }: ClinicalDiagnosisStatusBadgeProps) {
  return <span className={`clinical-diagnosis-status clinical-diagnosis-status--${status}`}>{LABELS[status]}</span>;
}
