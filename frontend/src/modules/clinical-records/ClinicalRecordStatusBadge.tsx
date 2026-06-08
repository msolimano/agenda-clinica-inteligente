import type { ClinicalRecordStatus } from './clinicalRecords.types';

const labels: Record<ClinicalRecordStatus, string> = {
  draft: 'Borrador',
  open: 'Abierta',
  closed: 'Cerrada'
};

interface ClinicalRecordStatusBadgeProps {
  status: ClinicalRecordStatus;
}

export function ClinicalRecordStatusBadge({ status }: ClinicalRecordStatusBadgeProps) {
  return <span className={`clinical-record-status clinical-record-status--${status}`}>{labels[status]}</span>;
}
