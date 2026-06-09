import type { ClinicalEvolutionStatus } from './clinicalEvolutions.types';

const LABELS: Record<ClinicalEvolutionStatus, string> = {
  draft: 'Borrador',
  active: 'Activa',
  corrected: 'Corregida',
  cancelled: 'Cancelada'
};

interface ClinicalEvolutionStatusBadgeProps {
  status: ClinicalEvolutionStatus;
}

export function ClinicalEvolutionStatusBadge({ status }: ClinicalEvolutionStatusBadgeProps) {
  return <span className={`clinical-evolution-status clinical-evolution-status--${status}`}>{LABELS[status]}</span>;
}
