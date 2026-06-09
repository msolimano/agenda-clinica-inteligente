import type { DiagnosisCatalogStatus } from './diagnosisCatalog.types';

const labels: Record<DiagnosisCatalogStatus, string> = {
  active: 'Activo',
  inactive: 'Inactivo',
  deleted: 'Eliminado'
};

interface DiagnosisCatalogStatusBadgeProps {
  status: DiagnosisCatalogStatus;
}

export function DiagnosisCatalogStatusBadge({ status }: DiagnosisCatalogStatusBadgeProps) {
  return <span className={`diagnosis-catalog-status diagnosis-catalog-status--${status}`}>{labels[status]}</span>;
}
