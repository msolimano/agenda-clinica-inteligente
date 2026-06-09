import type { MedicationCatalogStatus } from './medications.types';
import './medications.css';

const LABELS: Record<MedicationCatalogStatus, string> = {
  active: 'Activo',
  inactive: 'Inactivo',
  deleted: 'Eliminado'
};

interface MedicationStatusBadgeProps {
  status: MedicationCatalogStatus;
}

export function MedicationStatusBadge({ status }: MedicationStatusBadgeProps) {
  return <span className={`medication-status medication-status--${status}`}>{LABELS[status]}</span>;
}
