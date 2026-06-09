import type { ClinicalPrescriptionStatus } from './clinicalPrescriptions.types';

const LABELS: Record<ClinicalPrescriptionStatus, string> = {
  draft: 'Borrador',
  active: 'Activa',
  suspended: 'Suspendida',
  completed: 'Completada',
  cancelled: 'Cancelada'
};

interface ClinicalPrescriptionStatusBadgeProps {
  status: ClinicalPrescriptionStatus;
}

export function ClinicalPrescriptionStatusBadge({ status }: ClinicalPrescriptionStatusBadgeProps) {
  return <span className={`clinical-prescription-status clinical-prescription-status--${status}`}>{LABELS[status]}</span>;
}
