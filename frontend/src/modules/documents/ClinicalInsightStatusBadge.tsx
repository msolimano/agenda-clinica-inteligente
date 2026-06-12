import type { ClinicalInsightStatus } from './clinicalInsights.types';

const labels: Record<ClinicalInsightStatus, string> = {
  pending: 'Pendiente',
  accepted: 'Aceptado',
  rejected: 'Rechazado',
  dismissed: 'Descartado'
};

interface ClinicalInsightStatusBadgeProps {
  status: ClinicalInsightStatus;
}

export function ClinicalInsightStatusBadge({ status }: ClinicalInsightStatusBadgeProps) {
  return <span className={`clinical-insight-status clinical-insight-status--${status}`}>{labels[status]}</span>;
}
